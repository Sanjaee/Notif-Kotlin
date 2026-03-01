package service

import (
	"errors"

	"yourapp/internal/model"
	"yourapp/internal/repository"
)

type ChatService interface {
	ListUsersForChat(currentUserID string, limit, offset int) ([]*model.User, error)
	GetOrCreateConversation(currentUserID, otherUserID string) (*model.Conversation, error)
	ListConversations(userID string, limit, offset int) ([]ConversationWithMeta, error)
	GetMessages(conversationID, userID string, limit, offset int) ([]*model.Message, error)
	SendMessage(conversationID, senderID, message, messageType string) (*model.Message, error)
	MarkConversationRead(conversationID, userID string) error
	ListNotifications(userID string, limit, offset int) ([]*model.Notification, error)
	MarkNotificationRead(notificationID, userID string) error
	UnreadCount(userID string) (int64, error)
	IsMember(conversationID, userID string) (bool, error)
	GetConversationMemberIDs(conversationID string) ([]string, error)
}

type ConversationWithMeta struct {
	Conversation *model.Conversation `json:"conversation"`
	OtherMember  *model.User         `json:"other_member,omitempty"`
	LastMessage  *model.Message      `json:"last_message,omitempty"`
	UnreadCount  int64               `json:"unread_count"`
}

type chatService struct {
	userRepo  repository.UserRepository
	convRepo  repository.ConversationRepository
	msgRepo   repository.MessageRepository
	notifRepo repository.NotificationRepository
}

func NewChatService(
	userRepo repository.UserRepository,
	convRepo repository.ConversationRepository,
	msgRepo repository.MessageRepository,
	notifRepo repository.NotificationRepository,
) ChatService {
	return &chatService{
		userRepo:  userRepo,
		convRepo:  convRepo,
		msgRepo:   msgRepo,
		notifRepo: notifRepo,
	}
}

func (s *chatService) ListUsersForChat(currentUserID string, limit, offset int) ([]*model.User, error) {
	return s.userRepo.ListForChat(currentUserID, limit, offset)
}

func (s *chatService) GetOrCreateConversation(currentUserID, otherUserID string) (*model.Conversation, error) {
	if currentUserID == otherUserID {
		return nil, errors.New("cannot create conversation with yourself")
	}
	if _, err := s.userRepo.FindByID(otherUserID); err != nil {
		return nil, errors.New("user not found")
	}
	return s.convRepo.GetOrCreateDM(currentUserID, otherUserID)
}

func (s *chatService) otherMemberID(conversationID, excludeUserID string) (string, error) {
	ids, err := s.convRepo.GetMemberIDs(conversationID)
	if err != nil || len(ids) == 0 {
		return "", err
	}
	for _, id := range ids {
		if id != excludeUserID {
			return id, nil
		}
	}
	return "", nil
}

func (s *chatService) ListConversations(userID string, limit, offset int) ([]ConversationWithMeta, error) {
	convs, err := s.convRepo.ListByUserID(userID, limit, offset)
	if err != nil {
		return nil, err
	}
	result := make([]ConversationWithMeta, 0, len(convs))
	for _, c := range convs {
		meta := ConversationWithMeta{Conversation: c}
		otherID, err := s.otherMemberID(c.ID, userID)
		if err == nil && otherID != "" {
			u, err := s.userRepo.FindByID(otherID)
			if err == nil {
				meta.OtherMember = u
			}
		}
		msgs, _ := s.msgRepo.ListByConversationID(c.ID, 1, 0)
		if len(msgs) > 0 {
			meta.LastMessage = msgs[0]
		}
		meta.UnreadCount, _ = s.msgRepo.CountUnreadByConversationAndUser(c.ID, userID)
		result = append(result, meta)
	}
	return result, nil
}

func (s *chatService) IsMember(conversationID, userID string) (bool, error) {
	ids, err := s.convRepo.GetMemberIDs(conversationID)
	if err != nil {
		return false, err
	}
	for _, id := range ids {
		if id == userID {
			return true, nil
		}
	}
	return false, nil
}

func (s *chatService) GetMessages(conversationID, userID string, limit, offset int) ([]*model.Message, error) {
	ok, err := s.IsMember(conversationID, userID)
	if err != nil || !ok {
		return nil, errors.New("conversation not found or access denied")
	}
	return s.msgRepo.ListByConversationID(conversationID, limit, offset)
}

func (s *chatService) SendMessage(conversationID, senderID, message, messageType string) (*model.Message, error) {
	if messageType == "" {
		messageType = model.MessageTypeText
	}
	ok, err := s.IsMember(conversationID, senderID)
	if err != nil || !ok {
		return nil, errors.New("conversation not found or access denied")
	}
	msg := &model.Message{
		ConversationID: conversationID,
		SenderID:       senderID,
		Message:        message,
		MessageType:    messageType,
	}
	if err := s.msgRepo.Create(msg); err != nil {
		return nil, err
	}
	conv, _ := s.convRepo.FindByID(conversationID)
	if conv != nil {
		_ = s.convRepo.Update(conv) // bump updated_at for list order
	}
	memberIDs, _ := s.convRepo.GetMemberIDs(conversationID)
	for _, uid := range memberIDs {
		if uid != senderID {
			_ = s.notifRepo.Create(&model.Notification{UserID: uid, MessageID: msg.ID})
		}
	}
	// Return with Sender preloaded for API/WS
	msg, _ = s.msgRepo.FindByID(msg.ID)
	return msg, nil
}

func (s *chatService) MarkConversationRead(conversationID, userID string) error {
	ok, err := s.IsMember(conversationID, userID)
	if err != nil || !ok {
		return errors.New("conversation not found or access denied")
	}
	if err := s.msgRepo.MarkReadByConversationAndUser(conversationID, userID); err != nil {
		return err
	}
	return s.notifRepo.MarkReadByUserIDAndConversationID(userID, conversationID)
}

func (s *chatService) ListNotifications(userID string, limit, offset int) ([]*model.Notification, error) {
	return s.notifRepo.ListByUserID(userID, limit, offset)
}

func (s *chatService) MarkNotificationRead(notificationID, userID string) error {
	n, err := s.notifRepo.FindByID(notificationID)
	if err != nil || n == nil || n.UserID != userID {
		return errors.New("notification not found")
	}
	return s.notifRepo.MarkReadByID(notificationID)
}

func (s *chatService) UnreadCount(userID string) (int64, error) {
	return s.notifRepo.CountUnreadByUserID(userID)
}

func (s *chatService) GetConversationMemberIDs(conversationID string) ([]string, error) {
	return s.convRepo.GetMemberIDs(conversationID)
}
