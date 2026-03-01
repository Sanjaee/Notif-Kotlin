package ws

import (
	"time"

	"github.com/gorilla/websocket"
)

const (
	writeWait      = 10 * time.Second
	pongWait       = 60 * time.Second
	pingPeriod     = (pongWait * 9) / 10
	maxMessageSize = 512 * 1024
)

// ReadPump reads from the WebSocket.
func (c *Client) ReadPump() {
	defer func() {
		c.Hub.Unregister <- c
	}()
	c.Conn().SetReadLimit(maxMessageSize)
	c.Conn().SetReadDeadline(time.Now().Add(pongWait))
	c.Conn().SetPongHandler(func(string) error {
		c.Conn().SetReadDeadline(time.Now().Add(pongWait))
		return nil
	})
	for {
		_, _, err := c.Conn().ReadMessage()
		if err != nil {
			break
		}
		c.Conn().SetReadDeadline(time.Now().Add(pongWait))
	}
}

// WritePump sends messages from hub to the client.
func (c *Client) WritePump() {
	ticker := time.NewTicker(pingPeriod)
	defer func() {
		ticker.Stop()
		c.Conn().Close()
	}()
	for {
		select {
		case msg, ok := <-c.Send:
			if !ok {
				c.Conn().WriteMessage(websocket.CloseMessage, []byte{})
				return
			}
			c.Conn().SetWriteDeadline(time.Now().Add(writeWait))
			if err := c.Conn().WriteMessage(websocket.TextMessage, msg); err != nil {
				return
			}
		case <-ticker.C:
			c.Conn().SetWriteDeadline(time.Now().Add(writeWait))
			if err := c.Conn().WriteMessage(websocket.PingMessage, nil); err != nil {
				return
			}
		}
	}
}
