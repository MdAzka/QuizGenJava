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
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    private String apiKey;

    public ApiBasedGenerator(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Question> generate(String teks, QuizConfig config) {
        try {
            String prompt = buatPrompt(teks, config);
            String response = panggilAPI(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            System.out.println("Gemini API Error: " + e.getMessage());
            System.out.println("Fallback ke RuleBasedGenerator...");
            return new RuleBasedGenerator().generate(teks, config);
        }
    }

    private String buatPrompt(String teks, QuizConfig config) {
        return "Kamu adalah generator soal quiz. Buat soal dari teks berikut.\n\n" +
            "TEKS:\n" + teks + "\n\n" +
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
            "PENTING: Tulis soal saja tanpa penjelasan tambahan!";
    }

    private String panggilAPI(String prompt) throws Exception {
        String urlStr = API_URL + "?key=" + apiKey;
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
            throw new Exception("HTTP " + code + ": " + sb.toString());
        }

        return ekstrakTeks(sb.toString());
    }

    private String ekstrakTeks(String json) {
        String cari = "\"text\": \"";
        int start = json.indexOf(cari);
        if (start == -1) return "";
        start += cari.length();
        int end = json.indexOf("\"", start);
        // Cari closing quote yang bukan escaped
        while (end != -1 && json.charAt(end - 1) == '\\') {
            end = json.indexOf("\"", end + 1);
        }
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
                .replace("\r", "");
    }
}