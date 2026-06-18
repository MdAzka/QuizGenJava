package config;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class QuizHistoryManager {

    private static final String FILE_RIWAYAT = "quiz_history.json";

    // Representasi satu riwayat quiz
    public static class RiwayatQuiz {
        public String judul;
        public String tanggalWaktu;
        public int jumlahSoal;
        public String namaFile;

        public RiwayatQuiz(String judul, int jumlahSoal, String namaFile) {
            this.judul = judul;
            this.jumlahSoal = jumlahSoal;
            this.namaFile = namaFile;
            this.tanggalWaktu = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }

        // Constructor untuk load dari file
        public RiwayatQuiz(String judul, String tanggalWaktu, int jumlahSoal, String namaFile) {
            this.judul = judul;
            this.tanggalWaktu = tanggalWaktu;
            this.jumlahSoal = jumlahSoal;
            this.namaFile = namaFile;
        }

        @Override
        public String toString() {
            return "[" + tanggalWaktu + "] " + judul + " (" + jumlahSoal + " soal) → " + namaFile;
        }
    }

    // Simpan riwayat baru
    public static void simpan(String judul, int jumlahSoal, String namaFile) {
        List<RiwayatQuiz> semua = loadSemua();
        semua.add(new RiwayatQuiz(judul, jumlahSoal, namaFile));
        tulisKeFile(semua);
    }

    // Load semua riwayat
    public static List<RiwayatQuiz> loadSemua() {
        List<RiwayatQuiz> hasil = new ArrayList<>();
        File file = new File(FILE_RIWAYAT);

        if (!file.exists()) return hasil;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String judul = "", tanggal = "", namaFile = "";
            int jumlah = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains("\"judul\""))
                    judul = ambilNilai(line);
                else if (line.contains("\"tanggal\""))
                    tanggal = ambilNilai(line);
                else if (line.contains("\"jumlahSoal\""))
                    jumlah = Integer.parseInt(ambilNilai(line));
                else if (line.contains("\"namaFile\"")) {
                    namaFile = ambilNilai(line);
                    hasil.add(new RiwayatQuiz(judul, tanggal, jumlah, namaFile));
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal load riwayat: " + e.getMessage());
        }

        return hasil;
    }

    // Hapus semua riwayat
    public static void hapusSemua() {
        new File(FILE_RIWAYAT).delete();
    }

    // Tulis semua riwayat ke file JSON
    private static void tulisKeFile(List<RiwayatQuiz> semua) {
        try (FileWriter fw = new FileWriter(FILE_RIWAYAT)) {
            fw.write("[\n");
            for (int i = 0; i < semua.size(); i++) {
                RiwayatQuiz r = semua.get(i);
                fw.write("  {\n");
                fw.write("    \"judul\": \"" + r.judul + "\",\n");
                fw.write("    \"tanggal\": \"" + r.tanggalWaktu + "\",\n");
                fw.write("    \"jumlahSoal\": " + r.jumlahSoal + ",\n");
                fw.write("    \"namaFile\": \"" + r.namaFile + "\"\n");
                fw.write("  }");
                if (i < semua.size() - 1) fw.write(",");
                fw.write("\n");
            }
            fw.write("]");
        } catch (IOException e) {
            System.out.println("Gagal simpan riwayat: " + e.getMessage());
        }
    }

    private static String ambilNilai(String line) {
        int start = line.lastIndexOf(":") + 1;
        String val = line.substring(start).trim();
        val = val.replaceAll("[,\"\\[\\]]", "").trim();
        return val;
    }
}