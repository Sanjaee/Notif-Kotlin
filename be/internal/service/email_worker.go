package service

import (
	"context"
	"yourapp/internal/util"
)

// EmailJobSubmitter submits email jobs (e.g. WorkerPool). Used by AuthService.
type EmailJobSubmitter interface {
	Submit(job util.EmailMessage) bool
}

const (
	defaultWorkerCount = 4
	defaultQueueSize   = 256
)

// EmailJobHandler returns a JobHandler that processes email jobs using EmailService.
func EmailJobHandler(emailService EmailService) util.JobHandler {
	return func(ctx context.Context, job util.EmailMessage) error {
		switch job.Type {
		case "otp":
			return emailService.SendOTPEmail(job.To, job.Body)
		case "reset_password":
			return emailService.SendResetPasswordEmail(job.To, job.Body)
		case "verification":
			return emailService.SendVerificationEmail(job.To, job.Body)
		case "welcome":
			return emailService.SendWelcomeEmail(job.To, job.Subject)
		default:
			return emailService.SendOTPEmail(job.To, job.Body)
		}
	}
}

// StartEmailWorkerPool creates and starts a worker pool for email jobs.
// Call the returned shutdown function (e.g. defer) when the app exits.
func StartEmailWorkerPool(ctx context.Context, emailService EmailService, workers, queueSize int) *util.WorkerPool {
	if workers < 1 {
		workers = defaultWorkerCount
	}
	if queueSize < 1 {
		queueSize = defaultQueueSize
	}
	pool := util.NewWorkerPool(workers, queueSize, EmailJobHandler(emailService))
	pool.Start(ctx)
	return pool
}
