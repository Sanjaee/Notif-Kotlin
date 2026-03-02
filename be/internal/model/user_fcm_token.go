package model

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// UserFcmToken menyimpan FCM token per device. Satu user bisa punya banyak token (banyak device).
// Token bisa di-hit (kirim notifikasi) kapan saja dari backend tanpa app dibuka.
type UserFcmToken struct {
	ID        string         `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UserID    string         `gorm:"type:uuid;not null;uniqueIndex:idx_user_fcm" json:"user_id"`
	FcmToken  string         `gorm:"type:text;not null;uniqueIndex:idx_user_fcm" json:"-"` // unique (user_id, fcm_token)
	CreatedAt time.Time      `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt time.Time      `gorm:"autoUpdateTime" json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`
}

func (u *UserFcmToken) BeforeCreate(tx *gorm.DB) error {
	if u.ID == "" {
		u.ID = uuid.New().String()
	}
	return nil
}

func (UserFcmToken) TableName() string {
	return "user_fcm_tokens"
}
