package app

import (
	"context"
	"log"
	"net/http"
	"strconv"
	"strings"

	"yourapp/internal/fcm"
	"yourapp/internal/model"
	"yourapp/internal/service"
	"yourapp/internal/util"
	"yourapp/internal/ws"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

var wsUpgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

type ChatHandler struct {
	chatService service.ChatService
	jwtSecret   string
	hub         *ws.Hub
	fcmClient   *fcm.Client
}

func NewChatHandler(chatService service.ChatService, jwtSecret string, hub *ws.Hub, fcmClient *fcm.Client) *ChatHandler {
	return &ChatHandler{chatService: chatService, jwtSecret: jwtSecret, hub: hub, fcmClient: fcmClient}
}

func (h *ChatHandler) userID(c *gin.Context) string {
	uid, ok := c.Get("userID")
	if !ok {
		util.Unauthorized(c, "unauthorized")
		return ""
	}
	return uid.(string)
}

// ListUsers GET /api/v1/chat/users
func (h *ChatHandler) ListUsers(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	offset, _ := strconv.Atoi(c.DefaultQuery("offset", "0"))
	users, err := h.chatService.ListUsersForChat(userID, limit, offset)
	if err != nil {
		util.ErrorResponse(c, http.StatusInternalServerError, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", gin.H{"users": users})
}

// GetOrCreateConversation POST /api/v1/chat/conversations
func (h *ChatHandler) GetOrCreateConversation(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	var req struct {
		OtherUserID string `json:"other_user_id" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		util.BadRequest(c, "other_user_id is required")
		return
	}
	conv, err := h.chatService.GetOrCreateConversation(userID, req.OtherUserID)
	if err != nil {
		util.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", gin.H{"conversation": conv})
}

// ListConversations GET /api/v1/chat/conversations
func (h *ChatHandler) ListConversations(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	offset, _ := strconv.Atoi(c.DefaultQuery("offset", "0"))
	list, err := h.chatService.ListConversations(userID, limit, offset)
	if err != nil {
		util.ErrorResponse(c, http.StatusInternalServerError, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", gin.H{"conversations": list})
}

// GetMessages GET /api/v1/chat/conversations/:id/messages
func (h *ChatHandler) GetMessages(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	conversationID := c.Param("id")
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "50"))
	offset, _ := strconv.Atoi(c.DefaultQuery("offset", "0"))
	msgs, err := h.chatService.GetMessages(conversationID, userID, limit, offset)
	if err != nil {
		util.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", gin.H{"messages": msgs})
}

// SendMessage POST /api/v1/chat/conversations/:id/messages
func (h *ChatHandler) SendMessage(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	conversationID := c.Param("id")
	var req struct {
		Message     string `json:"message" binding:"required"`
		MessageType string `json:"message_type"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		util.BadRequest(c, "message is required")
		return
	}
	if req.MessageType == "" {
		req.MessageType = model.MessageTypeText
	}
	msg, err := h.chatService.SendMessage(conversationID, userID, req.Message, req.MessageType)
	if err != nil {
		util.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}
	h.pushNewMessage(conversationID, userID, msg)
	util.SuccessResponse(c, http.StatusCreated, "OK", gin.H{"message": msg})
}

func (h *ChatHandler) pushNewMessage(conversationID, senderID string, msg *model.Message) {
	ids, err := h.chatService.GetConversationMemberIDs(conversationID)
	if err != nil {
		log.Printf("[chat] pushNewMessage GetConversationMemberIDs: %v", err)
		return
	}
	senderName := ""
	if msg.Sender != nil {
		senderName = msg.Sender.FullName
	}
	if senderName == "" {
		senderName = "Someone"
	}
	for _, uid := range ids {
		if uid != senderID {
			log.Printf("[chat] push new_message to user %s (conv %s)", uid, conversationID)
			h.hub.SendToUser(uid, gin.H{"type": "new_message", "message": msg})
			h.hub.SendToUser(uid, gin.H{"type": "new_notification", "message_id": msg.ID})
			// FCM: agar notifikasi muncul saat app closed (seperti WhatsApp/Discord)
			if h.fcmClient == nil {
				log.Printf("[chat] FCM disabled: set FIREBASE_CREDENTIALS_PATH di .env agar notif muncul saat app closed")
			} else {
				tokens, _ := h.chatService.GetUserFCMTokens(uid)
				if len(tokens) == 0 {
					log.Printf("[chat] FCM skip: user %s belum daftar FCM token (buka app → login → token akan terkirim ke POST /chat/fcm-token)", uid)
				} else {
					data := map[string]string{
						"conversation_id":     conversationID,
						"other_display_name": senderName,
						"sender_name":        senderName,
						"title":              senderName,
						"body":               msg.Message,
						"message":            msg.Message,
					}
					for _, fcmToken := range tokens {
						if err := h.fcmClient.Send(context.Background(), fcmToken, data, senderName, msg.Message); err != nil {
							log.Printf("[chat] fcm send to user %s (token): %v", uid, err)
						} else {
							log.Printf("[chat] FCM sent to user %s (notif akan muncul di device)", uid)
						}
					}
				}
			}
		}
	}
}

// MarkConversationRead POST /api/v1/chat/conversations/:id/read
func (h *ChatHandler) MarkConversationRead(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	conversationID := c.Param("id")
	if err := h.chatService.MarkConversationRead(conversationID, userID); err != nil {
		util.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", nil)
}

// ListNotifications GET /api/v1/chat/notifications
func (h *ChatHandler) ListNotifications(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "30"))
	offset, _ := strconv.Atoi(c.DefaultQuery("offset", "0"))
	list, err := h.chatService.ListNotifications(userID, limit, offset)
	if err != nil {
		util.ErrorResponse(c, http.StatusInternalServerError, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", gin.H{"notifications": list})
}

// MarkNotificationRead POST /api/v1/chat/notifications/:id/read
func (h *ChatHandler) MarkNotificationRead(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	notificationID := c.Param("id")
	if err := h.chatService.MarkNotificationRead(notificationID, userID); err != nil {
		util.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", nil)
}

// UnreadCount GET /api/v1/chat/notifications/unread-count
func (h *ChatHandler) UnreadCount(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	count, err := h.chatService.UnreadCount(userID)
	if err != nil {
		util.ErrorResponse(c, http.StatusInternalServerError, err.Error(), nil)
		return
	}
	util.SuccessResponse(c, http.StatusOK, "OK", gin.H{"unread_count": count})
}

// RegisterFCMToken POST /api/v1/chat/fcm-token — daftarkan FCM token agar server kirim push saat app closed
func (h *ChatHandler) RegisterFCMToken(c *gin.Context) {
	userID := h.userID(c)
	if userID == "" {
		return
	}
	var req struct {
		FcmToken string `json:"fcm_token" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		util.BadRequest(c, "fcm_token is required")
		return
	}
	if err := h.chatService.UpdateFCMToken(userID, req.FcmToken); err != nil {
		util.ErrorResponse(c, http.StatusInternalServerError, err.Error(), nil)
		return
	}
	log.Printf("[chat] FCM token registered for user %s (notif akan muncul saat app closed)", userID)
	util.SuccessResponse(c, http.StatusOK, "OK", nil)
}

// HandleWebSocket GET /api/v1/ws/chat?token=xxx
func (h *ChatHandler) HandleWebSocket(c *gin.Context) {
	token := c.Query("token")
	if token == "" {
		token = strings.TrimPrefix(c.GetHeader("Authorization"), "Bearer ")
	}
	if token == "" {
		log.Printf("[ws] connection rejected: token required")
		util.Unauthorized(c, "token required")
		return
	}
	claims, err := util.ValidateToken(token, h.jwtSecret)
	if err != nil {
		log.Printf("[ws] connection rejected: invalid token: %v", err)
		util.Unauthorized(c, "invalid or expired token")
		return
	}
	conn, err := wsUpgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		log.Printf("[ws] upgrade failed: %v", err)
		return
	}
	log.Printf("[ws] user %s connected", claims.UserID)
	client := &ws.Client{
		UserID: claims.UserID,
		Send:   make(chan []byte, 256),
		Hub:    h.hub,
	}
	client.SetConn(conn)
	h.hub.Register <- client
	go client.WritePump()
	client.ReadPump()
}
