package util

import (
	"context"
	"log"
	"sync"
)

// EmailMessage represents an email job (same contract as before, no RabbitMQ).
type EmailMessage struct {
	To      string `json:"to"`
	Subject string `json:"subject"`
	Body    string `json:"body"`
	Type    string `json:"type"` // "otp", "reset_password", "verification", "welcome"
}

// JobHandler processes an email job. Returns error to log; job is not retried.
type JobHandler func(ctx context.Context, job EmailMessage) error

// WorkerPool runs a fixed number of workers that process jobs from a channel.
// Best practice: buffered channel, context for shutdown, panic recovery in workers.
type WorkerPool struct {
	workers   int
	jobCh     chan EmailMessage
	handler   JobHandler
	wg        sync.WaitGroup
	once      sync.Once
	closed    bool
	closeMu   sync.Mutex
}

// NewWorkerPool creates a new worker pool. bufferSize is the job queue size.
func NewWorkerPool(workers int, bufferSize int, handler JobHandler) *WorkerPool {
	if workers < 1 {
		workers = 1
	}
	if bufferSize < 1 {
		bufferSize = 100
	}
	return &WorkerPool{
		workers: workers,
		jobCh:   make(chan EmailMessage, bufferSize),
		handler: handler,
	}
}

// Start starts the worker goroutines. Call with a context that you cancel on shutdown.
func (p *WorkerPool) Start(ctx context.Context) {
	for i := 0; i < p.workers; i++ {
		p.wg.Add(1)
		workerID := i + 1
		go func() {
			defer p.wg.Done()
			p.runWorker(ctx, workerID)
		}()
	}
	log.Printf("Worker pool started with %d workers", p.workers)
}

func (p *WorkerPool) runWorker(ctx context.Context, workerID int) {
	for {
		select {
		case <-ctx.Done():
			return
		case job, ok := <-p.jobCh:
			if !ok {
				return
			}
			// Recover from panic so one bad job doesn't kill the worker
			func() {
				defer func() {
					if r := recover(); r != nil {
						log.Printf("[worker %d] panic processing job: %v", workerID, r)
					}
				}()
				if err := p.handler(ctx, job); err != nil {
					log.Printf("[worker %d] job error (type=%s to=%s): %v", workerID, job.Type, job.To, err)
				} else {
					log.Printf("[worker %d] job done: type=%s to=%s", workerID, job.Type, job.To)
				}
			}()
		}
	}
}

// Submit enqueues a job. Non-blocking; returns false if pool is closed or queue full.
func (p *WorkerPool) Submit(job EmailMessage) bool {
	p.closeMu.Lock()
	defer p.closeMu.Unlock()
	if p.closed {
		log.Println("Worker pool is closed, job rejected")
		return false
	}
	select {
	case p.jobCh <- job:
		return true
	default:
		// Buffer full - log and drop or you could block. Best practice: don't block HTTP.
		log.Printf("Job queue full, dropping email job to %s (type=%s)", job.To, job.Type)
		return false
	}
}

// SubmitBlocking enqueues a job, blocking until there is space or ctx is done.
func (p *WorkerPool) SubmitBlocking(ctx context.Context, job EmailMessage) bool {
	p.closeMu.Lock()
	if p.closed {
		p.closeMu.Unlock()
		return false
	}
	p.closeMu.Unlock()
	select {
	case <-ctx.Done():
		return false
	case p.jobCh <- job:
		return true
	}
}

// Close stops accepting new jobs and waits for in-flight jobs to finish.
func (p *WorkerPool) Close() {
	p.once.Do(func() {
		p.closeMu.Lock()
		p.closed = true
		p.closeMu.Unlock()
		close(p.jobCh)
		p.wg.Wait()
		log.Println("Worker pool closed")
	})
}
