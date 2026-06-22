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
    Map<String, Integer> frekuensi = analyzer.getFrekuensiKata();
    Map<String, Double> skorKesulitan = analyzer.hitungSkorKesulitan();

    if (kalimat.isEmpty() || kataKunci.isEmpty()) {
        System.out.println("Teks terlalu pendek!");
        return hasilSoal;
    }

    int kesulitan = config.getTingkatKesulitan();

    // Filter kata kunci sesuai rentang skor level kesulitan
    List<String> kataKunciLevel = filterKataKunciByLevel(kataKunci, skorKesulitan, kesulitan);

    // Kalau hasil filter kosong (teks terlalu sedikit variasi), fallback pakai semua kata kunci
    if (kataKunciLevel.isEmpty()) kataKunciLevel = kataKunci;

    hasilSoal.addAll(generateFillInBlank(kalimat, kataKunciLevel, frekuensi, config.getJumlahFillInBlank(), kesulitan));
    hasilSoal.addAll(generateTrueFalse(kalimat, config.getJumlahTrueFalse(), kesulitan));
    hasilSoal.addAll(generateMultipleChoice(kalimat, kataKunciLevel, skorKesulitan, config.getJumlahMultipleChoice()));
    hasilSoal.addAll(generateShortAnswer(kataKunciLevel, config.getJumlahShortAnswer()));

    return hasilSoal;
}

// Filter kata kunci berdasarkan rentang skor sesuai level
private List<String> filterKataKunciByLevel(List<String> kataKunci, Map<String, Double> skor, int level) {
    List<String> hasil = new ArrayList<>();
    for (String kata : kataKunci) {
        double s = skor.getOrDefault(kata, 0.5);
        boolean masuk = switch (level) {
            case 1 -> s < 0.35;
            case 2 -> s >= 0.35 && s <= 0.65;
            default -> s > 0.65;
        };
        if (masuk) hasil.add(kata);
    }
    return hasil;
}

    // =====================
    // FILL IN THE BLANK
    // =====================
    private List<Question> generateFillInBlank(List<String> kalimat, List<String> kataKunci,
        Map<String, Integer> frekuensi, int jumlah, int kesulitan) {
    List<Question> hasil = new ArrayList<>();
    int count = 0;

    // Tentukan syarat minimal/maksimal kemunculan kata sesuai level
    // Level 1: kata familiar (muncul >= 3x). Level 2: muncul 2x. Level 3: muncul 1x (jarang/spesifik)
    for (String k : kalimat) {
        if (count >= jumlah) break;
        for (String kata : kataKunci) {
            if (k.toLowerCase().contains(kata.toLowerCase())) {
                int frek = frekuensi.getOrDefault(kata, 1);
                boolean cocokLevel = switch (kesulitan) {
                    case 1 -> frek >= 3;
                    case 2 -> frek == 2;
                    default -> frek == 1;
                };
                if (!cocokLevel) continue;

                String kataAsli = cariKataAsli(k, kata);
                if (kataAsli != null) {
                    hasil.add(new FillInBlankQuestion(k, kataAsli));
                    count++;
                    break;
                }
            }
        }
    }

    // Fallback: kalau tidak cukup soal ditemukan sesuai syarat ketat, isi sisanya tanpa syarat frekuensi
    if (count < jumlah) {
        for (String k : kalimat) {
            if (count >= jumlah) break;
            for (String kata : kataKunci) {
                if (k.toLowerCase().contains(kata.toLowerCase())) {
                    String kataAsli = cariKataAsli(k, kata);
                    if (kataAsli != null && !sudahDipakai(hasil, kataAsli)) {
                        hasil.add(new FillInBlankQuestion(k, kataAsli));
                        count++;
                        break;
                    }
                }
            }
        }
    }
    return hasil;
}

private boolean sudahDipakai(List<Question> list, String kataAsli) {
    for (Question q : list) {
        if (q.getKunciJawaban().equalsIgnoreCase(kataAsli)) return true;
    }
    return false;
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
    private List<Question> generateMultipleChoice(List<String> kalimat, List<String> kataKunci,
        Map<String, Double> skorKesulitan, int jumlah) {
    List<Question> hasil = new ArrayList<>();
    Random random = new Random();
    int count = 0;

    for (String k : kalimat) {
        if (count >= jumlah || kataKunci.size() < 4) break;

        for (String kata : kataKunci) {
            if (k.toLowerCase().contains(kata.toLowerCase())) {
                String kataAsli = cariKataAsli(k, kata);
                if (kataAsli == null) continue;

                String pertanyaan = k.replace(kataAsli, "____?");

                // Pilih distractor: yang skornya paling dekat dengan kata jawaban (lebih sulit dibedakan)
                double skorJawaban = skorKesulitan.getOrDefault(kata, 0.5);
                List<String> kandidat = new ArrayList<>();
                for (String d : kataKunci) {
                    if (!d.equalsIgnoreCase(kata)) kandidat.add(d);
                }
                kandidat.sort((a, b) -> {
                    double diffA = Math.abs(skorKesulitan.getOrDefault(a, 0.5) - skorJawaban);
                    double diffB = Math.abs(skorKesulitan.getOrDefault(b, 0.5) - skorJawaban);
                    return Double.compare(diffA, diffB);
                });

                if (kandidat.size() < 3) continue;
                List<String> distractor = kandidat.subList(0, 3);

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