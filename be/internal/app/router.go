package app

import (
	"context"
	"log"
	"yourapp/internal/config"
	"yourapp/internal/fcm"
	"yourapp/internal/middleware"
	"yourapp/internal/model"
	"yourapp/internal/repository"
	"yourapp/internal/service"
	"yourapp/internal/ws"

	"github.com/gin-gonic/gin"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func NewRouter(cfg *config.Config) *gin.Engine {
	// Set Gin mode
	if cfg.ServerPort == "5000" {
		gin.SetMode(gin.DebugMode)
	} else {
		gin.SetMode(gin.ReleaseMode)
	}

	r := gin.Default()

	// CORS middleware
	r.Use(corsMiddleware(cfg.ClientURL))

	// Rate limiting middleware (if enabled)
	if cfg.RateLimitEnabled {
		rateLimiter := middleware.NewRateLimiter(cfg.RateLimitRPS, cfg.RateLimitBurst)
		r.Use(rateLimiter.Middleware())
		log.Printf("Rate limiting enabled: %d req/sec, burst: %d", cfg.RateLimitRPS, cfg.RateLimitBurst)
	}

	// Initialize database
	db, err := initDB(cfg)
	if err != nil {
		panic("Failed to connect to database: " + err.Error())
	}

	// Auto migrate
	if err := db.AutoMigrate(
		&model.User{},
		&model.Conversation{},
		&model.ConversationMember{},
		&model.Message{},
		&model.Notification{},
		&model.UserFcmToken{},
	); err != nil {
		panic("Failed to migrate database: " + err.Error())
	}

	// Initialize repositories
	userRepo := repository.NewUserRepository(db)
	convRepo := repository.NewConversationRepository(db)
	msgRepo := repository.NewMessageRepository(db)
	notifRepo := repository.NewNotificationRepository(db)
	fcmTokenRepo := repository.NewFcmTokenRepository(db)

	// Initialize email service and worker pool (goroutine-based, no RabbitMQ)
	emailService := service.NewEmailService(cfg)
	ctx := context.Background()
	emailPool := service.StartEmailWorkerPool(ctx, emailService, 4, 256)

	// Initialize services (auth uses email pool for async email jobs)
	authService := service.NewAuthService(userRepo, cfg.JWTSecret, emailPool)
	chatService := service.NewChatService(userRepo, convRepo, msgRepo, notifRepo, fcmTokenRepo)

	// WebSocket hub for chat realtime
	hub := ws.NewHub()
	go hub.Run()

	// FCM client (opsional: agar notifikasi muncul saat app closed)
	var fcmClient *fcm.Client
	if cfg.FirebaseCredentialsPath == "" {
		log.Printf("FCM disabled: set FIREBASE_CREDENTIALS_PATH di .env (path ke file JSON Firebase service account) agar notif muncul saat app closed")
	} else {
		var err error
		fcmClient, err = fcm.New(context.Background(), cfg.FirebaseCredentialsPath)
		if err != nil {
			log.Printf("FCM init failed (notifications when app closed will be disabled): %v", err)
		} else {
			log.Printf("FCM enabled: push notifications when app is closed")
		}
	}

	// Initialize handlers
	authHandler := NewAuthHandler(authService, cfg.JWTSecret)
	chatHandler := NewChatHandler(chatService, cfg.JWTSecret, hub, fcmClient)

	// API routes
	api := r.Group("/api/v1")
	{
		// Auth routes
		auth := api.Group("/auth")
		{
			auth.POST("/register", authHandler.Register)
			auth.POST("/login", authHandler.Login)
			auth.POST("/verify-otp", authHandler.VerifyOTP)
			auth.POST("/resend-otp", authHandler.ResendOTP)
			auth.POST("/google-oauth", authHandler.GoogleOAuth)
			auth.POST("/refresh-token", authHandler.RefreshToken)
			auth.POST("/forgot-password", authHandler.RequestResetPassword)
			auth.POST("/verify-reset-password", authHandler.VerifyResetPassword)
			auth.POST("/reset-password", authHandler.ResetPassword)
			auth.POST("/verify-email", authHandler.VerifyEmail)

			// Protected routes
			auth.GET("/me", authHandler.AuthMiddleware(), authHandler.GetMe)
		}

		// Chat routes (protected)
		chat := api.Group("/chat")
		chat.Use(authHandler.AuthMiddleware())
		{
			chat.GET("/users", chatHandler.ListUsers)
			chat.POST("/conversations", chatHandler.GetOrCreateConversation)
			chat.GET("/conversations", chatHandler.ListConversations)
			chat.GET("/conversations/:id/messages", chatHandler.GetMessages)
			chat.POST("/conversations/:id/messages", chatHandler.SendMessage)
			chat.POST("/conversations/:id/read", chatHandler.MarkConversationRead)
			chat.GET("/notifications", chatHandler.ListNotifications)
			chat.POST("/notifications/:id/read", chatHandler.MarkNotificationRead)
			chat.GET("/notifications/unread-count", chatHandler.UnreadCount)
			chat.POST("/fcm-token", chatHandler.RegisterFCMToken)
		}

		// WebSocket (token in query or Authorization header)
		api.GET("/ws/chat", chatHandler.HandleWebSocket)
	}

	// Health check
	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{"status": "ok"})
	})

	return r
}

func initDB(cfg *config.Config) (*gorm.DB, error) {
	dsn := cfg.DatabaseURL
	if dsn == "" {
		dsn = "host=" + cfg.PostgresHost +
			" port=" + cfg.PostgresPort +
			" user=" + cfg.PostgresUser +
			" password=" + cfg.PostgresPassword +
			" dbname=" + cfg.PostgresDB +
			" sslmode=" + cfg.PostgresSSLMode
	}

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		return nil, err
	}

	return db, nil
}

func corsMiddleware(clientURL string) gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", clientURL)
		c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT, DELETE, PATCH")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}

		c.Next()
	}
}
