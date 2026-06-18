package generator;

import analyzer.TextAnalyzer;
import config.QuizConfig;
import model.*;

import java.util.*;

public class RuleBasedGenerator implements QuestionGenerator {

    @Override
    public List<Question> generate(String teks, QuizConfig config) {
        List<Question> hasilSoal = new ArrayList<>();
        TextAnalyzer analyzer = new TextAnalyzer(teks);

        List<String> kalimat = analyzer.getKalimat();
        List<String> kataKunci = analyzer.getKataKunci();

        // Pastikan ada cukup data
        if (kalimat.isEmpty() || kataKunci.isEmpty()) {
            System.out.println("Teks terlalu pendek!");
            return hasilSoal;
        }

        int kesulitan = config.getTingkatKesulitan();
        hasilSoal.addAll(generateFillInBlank(kalimat, kataKunci, config.getJumlahFillInBlank(), kesulitan));
        hasilSoal.addAll(generateTrueFalse(kalimat, config.getJumlahTrueFalse(), kesulitan));
        hasilSoal.addAll(generateMultipleChoice(kalimat, kataKunci, config.getJumlahMultipleChoice()));
        hasilSoal.addAll(generateShortAnswer(kataKunci, config.getJumlahShortAnswer()));
        return hasilSoal;
        
    }

    // =====================
    // FILL IN THE BLANK
    // =====================
    private List<Question> generateFillInBlank(List<String> kalimat, List<String> kataKunci, int jumlah, int kesulitan) {
        List<Question> hasil = new ArrayList<>();
        int count = 0;

        for (String k : kalimat) {
            if (count >= jumlah) break;
            if (kesulitan >= 2 && k.split("\\s+").length < 8) continue; 
            for (String kata : kataKunci) {
                // Cek apakah kata kunci ada di kalimat ini
                if (k.toLowerCase().contains(kata.toLowerCase())) {
                    // Cari kata asli (dengan huruf besar/kecil aslinya)
                    String kataAsli = cariKataAsli(k, kata);
                    if (kataAsli != null) {
                        hasil.add(new FillInBlankQuestion(k, kataAsli));
                        count++;
                        break;
                    }
                }
            }
        }
        return hasil;
    }

    // =====================
    // TRUE / FALSE
    // =====================
    private List<Question> generateTrueFalse(List<String> kalimat, int jumlah, int kesulitan) {
        List<Question> hasil = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < Math.min(jumlah, kalimat.size()); i++) {
            String k = kalimat.get(i);
            // Mudah: 50% salah | Sedang: 60% salah | Sulit: 70% salah
            int threshold = kesulitan == 1 ? 50 : kesulitan == 2 ? 40 : 30;
            boolean jawabanBenar = random.nextInt(100) >= threshold;

            if (!jawabanBenar) {
                // Modifikasi kalimat agar jadi salah
                k = k + " (TIDAK BENAR)";
            }

            hasil.add(new TrueFalseQuestion(k, jawabanBenar));
        }
        return hasil;
    }

    // =====================
    // MULTIPLE CHOICE
    // =====================
    private List<Question> generateMultipleChoice(List<String> kalimat, List<String> kataKunci, int jumlah) {
        List<Question> hasil = new ArrayList<>();
        Random random = new Random();
        int count = 0;

        for (String k : kalimat) {
            if (count >= jumlah || kataKunci.size() < 4) break;

            for (String kata : kataKunci) {
                if (k.toLowerCase().contains(kata.toLowerCase())) {
                    String kataAsli = cariKataAsli(k, kata);
                    if (kataAsli == null) continue;

                    // Buat pertanyaan
                    String pertanyaan = k.replace(kataAsli, "____?");

                    // Buat 4 pilihan: 1 benar + 3 distractor dari kata kunci lain
                    List<String> distractor = new ArrayList<>();
                    for (String d : kataKunci) {
                        if (!d.equalsIgnoreCase(kata) && distractor.size() < 3) {
                            distractor.add(d);
                        }
                    }

                    if (distractor.size() < 3) continue;

                    // Taruh jawaban benar di posisi random
                    int posisiBenar = random.nextInt(4);
                    List<String> pilihan = new ArrayList<>(distractor);
                    pilihan.add(posisiBenar, kataAsli);

                    hasil.add(new MultipleChoiceQuestion(pertanyaan, pilihan, posisiBenar));
                    count++;
                    break;
                }
            }
        }
        return hasil;
    }

    // =====================
    // SHORT ANSWER
    // =====================
    private List<Question> generateShortAnswer(List<String> kataKunci, int jumlah) {
        List<Question> hasil = new ArrayList<>();
        String[] templatePertanyaan = {
            "Jelaskan apa yang dimaksud dengan %s!",
            "Apa yang kamu ketahui tentang %s?",
            "Bagaimana peran %s dalam konteks ini?",
            "Mengapa %s penting untuk dipahami?"
        };

        Random random = new Random();
        for (int i = 0; i < Math.min(jumlah, kataKunci.size()); i++) {
            String kata = kataKunci.get(i);
            String template = templatePertanyaan[random.nextInt(templatePertanyaan.length)];
            String pertanyaan = String.format(template, kata);
            hasil.add(new ShortAnswerQuestion(pertanyaan, kata, "Berikan penjelasan tentang " + kata));
        }
        return hasil;
    }

    // =====================
    // HELPER
    // =====================
    private String cariKataAsli(String kalimat, String kata) {
        String[] kata2 = kalimat.split("\\s+");
        for (String k : kata2) {
            String bersih = k.replaceAll("[^a-zA-Z0-9]", "");
            if (bersih.equalsIgnoreCase(kata)) {
                return bersih;
            }
        }
        return null;
    }
}