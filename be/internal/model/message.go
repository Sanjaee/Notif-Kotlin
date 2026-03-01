package model

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// MessageType: text, image, etc.
const (
	MessageTypeText  = "text"
	MessageTypeImage = "image"
)

type Message struct {
	ID             string         `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	ConversationID string         `gorm:"type:uuid;not null;index" json:"conversation_id"`
	SenderID       string         `gorm:"type:uuid;not null;index" json:"sender_id"`
	Message        string         `gorm:"type:text;not null" json:"message"`
	MessageType    string         `gorm:"type:varchar(20);default:'text'" json:"message_type"`
	IsRead         bool           `gorm:"default:false" json:"is_read"`
	CreatedAt      time.Time      `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt      time.Time      `gorm:"autoUpdateTime" json:"updated_at"`
	DeletedAt      gorm.DeletedAt `gorm:"index" json:"-"`

	Sender *User `gorm:"foreignKey:SenderID" json:"sender,omitempty"`
}

func (m *Message) BeforeCreate(tx *gorm.DB) error {
	if m.ID == "" {
		m.ID = uuid.New().String()
	}
	if m.MessageType == "" {
		m.MessageType = MessageTypeText
	}
	return nil
}

func (Message) TableName() string {
	return "messages"
}
