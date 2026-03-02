package fcm

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"

	"golang.org/x/oauth2/google"
)

const (
	fcmScope = "https://www.googleapis.com/auth/firebase.messaging"
	fcmURL   = "https://fcm.googleapis.com/v1/projects/%s/messages:send"
)

// Client mengirim FCM data message (high priority) agar notifikasi muncul saat app closed.
type Client struct {
	projectID string
	client    *http.Client
}

// serviceAccountProjectID membaca project_id dari file JSON service account.
func serviceAccountProjectID(path string) (string, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return "", err
	}
	var v struct {
		ProjectID string `json:"project_id"`
	}
	if err := json.Unmarshal(data, &v); err != nil {
		return "", err
	}
	if v.ProjectID == "" {
		return "", fmt.Errorf("project_id not found in %s", path)
	}
	return v.ProjectID, nil
}

// New membuat FCM client. Jika credPath kosong, returns nil (FCM disabled).
func New(ctx context.Context, credPath string) (*Client, error) {
	if credPath == "" {
		return nil, nil
	}
	if err := os.Setenv("GOOGLE_APPLICATION_CREDENTIALS", credPath); err != nil {
		return nil, err
	}
	projectID, err := serviceAccountProjectID(credPath)
	if err != nil {
		return nil, fmt.Errorf("fcm credentials: %w", err)
	}
	client, err := google.DefaultClient(ctx, fcmScope)
	if err != nil {
		return nil, fmt.Errorf("fcm oauth: %w", err)
	}
	return &Client{projectID: projectID, client: client}, nil
}

// Send mengirim data message ke device. Priority high agar notif muncul saat app killed/background.
// Data keys harus string; title/body dipakai untuk notifikasi di Android.
func (c *Client) Send(ctx context.Context, deviceToken string, data map[string]string, title, body string) error {
	if deviceToken == "" {
		return nil
	}
	// FCM v1: https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages
	msg := map[string]interface{}{
		"token": deviceToken,
		"data":  data,
		"android": map[string]interface{}{
			"priority": "high",
		},
	}
	if title != "" || body != "" {
		msg["notification"] = map[string]interface{}{"title": title, "body": body}
	}
	payload := map[string]interface{}{"message": msg}
	bodyBytes, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	url := fmt.Sprintf(fcmURL, c.projectID)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(bodyBytes))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := c.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		var buf bytes.Buffer
		buf.ReadFrom(resp.Body)
		log.Printf("[fcm] send failed status=%d body=%s", resp.StatusCode, buf.String())
		return fmt.Errorf("fcm send: status %d", resp.StatusCode)
	}
	return nil
}
