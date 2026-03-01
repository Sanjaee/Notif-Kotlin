package repository

import (
	"yourapp/internal/model"

	"gorm.io/gorm"
)

type MessageRepository interface {
	Create(msg *model.Message) error
	FindByID(id string) (*model.Message, error)
	ListByConversationID(conversationID string, limit, offset int) ([]*model.Message, error)
	MarkReadByConversationAndUser(conversationID, userID string) error
	CountUnreadByConversationAndUser(conversationID, userID string) (int64, error)
}

type messageRepository struct {
	db *gorm.DB
}

func NewMessageRepository(db *gorm.DB) MessageRepository {
	return &messageRepository{db: db}
}

func (r *messageRepository) Create(msg *model.Message) error {
	return r.db.Create(msg).Error
}

func (r *messageRepository) FindByID(id string) (*model.Message, error) {
	var m model.Message
	if err := r.db.Preload("Sender").First(&m, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &m, nil
}

func (r *messageRepository) ListByConversationID(conversationID string, limit, offset int) ([]*model.Message, error) {
	if limit <= 0 {
		limit = 50
	}
	var list []*model.Message
	if err := r.db.Where("conversation_id = ?", conversationID).
		Preload("Sender").
		Order("created_at DESC").
		Limit(limit).Offset(offset).
		Find(&list).Error; err != nil {
		return nil, err
	}
	return list, nil
}

func (r *messageRepository) MarkReadByConversationAndUser(conversationID, userID string) error {
	return r.db.Model(&model.Message{}).
		Where("conversation_id = ? AND sender_id != ?", conversationID, userID).
		Update("is_read", true).Error
}

func (r *messageRepository) CountUnreadByConversationAndUser(conversationID, userID string) (int64, error) {
	var n int64
	err := r.db.Model(&model.Message{}).
		Where("conversation_id = ? AND sender_id != ? AND is_read = ?", conversationID, userID, false).
		Count(&n).Error
	return n, err
}
