package generator;

import config.QuizConfig;
import model.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiBasedGenerator implements QuestionGenerator {

    private static final String API_URL = 
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent";
    
    private List<String> apiKeys;



public ApiBasedGenerator(String apiKeyTunggal) {
    // Constructor lama tetap didukung untuk kompatibilitas
    this.apiKeys = new ArrayList<>();
    if (apiKeyTunggal != null && !apiKeyTunggal.isEmpty()) {
        this.apiKeys.add(apiKeyTunggal);
    }
}

public ApiBasedGenerator(List<String> apiKeys) {
    this.apiKeys = apiKeys;
}

    @Override
public List<Question> generate(String teks, QuizConfig config) {
    if (apiKeys == null || apiKeys.isEmpty()) {
        System.out.println("Tidak ada API key tersedia. Fallback ke RuleBasedGenerator...");
        return new RuleBasedGenerator().generate(teks, config);
    }

    String prompt = buatPrompt(teks, config);

    for (int i = 0; i < apiKeys.size(); i++) {
        String key = apiKeys.get(i);
        try {
            System.out.println("Mencoba API key #" + (i + 1) + "...");
            String response = panggilAPI(prompt, key);
            List<Question> hasil = parseResponse(response);
            System.out.println("Berhasil menggunakan API key #" + (i + 1));
            return hasil;
         } catch (Exception e) {
    System.out.println("=== ERROR LENGKAP key #" + (i + 1) + " ===");
    System.out.println(e.getMessage());
    System.out.println("================================");
    continue;
        }
    }

    System.out.println("Semua API key gagal. Fallback ke RuleBasedGenerator...");
    return new RuleBasedGenerator().generate(teks, config);
}

    
     private String buatPrompt(String teks, QuizConfig config) {
    int kesulitan = config.getTingkatKesulitan();
    String deskripsiLevel = switch (kesulitan) {
        case 1 -> "Level 1 (Mudah): Soal tentang fakta langsung, definisi dasar, istilah yang sering muncul. Jawaban tersurat jelas di teks.";
        case 2 -> "Level 2 (Sedang): Soal tentang hubungan antar konsep, penerapan definisi. Butuh pemahaman, bukan sekadar hafalan.";
        default -> "Level 3 (Sulit): Soal tentang konsep abstrak, istilah teknis khusus, perbedaan halus antar konsep. Distraktor pilihan ganda dibuat semirip mungkin dengan jawaban benar.";
    };

    return "Kamu adalah generator soal quiz profesional untuk materi akademik.\n\n" +
        "INSTRUKSI WAJIB (HARUS DIIKUTI):\n" +
        "1. Buat soal HANYA dari konsep, definisi, fakta, dan penjelasan akademik dalam teks.\n" +
        "2. ABAIKAN SEPENUHNYA: alamat email, URL, nama jurnal, volume/nomor penerbitan, " +
        "tahun publikasi, nama penulis, nomor halaman, DOI, ISSN, copyright notice, " +
        "referensi/daftar pustaka, dan semua metadata penerbitan.\n" +
        "3. Soal harus dapat dipahami tanpa melihat teks asli — hindari pertanyaan yang " +
        "merujuk 'menurut teks...' atau 'pada paragraf...'.\n" +
        "4. Gunakan bahasa yang sama dengan teks (Indonesia -> soal Indonesia, Inggris -> soal Inggris).\n" +
        "5. Jika teks tidak cukup mengandung konten akademik untuk dibuat soal (misalnya hanya " +
        "berisi template formatting, instruksi penulisan, atau metadata), kembalikan teks kosong saja " +
        "tanpa membuat soal sama sekali.\n\n" +
        "TINGKAT KESULITAN: " + deskripsiLevel + "\n\n" +
        "TEKS MATERI:\n" + teks + "\n\n" +
        "BUAT SOAL BERIKUT (satu per baris, format PERSIS seperti contoh):\n" +
        "- " + config.getJumlahFillInBlank() + " soal ISIAN\n" +
        "- " + config.getJumlahTrueFalse() + " soal BENAR_SALAH\n" +
        "- " + config.getJumlahMultipleChoice() + " soal PILIHAN_GANDA\n" +
        "- " + config.getJumlahShortAnswer() + " soal ESAI\n\n" +
        "FORMAT WAJIB:\n" +
        "ISIAN|kalimat dengan _____ sebagai jawaban|jawaban\n" +
        "BENAR_SALAH|pernyataan|Benar\n" +
        "PILIHAN_GANDA|pertanyaan|A) pilihan1|B) pilihan2|C) pilihan3|D) pilihan4|A)\n" +
        "ESAI|pertanyaan esai|kata kunci jawaban\n\n" +
        "PENTING: Tulis soal saja tanpa penjelasan tambahan! Jangan tulis apapun selain baris soal.";
}

    private String panggilAPI(String prompt, String key) throws Exception {
    String urlStr = API_URL + "?key=" + key;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        String body = "{"
            + "\"contents\": [{"
            + "\"parts\": [{\"text\": \"" + escapeJson(prompt) + "\"}]"
            + "}]"
            + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        if (code != 200) {
    System.out.println("=== DEBUG: Body request yang dikirim (300 karakter pertama) ===");
    System.out.println(body.substring(0, Math.min(300, body.length())));
    throw new Exception("HTTP " + code + ": " + sb.toString());
}

        return ekstrakTeks(sb.toString());
    }

    private String ekstrakTeks(String json) {
    int start = json.indexOf("\"text\"");
    if (start == -1) return "";
    
    // Cari posisi tanda kutip pembuka nilai (setelah "text" lalu : lalu spasi opsional lalu ")
    int colonPos = json.indexOf(":", start);
    if (colonPos == -1) return "";
    
    int quoteStart = json.indexOf("\"", colonPos);
    if (quoteStart == -1) return "";
    
    start = quoteStart + 1;
    int end = json.indexOf("\"", start);
    while (end != -1 && json.charAt(end - 1) == '\\') {
        end = json.indexOf("\"", end + 1);
    }
    if (end == -1) return "";

    return json.substring(start, end)
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\");
}

    private List<Question> parseResponse(String teks) {
        List<Question> hasil = new ArrayList<>();
        String[] baris = teks.split("\n");

        for (String b : baris) {
            b = b.trim();
            if (b.isEmpty()) continue;

            
            // Post-processing filter: buang baris yang masih mengandung metadata

            String bLower = b.toLowerCase();
        if (bLower.contains("@") || bLower.contains("http") ||
            bLower.contains("doi") || bLower.contains("issn") ||
            bLower.contains("vol.") || bLower.contains("no.") ||
            bLower.contains("pp.")) {
            System.out.println("Skip soal mengandung metadata: " + b);
            continue;
        }

            try {
                String[] bagian = b.split("\\|");
                if (bagian.length < 3) continue;
                String tipe = bagian[0].trim().toUpperCase();

                switch (tipe) {
                    case "ISIAN":
                        hasil.add(new FillInBlankQuestion(
                            bagian[1].trim(), bagian[2].trim()
                        ));
                        break;

                    case "BENAR_SALAH":
                        boolean jwb = bagian[2].trim().equalsIgnoreCase("Benar");
                        hasil.add(new TrueFalseQuestion(bagian[1].trim(), jwb));
                        break;

                    case "PILIHAN_GANDA":
                        if (bagian.length >= 7) {
                            List<String> pilihan = Arrays.asList(
                                bagian[2].replaceFirst("^A\\)", "").trim(),
                                bagian[3].replaceFirst("^B\\)", "").trim(),
                                bagian[4].replaceFirst("^C\\)", "").trim(),
                                bagian[5].replaceFirst("^D\\)", "").trim()
                            );
                            String jwbMC = bagian[6].trim();
                            int idx = jwbMC.startsWith("A") ? 0
                                    : jwbMC.startsWith("B") ? 1
                                    : jwbMC.startsWith("C") ? 2 : 3;
                            hasil.add(new MultipleChoiceQuestion(
                                bagian[1].trim(), pilihan, idx
                            ));
                        }
                        break;

                    case "ESAI":
                        hasil.add(new ShortAnswerQuestion(
                            bagian[1].trim(),
                            bagian[2].trim(),
                            "Jawab dengan lengkap"
                        ));
                        break;
                }
            } catch (Exception e) {
                System.out.println("Skip baris: " + b);
            }
        }
        return hasil;
    }

    private String escapeJson(String s) {
    return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("\t", "\\t")
            .replaceAll("[\\x00-\\x1F]", ""); // buang karakter kontrol non-printable
    }
}