package repository

import (
	"yourapp/internal/model"

	"gorm.io/gorm"
)

type ConversationRepository interface {
	Create(conv *model.Conversation) error
	Update(conv *model.Conversation) error
	FindByID(id string) (*model.Conversation, error)
	GetOrCreateDM(userID1, userID2 string) (*model.Conversation, error)
	ListByUserID(userID string, limit, offset int) ([]*model.Conversation, error)
	GetMemberIDs(conversationID string) ([]string, error)
}

type conversationRepository struct {
	db *gorm.DB
}

func NewConversationRepository(db *gorm.DB) ConversationRepository {
	return &conversationRepository{db: db}
}

func (r *conversationRepository) Create(conv *model.Conversation) error {
	return r.db.Create(conv).Error
}

func (r *conversationRepository) Update(conv *model.Conversation) error {
	return r.db.Save(conv).Error
}

func (r *conversationRepository) FindByID(id string) (*model.Conversation, error) {
	var c model.Conversation
	if err := r.db.First(&c, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &c, nil
}

func (r *conversationRepository) GetOrCreateDM(userID1, userID2 string) (*model.Conversation, error) {
	var convID string
	err := r.db.Raw(
		"SELECT c.id FROM conversations c INNER JOIN conversation_members m ON m.conversation_id = c.id AND m.deleted_at IS NULL WHERE c.deleted_at IS NULL AND m.user_id IN (?, ?) GROUP BY c.id HAVING COUNT(DISTINCT m.user_id) = 2 LIMIT 1",
		userID1, userID2,
	).Scan(&convID).Error
	if err == nil && convID != "" {
		return r.FindByID(convID)
	}

	conv := &model.Conversation{}
	if err := r.db.Create(conv).Error; err != nil {
		return nil, err
	}
	m1 := &model.ConversationMember{ConversationID: conv.ID, UserID: userID1}
	m2 := &model.ConversationMember{ConversationID: conv.ID, UserID: userID2}
	if err := r.db.Create(m1).Error; err != nil {
		return nil, err
	}
	if err := r.db.Create(m2).Error; err != nil {
		return nil, err
	}
	return conv, nil
}

func (r *conversationRepository) ListByUserID(userID string, limit, offset int) ([]*model.Conversation, error) {
	if limit <= 0 {
		limit = 20
	}
	var convIDs []string
	if err := r.db.Model(&model.ConversationMember{}).
		Where("user_id = ?", userID).
		Pluck("conversation_id", &convIDs).Error; err != nil {
		return nil, err
	}
	if len(convIDs) == 0 {
		return []*model.Conversation{}, nil
	}
	var list []*model.Conversation
	if err := r.db.Where("id IN ?", convIDs).
		Order("updated_at DESC").
		Limit(limit).Offset(offset).
		Find(&list).Error; err != nil {
		return nil, err
	}
	return list, nil
}

func (r *conversationRepository) GetMemberIDs(conversationID string) ([]string, error) {
	var ids []string
	err := r.db.Model(&model.ConversationMember{}).
		Where("conversation_id = ?", conversationID).
		Pluck("user_id", &ids).Error
	return ids, err
}
