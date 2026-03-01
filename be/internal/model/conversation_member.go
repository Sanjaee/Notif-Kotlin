package model

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type ConversationMember struct {
	ID             string         `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	ConversationID string         `gorm:"type:uuid;not null;index" json:"conversation_id"`
	UserID         string         `gorm:"type:uuid;not null;index" json:"user_id"`
	JoinedAt       time.Time      `gorm:"autoCreateTime" json:"joined_at"`
	CreatedAt      time.Time      `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt      time.Time      `gorm:"autoUpdateTime" json:"updated_at"`
	DeletedAt      gorm.DeletedAt `gorm:"index" json:"-"`

	User *User `gorm:"foreignKey:UserID" json:"user,omitempty"`
}

func (m *ConversationMember) BeforeCreate(tx *gorm.DB) error {
	if m.ID == "" {
		m.ID = uuid.New().String()
	}
	return nil
}

func (ConversationMember) TableName() string {
	return "conversation_members"
}
