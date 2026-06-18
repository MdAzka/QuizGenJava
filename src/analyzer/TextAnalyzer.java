package analyzer;

import java.util.*;

public class TextAnalyzer {

    private String teks;
    private List<String> kalimat;
    private List<String> kataKunci;

    public TextAnalyzer(String teks) {
        this.teks = teks;
        this.kalimat = new ArrayList<>();
        this.kataKunci = new ArrayList<>();
        analisis();
    }

    // Memecah teks menjadi kalimat-kalimat
    private void analisis() {
        // Pecah berdasarkan tanda titik, tanya, seru
        String[] pecahan = teks.split("[.!?]+");
        for (String k : pecahan) {
            String bersih = k.trim();
            if (bersih.length() > 10) { // abaikan kalimat terlalu pendek
                kalimat.add(bersih);
            }
        }
        ekstrakKataKunci();
    }

    // Ekstrak kata kunci berdasarkan frekuensi
    private void ekstrakKataKunci() {
        // Kata-kata umum yang diabaikan (stopwords)
        Set<String> stopwords = new HashSet<>(Arrays.asList(
            "yang", "dan", "di", "ke", "dari", "ini", "itu",
            "adalah", "dengan", "untuk", "pada", "dalam", "atau",
            "the", "is", "a", "an", "of", "in", "to", "and", "for"
        ));

        // Hitung frekuensi tiap kata
        Map<String, Integer> frekuensi = new HashMap<>();
        String[] semuaKata = teks.toLowerCase().split("\\s+");

        for (String kata : semuaKata) {
            // Bersihkan tanda baca
            kata = kata.replaceAll("[^a-zA-Z0-9]", "");
            if (kata.length() > 3 && !stopwords.contains(kata)) {
                frekuensi.put(kata, frekuensi.getOrDefault(kata, 0) + 1);
            }
        }

        // Urutkan berdasarkan frekuensi tertinggi
        List<Map.Entry<String, Integer>> urutan = new ArrayList<>(frekuensi.entrySet());
        urutan.sort((a, b) -> b.getValue() - a.getValue());

        // Ambil 10 kata kunci teratas
        for (int i = 0; i < Math.min(30, urutan.size()); i++) {
            kataKunci.add(urutan.get(i).getKey());
        }
    }

    // Getter
    public List<String> getKalimat() { return kalimat; }
    public List<String> getKataKunci() { return kataKunci; }
    public String getTeks() { return teks; }
}