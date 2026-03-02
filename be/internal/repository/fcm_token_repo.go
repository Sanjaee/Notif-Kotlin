package repository

import (
	"time"

	"yourapp/internal/model"

	"gorm.io/gorm"
)

type FcmTokenRepository interface {
	// AddToken menyimpan token untuk user. Jika (user_id, token) sudah ada, update updated_at.
	// Satu user bisa punya banyak token (banyak device).
	AddToken(userID, fcmToken string) error
	// GetTokensByUserID mengembalikan semua FCM token user (untuk kirim ke semua device).
	GetTokensByUserID(userID string) ([]string, error)
}

type fcmTokenRepository struct {
	db *gorm.DB
}

func NewFcmTokenRepository(db *gorm.DB) FcmTokenRepository {
	return &fcmTokenRepository{db: db}
}

func (r *fcmTokenRepository) AddToken(userID, fcmToken string) error {
	if userID == "" || fcmToken == "" {
		return nil
	}
	var existing model.UserFcmToken
	err := r.db.Where("user_id = ? AND fcm_token = ?", userID, fcmToken).First(&existing).Error
	if err == nil {
		return r.db.Model(&existing).Update("updated_at", time.Now()).Error
	}
	return r.db.Create(&model.UserFcmToken{UserID: userID, FcmToken: fcmToken}).Error
}

func (r *fcmTokenRepository) GetTokensByUserID(userID string) ([]string, error) {
	var rows []model.UserFcmToken
	if err := r.db.Where("user_id = ?", userID).Find(&rows).Error; err != nil {
		return nil, err
	}
	tokens := make([]string, 0, len(rows))
	for _, row := range rows {
		if row.FcmToken != "" {
			tokens = append(tokens, row.FcmToken)
		}
	}
	return tokens, nil
}
