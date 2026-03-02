# Memperbaiki Error: "R.jar - The process cannot access the file"

Error ini terjadi karena **proses lain masih mengunci** file di folder `app/build` (biasanya Gradle daemon lama, IDE, atau OneDrive sync).

## Langkah perbaikan (lakukan berurutan)

### 1. Tutup Cursor / Android Studio
Tutup aplikasi yang membuka project ini agar tidak mengunci file build.

### 2. Hentikan Gradle daemon
Di terminal (PowerShell atau CMD), dari folder **mobile**:
```bat
cd c:\Users\afriz\OneDrive\Desktop\mobile-notif-kotlin\mobile
gradlew.bat --stop
```

### 3. Hapus folder build secara manual
- Buka File Explorer, masuk ke:
  `c:\Users\afriz\OneDrive\Desktop\mobile-notif-kotlin\mobile\app\`
- **Hapus folder `build`** (klik kanan → Delete).
- Jika muncul "file in use", tutup semua program yang mungkin memakai project (Terminal, IDE, dsb.) lalu coba lagi. Atau restart PC lalu hapus folder `build` sebelum membuka project.

### 4. (Opsional) Jeda OneDrive
Project ada di OneDrive. Sync OneDrive bisa mengunci file. Coba:
- Klik ikon OneDrive di system tray → Pause syncing (1–2 jam), lalu hapus folder `app\build` dan jalankan build lagi.

### 5. Build lagi
Buka project di Cursor/Android Studio, lalu jalankan build (Run atau `.\gradlew.bat assembleDebug`).

---

**Ringkas:** Tutup IDE → `gradlew --stop` → hapus folder `app\build` → buka lagi project → build.
