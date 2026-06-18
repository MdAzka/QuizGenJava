# Quiz Generator Otomatis

Aplikasi desktop Java (JavaFX) untuk membuat soal quiz otomatis dari teks bacaan atau PDF, menggunakan Rule-Based logic atau Gemini AI.

## Cara Setup
1. Download JavaFX SDK 21 dan PDFBox, taruh di folder `lib/`
2. Copy `config.properties.example` jadi `config.properties`, isi dengan API key Gemini kamu
3. Compile dan jalankan dari folder `src`

## Fitur
- 4 jenis soal: Isian, Benar/Salah, Pilihan Ganda, Esai
- Generator Rule-Based (offline) & Gemini AI (online)
- Upload PDF sebagai sumber materi
- Tingkat kesulitan (Mudah/Sedang/Sulit)
- Ekspor ke TXT, HTML/PDF, JSON
- Riwayat quiz tersimpan