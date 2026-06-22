package analyzer;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TextAnalyzer {

    private String teks;
    private List<String> kalimat;
    private List<String> kataKunci;
    private Map<String, Integer> frekuensiKata; // simpan frekuensi untuk dipakai scoring nanti
    

    public TextAnalyzer(String teks) {
        this.teks = teks;
        this.kalimat = new ArrayList<>();
        this.kataKunci = new ArrayList<>();
        analisis();
    }

    private String bersihkanTeks(String input) {
    String hasil = input;

    // 1. Hapus email
    hasil = hasil.replaceAll("\\S+@\\S+\\.\\S+", " ");

    // 2. Hapus URL
    hasil = hasil.replaceAll("https?://\\S+", " ");
    hasil = hasil.replaceAll("www\\.\\S+", " ");

    // 3. Hapus sitasi
    hasil = hasil.replaceAll("\\[\\d+(,\\s*\\d+)*\\]", " ");
    hasil = hasil.replaceAll("\\([A-Za-z]+,?\\s*\\d{4}\\)", " ");
    hasil = hasil.replaceAll("(?i)\\bet al\\.?", " ");

    String[] baris = hasil.split("\n");
    StringBuilder sb = new StringBuilder();
    boolean dalamBlokAfiliasi = false;

    for (String b : baris) {
        String trimmed = b.trim();
        if (trimmed.isEmpty()) continue;

        // Nomor halaman murni
        if (trimmed.matches("^\\d+$")) continue;
        if (trimmed.matches("(?i)^(page|halaman)\\s+\\d+.*")) continue;

        // Metadata jurnal eksplisit (Vol, No, ISSN, dst)
        boolean adaMetadataJurnal = trimmed.matches(
            "(?i).*\\b(vol\\.?|no\\.?|issn|doi|copyright|journal of|proceedings|©)\\b.*"
        );
        if (adaMetadataJurnal) continue;

        // Deteksi PEMICU mulai blok afiliasi: baris berisi "Department of",
        // "Faculty of", "University", "Universitas", atau nama kota + negara
        boolean pemicuAfiliasi = trimmed.matches(
            "(?i).*\\b(department of|faculty of|universit(y|as)|institute of|" +
            "school of|college of)\\b.*"
        );

        // Baris yang HANYA berisi "Kota, Negara" (contoh: "Semarang, Indonesia")
        boolean polaKotaNegara = trimmed.matches("^[A-Z][a-zA-Z]+,\\s*[A-Z][a-zA-Z]+$");

        // Baris yang terlihat seperti nama orang (2-4 kata, semua diawali huruf besar,
        // tidak ada kata kerja umum, sering diikuti titik di akhir untuk inisial)
        boolean polaNamaOrang = trimmed.matches(
            "^([A-Z][a-zA-Z'\\-]*\\.?\\s*){2,4}$"
        ) && trimmed.split("\\s+").length <= 4 && !trimmed.contains(",");

        if (pemicuAfiliasi) {
            dalamBlokAfiliasi = true;
            continue;
        }
        if (polaKotaNegara) {
            dalamBlokAfiliasi = false; // kota+negara biasanya penutup blok afiliasi
            continue;
        }
        if (dalamBlokAfiliasi) {
            // Masih dalam blok afiliasi (misal baris "Semarang, Indonesia" sendirian,
            // atau baris nama berikutnya) — skip
            continue;
        }
        if (polaNamaOrang) {
            continue;
        }

        // Baris dominan simbol/noise OCR
        String tanpaSimbol = trimmed.replaceAll("[a-zA-Z0-9\\s]", "");
        if ((double) tanpaSimbol.length() / trimmed.length() > 0.5) continue;

        sb.append(trimmed).append(" ");
    }

    return sb.toString().trim();

    
}

    // Memecah teks menjadi kalimat-kalimat
    private void analisis() {
    this.teks = bersihkanTeks(this.teks); // bersihkan dulu sebelum diproses

    String[] pecahan = teks.split("[.!?]+");
    for (String k : pecahan) {
        String bersih = k.trim();
        if (bersih.length() > 10) {
            kalimat.add(bersih);
        }
    }
    ekstrakKataKunci();
    }

    private String deteksiBahasa(String teks) {
    String lower = teks.toLowerCase();
    String[] kataBahasaId = {"yang", "dan", "di", "ini", "itu", "dengan"};
    String[] kataBahasaEn = {"the", "is", "are", "of", "and", "in"};

    int skorId = 0, skorEn = 0;
    for (String k : kataBahasaId) if (lower.contains(" " + k + " ")) skorId++;
    for (String k : kataBahasaEn) if (lower.contains(" " + k + " ")) skorEn++;

    if (skorId > skorEn) return "id";
    if (skorEn > skorId) return "en";
    return "both"; // campuran
}

private Set<String> loadStopwords(String teks) {
    Set<String> stopwords = new HashSet<>();

    // Selalu load keduanya (teks campuran ID+EN sangat umum di jurnal Indonesia)
    stopwords.addAll(bacaFileStopwords("../resources/stopwords_id.txt"));
    stopwords.addAll(bacaFileStopwords("../resources/stopwords_en.txt"));

    // Fallback minimal kalau file tidak ditemukan
    if (stopwords.isEmpty()) {
        stopwords.addAll(Arrays.asList(
            "yang", "dan", "di", "ke", "dari", "ini", "itu",
            "adalah", "dengan", "untuk", "pada", "dalam", "atau",
            "the", "is", "a", "an", "of", "in", "to", "and", "for"
        ));
        System.out.println("Stopwords file tidak ditemukan, menggunakan daftar bawaan.");
    }

    return stopwords;
}

private Set<String> bacaFileStopwords(String path) {
    Set<String> hasil = new HashSet<>();
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        String line;
        while ((line = br.readLine()) != null) {
            String kata = line.trim().toLowerCase();
            if (!kata.isEmpty() && !kata.startsWith("#")) {
                hasil.add(kata);
            }
        }
        System.out.println("Stopwords dimuat dari: " + path + " (" + hasil.size() + " kata)");
    } catch (IOException e) {
        System.out.println("Tidak bisa baca stopwords dari: " + path);
    }
    return hasil;
}

    // Ekstrak kata kunci berdasarkan frekuensi
    private void ekstrakKataKunci() {
    Set<String> stopwords = loadStopwords(teks); 
    

    frekuensiKata = new HashMap<>();
    String[] semuaKata = teks.toLowerCase().split("\\s+");

    for (String kata : semuaKata) {
        kata = kata.replaceAll("[^a-zA-Z0-9]", "");
        if (kata.length() > 3 && !stopwords.contains(kata)) {
            frekuensiKata.put(kata, frekuensiKata.getOrDefault(kata, 0) + 1);
        }
    }

    List<Map.Entry<String, Integer>> urutan = new ArrayList<>(frekuensiKata.entrySet());
    urutan.sort((a, b) -> b.getValue() - a.getValue());

    for (int i = 0; i < Math.min(30, urutan.size()); i++) {
        kataKunci.add(urutan.get(i).getKey());
    }
}

