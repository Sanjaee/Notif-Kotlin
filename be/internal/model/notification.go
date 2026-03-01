package model

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Notification struct {
	ID        string         `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID    string         `gorm:"type:uuid;not null;index" json:"user_id"`
	MessageID string         `gorm:"type:uuid;not null;index" json:"message_id"`
	IsRead    bool           `gorm:"default:false" json:"is_read"`
	CreatedAt time.Time      `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt time.Time      `gorm:"autoUpdateTime" json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`

	Message *Message `gorm:"foreignKey:MessageID" json:"message,omitempty"`
}

func (n *Notification) BeforeCreate(tx *gorm.DB) error {
	if n.ID == "" {
		n.ID = uuid.New().String()
	}
	return nil
}

func (Notification) TableName() string {
	return "notifications"
}
