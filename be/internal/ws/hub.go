package ws

import (
	"encoding/json"
	"log"
	"sync"

	"github.com/gorilla/websocket"
)

type Client struct {
	UserID string
	Send   chan []byte
	conn   *websocket.Conn
	Hub    *Hub
}

type UserMessage struct {
	UserID  string
	Payload []byte
}

type Hub struct {
	mu              sync.RWMutex
	clients         map[string]map[*Client]bool
	broadcastToUser chan *UserMessage
	Register        chan *Client
	Unregister      chan *Client
}

func NewHub() *Hub {
	return &Hub{
		clients:         make(map[string]map[*Client]bool),
		broadcastToUser:  make(chan *UserMessage, 256),
		Register:        make(chan *Client),
		Unregister:      make(chan *Client),
	}
}

func (h *Hub) Run() {
	for {
		select {
		case c := <-h.Register:
			h.mu.Lock()
			if h.clients[c.UserID] == nil {
				h.clients[c.UserID] = make(map[*Client]bool)
			}
			h.clients[c.UserID][c] = true
			h.mu.Unlock()
			log.Printf("[ws] user %s connected", c.UserID)

		case c := <-h.Unregister:
			h.mu.Lock()
			if m, ok := h.clients[c.UserID]; ok {
				delete(m, c)
				if len(m) == 0 {
					delete(h.clients, c.UserID)
				}
			}
			close(c.Send)
			h.mu.Unlock()

		case um := <-h.broadcastToUser:
			h.mu.RLock()
			for client := range h.clients[um.UserID] {
				select {
				case client.Send <- um.Payload:
				default:
				}
			}
			h.mu.RUnlock()
		}
	}
}

func (h *Hub) SendToUser(userID string, v interface{}) {
	payload, err := json.Marshal(v)
	if err != nil {
		return
	}
	h.broadcastToUser <- &UserMessage{UserID: userID, Payload: payload}
}

func (c *Client) SetConn(conn *websocket.Conn) { c.conn = conn }
func (c *Client) Conn() *websocket.Conn       { return c.conn }
