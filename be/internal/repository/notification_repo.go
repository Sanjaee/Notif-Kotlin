package repository

import (
	"yourapp/internal/model"

	"gorm.io/gorm"
)

type NotificationRepository interface {
	Create(n *model.Notification) error
	FindByID(id string) (*model.Notification, error)
	ListByUserID(userID string, limit, offset int) ([]*model.Notification, error)
	MarkReadByUserID(userID string) error
	MarkReadByMessageID(messageID string) error
	MarkReadByID(id string) error
	MarkReadByUserIDAndConversationID(userID, conversationID string) error
	CountUnreadByUserID(userID string) (int64, error)
}

type notificationRepository struct {
	db *gorm.DB
}

func NewNotificationRepository(db *gorm.DB) NotificationRepository {
	return &notificationRepository{db: db}
}

func (r *notificationRepository) Create(n *model.Notification) error {
	return r.db.Create(n).Error
}

func (r *notificationRepository) FindByID(id string) (*model.Notification, error) {
	var n model.Notification
	if err := r.db.Preload("Message").First(&n, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &n, nil
}

func (r *notificationRepository) ListByUserID(userID string, limit, offset int) ([]*model.Notification, error) {
	if limit <= 0 {
		limit = 30
	}
	var list []*model.Notification
	if err := r.db.Where("user_id = ?", userID).
		Preload("Message").
		Order("created_at DESC").
		Limit(limit).Offset(offset).
		Find(&list).Error; err != nil {
		return nil, err
	}
	return list, nil
}

func (r *notificationRepository) MarkReadByUserID(userID string) error {
	return r.db.Model(&model.Notification{}).Where("user_id = ?", userID).Update("is_read", true).Error
}

func (r *notificationRepository) MarkReadByMessageID(messageID string) error {
	return r.db.Model(&model.Notification{}).Where("message_id = ?", messageID).Update("is_read", true).Error
}

func (r *notificationRepository) MarkReadByID(id string) error {
	return r.db.Model(&model.Notification{}).Where("id = ?", id).Update("is_read", true).Error
}

func (r *notificationRepository) MarkReadByUserIDAndConversationID(userID, conversationID string) error {
	return r.db.Exec(
		"UPDATE notifications SET is_read = true WHERE user_id = ? AND message_id IN (SELECT id FROM messages WHERE conversation_id = ? AND deleted_at IS NULL)",
		userID, conversationID,
	).Error
}

func (r *notificationRepository) CountUnreadByUserID(userID string) (int64, error) {
	var n int64
	err := r.db.Model(&model.Notification{}).Where("user_id = ? AND is_read = ?", userID, false).Count(&n).Error
	return n, err
}