public Map<String, Double> hitungSkorKesulitan() {
    Map<String, Double> skor = new HashMap<>();
    if (frekuensiKata.isEmpty() || kataKunci.isEmpty()) return skor;

    int maxFrekuensi = frekuensiKata.values().stream()
        .max(Integer::compareTo).orElse(1);

    for (String kata : kataKunci) {
        int panjang = kata.length();
        int frek = frekuensiKata.getOrDefault(kata, 1);
        double frekNormalisasi = (double) frek / maxFrekuensi;

        double skorPanjang = Math.min(panjang / 20.0, 1.0);
        double skorFrekuensi = 1 - frekNormalisasi;

        double skorAkhir = (skorPanjang * 0.4) + (skorFrekuensi * 0.6);
        skor.put(kata, skorAkhir);
    }
    return skor;
}

public Map<String, Integer> getFrekuensiKata() {
    return frekuensiKata;
}

public static int[] sarankanJumlahSoal(String teks) {
    if (teks == null || teks.trim().isEmpty()) {
        return new int[]{0, 0, 0, 0};
    }

    // Hitung kalimat valid pakai TextAnalyzer (sudah termasuk filter metadata)
    TextAnalyzer analyzer = new TextAnalyzer(teks);
    int jumlahKalimat = analyzer.getKalimat().size();

    if (jumlahKalimat < 5)       return new int[]{1, 1, 1, 0};
    else if (jumlahKalimat <= 10) return new int[]{2, 2, 1, 1};
    else if (jumlahKalimat <= 20) return new int[]{3, 3, 2, 1};
    else if (jumlahKalimat <= 35) return new int[]{4, 4, 3, 2};
    else                           return new int[]{5, 5, 4, 3};
}
    // Getter
    public List<String> getKalimat() { return kalimat; }
    public List<String> getKataKunci() { return kataKunci; }
    public String getTeks() { return teks; }
}