# Zacode Go Backend

Backend server untuk aplikasi Zacode menggunakan Go dengan arsitektur clean architecture.

## Struktur Proyek

```
/yourapp
│
├── cmd/
│   └── server/
│       └── main.go
│
├── internal/
│   ├── config/          # load env, config global
│   │   └── config.go
│   │
│   ├── app/             # HTTP handler + routing (Gin)
│   │   ├── router.go
│   │   ├── auth_handler.go
│   │   ├── chat_handler.go
│   │   └── ...
│   │
│   ├── service/         # business logic / usecase
│   │   ├── auth_service.go
│   │   ├── chat_service.go
│   │   └── ...
│   │
│   ├── repository/      # DB access (gorm / raw SQL)
│   │   ├── user_repo.go
│   │   ├── chat_repo.go
│   │   └── ...
│   │
│   ├── model/           # struct model untuk DB
│   │   ├── user.go
│   │   ├── chat.go
│   │   └── ...
│   │
│   ├── websocket/       # ws hub, manager, client
│   │   ├── hub.go
│   │   ├── client.go
│   │   ├── ws_handler.go
│   │   └── ...
│   │
│   └── util/            # helper: jwt, hash, error, response
│       ├── jwt.go
│       ├── hash.go
│       └── response.go
│
├── pkg/                 # library reusable (optional)
│   └── logger/
│       └── logger.go
│
├── go.mod
├── .env
├── Dockerfile
└── docker-compose.yml
```

## Deskripsi Folder

### `cmd/server/`
Entry point aplikasi. Berisi `main.go` yang menginisialisasi dan menjalankan server.

### `internal/config/`
Konfigurasi aplikasi, termasuk loading environment variables dan setup global config.

### `internal/app/`
Layer HTTP handler dan routing menggunakan Gin framework.
- `router.go`: Setup routing dan middleware
- `*_handler.go`: HTTP handlers untuk setiap endpoint

### `internal/service/`
Business logic layer (use case layer). Berisi logika bisnis aplikasi.

### `internal/repository/`
Data access layer. Interface dan implementasi untuk akses database (GORM atau raw SQL).

### `internal/model/`
Struct model untuk database. Definisi struct yang digunakan untuk mapping database.

### `internal/websocket/`
WebSocket implementation untuk real-time communication.
- `hub.go`: WebSocket hub untuk manage connections
- `client.go`: WebSocket client implementation
- `ws_handler.go`: WebSocket handler

### `internal/util/`
Utility functions dan helpers:
- `jwt.go`: JWT token generation dan validation
- `hash.go`: Password hashing utilities
- `response.go`: Standard response formatter

### `pkg/logger/`
Reusable logger library yang bisa digunakan di seluruh aplikasi.

## Setup

### Prerequisites
- Go 1.21+
- Docker & Docker Compose
- PostgreSQL
- Redis (optional)
- RabbitMQ (optional)

### Installation

1. Clone repository
```bash
git clone <repository-url>
cd /go
```

2. Copy environment file
```bash
cp .env.example .env
```

3. Update `.env` dengan konfigurasi yang sesuai

4. Install dependencies
```bash
go mod download
```

5. Run dengan Docker Compose
```bash
docker-compose up -d
```

6. Atau run secara lokal
```bash
go run cmd/server/main.go
```

## Environment Variables

Buat file `.env` dengan variabel berikut:

```env
# Server
PORT=5000
SERVER_HOST=0.0.0.0
CLIENT_URL=http://localhost:3000

# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=your_database
POSTGRES_SSLMODE=disable

# JWT
JWT_SECRET=your_jwt_secret_key

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=your_user
RABBITMQ_PASSWORD=your_password

# FCM (notifikasi saat app closed / di luar app)
FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json
```

## FCM: Notifikasi di Luar App (saat app closed)

Agar notifikasi chat (dengan Balas & Tandai dibaca) muncul saat app **tidak dibuka** / ditutup:

### 1. Dapatkan Service Account Key (Firebase)

- Firebase Console → **Project settings** (ikon gear) → tab **Service accounts**.
- Klik **Generate new private key** → download file JSON (nama: `project-firebase-adminsdk-xxx.json`).

### 2. Simpan file & env

- **Lokal:** Taruh file JSON di folder `be/`. Di `.env`:
  ```env
  FIREBASE_CREDENTIALS_PATH=./ezastore-firebase-adminsdk-xtqoa-6397c4e281.json
  ```
- **Docker:** Di `docker-compose.yml` sudah ada volume yang mount file JSON ke `/app/firebase-credentials.json`. Pastikan file `ezastore-firebase-adminsdk-xtqoa-6397c4e281.json` ada di folder `be/`, atau sesuaikan nama file di volume.
- **VPS (nano):**
  ```bash
  cd ~/chat/be
  nano ezastore-firebase-adminsdk-xtqoa-6397c4e281.json   # paste isi JSON, Ctrl+O, Ctrl+X
  nano .env   # tambah: FIREBASE_CREDENTIALS_PATH=/app/firebase-credentials.json
  docker compose up -d --build
  ```

### 3. Alur

- App (Android) login → panggil `POST /api/v1/chat/fcm-token` dengan body `{"fcm_token": "..."}`.
- Backend menyimpan token di tabel `user_fcm_tokens` (bisa banyak device per user).
- Saat ada pesan chat baru, backend kirim FCM **data-only** ke token penerima → notifikasi tampil dengan aksi Balas & Tandai dibaca walaupun app closed.

## Development

### Run Development Server
```bash
go run cmd/server/main.go
```

### Build
```bash
go build -o bin/server cmd/server/main.go
```

### Run Tests
```bash
go test ./...
```

## Docker

### Build Image
```bash
docker build -t 
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

## Services & Ports

Setelah menjalankan `docker-compose up -d`, services berikut akan tersedia:

- **API Server**: http://localhost:5000
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **RabbitMQ Management UI**: http://localhost:15672
  - Username: `yourapp` (default)
  - Password: `password123` (default)
- **pgweb (Database UI)**: http://localhost:8081
  - Web-based PostgreSQL client untuk melihat dan mengelola database
  - Otomatis terhubung ke database yang dikonfigurasi

## Architecture

Aplikasi ini menggunakan **Clean Architecture** dengan layer separation:

1. **Handler Layer** (`internal/app/`): HTTP handlers dan routing
2. **Service Layer** (`internal/service/`): Business logic
3. **Repository Layer** (`internal/repository/`): Data access
4. **Model Layer** (`internal/model/`): Domain models

## License

MIT

