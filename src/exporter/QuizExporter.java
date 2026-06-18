package exporter;

import model.Question;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class QuizExporter {

    private static String getTanggal() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
    
    private static String getFolderOutput() {
    String desktop = System.getProperty("user.home") + "\\Desktop\\QuizOutput";
    java.io.File folder = new java.io.File(desktop);
    if (!folder.exists()) folder.mkdirs();
    return desktop;
}


    // ========================
    // EKSPOR KE TXT
    // ========================
    public static String exportTxt(List<Question> soalList, String judulQuiz) {
        String namaFile = getFolderOutput() + "\\quiz_" + judulQuiz + "_" + getTanggal() + ".txt";
        StringBuilder sb = new StringBuilder();

        sb.append("==========================================\n");
        sb.append("  QUIZ: ").append(judulQuiz.toUpperCase()).append("\n");
        sb.append("  Tanggal: ").append(getTanggal()).append("\n");
        sb.append("==========================================\n\n");

        sb.append("--- SOAL ---\n\n");
        int nomor = 1;
        for (Question q : soalList) {
            sb.append("Soal ").append(nomor).append(":\n");
            sb.append(q.render()).append("\n\n");
            nomor++;
        }

        sb.append("\n==========================================\n");
        sb.append("           KUNCI JAWABAN\n");
        sb.append("==========================================\n\n");
        nomor = 1;
        for (Question q : soalList) {
            sb.append("Soal ").append(nomor).append(": ")
              .append(q.getKunciJawaban()).append("\n");
            nomor++;
        }

        return tulisFile(namaFile, sb.toString()) ? namaFile : null;
    }

    // ========================
    // EKSPOR KE HTML (bisa print jadi PDF)
    // ========================
    public static String exportHtml(List<Question> soalList, String judulQuiz) {
    String namaFile = getFolderOutput() + "\\quiz_" + judulQuiz + "_" + getTanggal() + ".html";
    StringBuilder sb = new StringBuilder();

    // Pisahkan soal per tipe
    List<Question> mc   = new ArrayList<>();
    List<Question> fib  = new ArrayList<>();
    List<Question> tf   = new ArrayList<>();
    List<Question> esai = new ArrayList<>();
    for (Question q : soalList) {
        String t = q.getClass().getSimpleName();
        if (t.equals("MultipleChoiceQuestion"))  mc.add(q);
        else if (t.equals("FillInBlankQuestion")) fib.add(q);
        else if (t.equals("TrueFalseQuestion"))   tf.add(q);
        else                                       esai.add(q);
    }

    sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
    sb.append("<title>").append(judulQuiz).append("</title><style>");
    sb.append("*{margin:0;padding:0;box-sizing:border-box}");
    sb.append("body{font-family:'Segoe UI',Arial,sans-serif;font-size:13pt;color:#1a1a1a;background:#fff;padding:48px 64px;max-width:820px;margin:auto}");
    sb.append("h1{font-size:17pt;font-weight:700;text-align:center;letter-spacing:.5px;margin-bottom:4px}");
    sb.append(".subtitle{text-align:center;font-size:11pt;color:#555;margin-bottom:6px}");
    sb.append(".meta{text-align:center;font-size:10pt;color:#888;margin-bottom:4px}");
    sb.append(".identity{display:flex;justify-content:space-between;font-size:11pt;border-top:1.5px solid #222;border-bottom:1px solid #ccc;padding:8px 0;margin:18px 0 24px}");
    sb.append(".seksi{font-size:12pt;font-weight:700;margin:28px 0 14px;padding-bottom:4px;border-bottom:1.5px solid #222;text-transform:uppercase;letter-spacing:.5px}");
    sb.append(".soal{margin-bottom:20px;line-height:1.75}");
    sb.append(".soal-q{display:flex;gap:10px}");
    sb.append(".soal-num{font-weight:600;min-width:24px}");
    sb.append(".pilihan{margin:6px 0 0 34px;line-height:1.9}");
    sb.append(".jawaban{margin:4px 0 0 34px;font-weight:700;font-size:11pt;color:#1a1a1a}");
    sb.append(".garis-isian{display:inline-block;border-bottom:1.5px solid #555;width:140px;margin:0 4px;vertical-align:bottom}");
    sb.append(".bs-label{font-weight:600;margin-left:8px;color:#333}");
    sb.append(".no-print{text-align:center;margin:24px 0}");
    sb.append(".no-print button{padding:9px 28px;font-size:12pt;border:1.5px solid #222;background:#fff;cursor:pointer;border-radius:4px;font-family:inherit}");
    sb.append(".no-print button:hover{background:#f0f0f0}");
    sb.append("@media print{.no-print{display:none}body{padding:32px 48px}}");
    sb.append("</style></head><body>");

    // Header
    sb.append("<h1>").append(judulQuiz.toUpperCase()).append("</h1>");
    sb.append("<div class='subtitle'>Lembar Soal Quiz</div>");
    sb.append("<div class='meta'>Tanggal: ").append(getTanggal())
      .append(" &nbsp;·&nbsp; Total Soal: ").append(soalList.size()).append("</div>");
    sb.append("<div class='identity'>");
    sb.append("<span>Nama &nbsp;: _________________________________</span>");
    sb.append("<span>Nilai &nbsp;: ___________</span>");
    sb.append("</div>");

    sb.append("<div class='no-print'><button onclick='window.print()'>Cetak / Simpan sebagai PDF</button></div>");

    int globalNum = 1;

    // A. Pilihan Ganda
    if (!mc.isEmpty()) {
        sb.append("<div class='seksi'>A. Pilihan Ganda</div>");
        for (Question q : mc) {
            String[] baris = q.render().split("\n");
            sb.append("<div class='soal'><div class='soal-q'>");
            sb.append("<span class='soal-num'>").append(globalNum).append(".</span>");
            sb.append("<span>").append(baris[0].replace("Pilihan Ganda: ", "")).append("</span></div>");
            sb.append("<div class='pilihan'>");
            for (int i = 1; i < baris.length; i++)
                if (!baris[i].trim().isEmpty())
                    sb.append(baris[i].trim()).append("<br>");
            sb.append("</div>");
            sb.append("<div class='jawaban'>Jawaban: ").append(q.getKunciJawaban()).append("</div>");
            sb.append("</div>");
            globalNum++;
        }
    }

    // B. Isian
    if (!fib.isEmpty()) {
        sb.append("<div class='seksi'>B. Isian</div>");
        for (Question q : fib) {
            String soalTeks = q.render().replace("Isian: ", "")
                .replace("_____", "<span class='garis-isian'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
            sb.append("<div class='soal'><div class='soal-q'>");
            sb.append("<span class='soal-num'>").append(globalNum).append(".</span>");
            sb.append("<span>").append(soalTeks).append("</span></div>");
            sb.append("<div class='jawaban'>Jawaban: ").append(q.getKunciJawaban()).append("</div>");
            sb.append("</div>");
            globalNum++;
        }
    }

    // C. Benar / Salah
    if (!tf.isEmpty()) {
        sb.append("<div class='seksi'>C. Benar / Salah</div>");
        for (Question q : tf) {
            String soalTeks = q.render()
                .replace("Benar/Salah: ", "")
                .replace(" (B/S)", "");
            sb.append("<div class='soal'><div class='soal-q'>");
            sb.append("<span class='soal-num'>").append(globalNum).append(".</span>");
            sb.append("<span>").append(soalTeks);
            sb.append("<span class='bs-label'>( B &nbsp;/&nbsp; S )</span></span></div>");
            sb.append("<div class='jawaban'>Jawaban: ").append(q.getKunciJawaban()).append("</div>");
            sb.append("</div>");
            globalNum++;
        }
    }

    // D. Esai
    if (!esai.isEmpty()) {
        sb.append("<div class='seksi'>D. Esai Singkat</div>");
        for (Question q : esai) {
            String soalTeks = q.render()
                .replace("Esai Singkat: ", "")
                .replaceAll("\\(Petunjuk:.*?\\)", "");
            sb.append("<div class='soal'><div class='soal-q'>");
            sb.append("<span class='soal-num'>").append(globalNum).append(".</span>");
            sb.append("<span>").append(soalTeks.trim()).append("</span></div>");
            sb.append("<div class='jawaban'>Jawaban: ").append(q.getKunciJawaban()).append("</div>");
            sb.append("</div>");
            globalNum++;
        }
    }

    sb.append("</body></html>");
    return tulisFile(namaFile, sb.toString()) ? namaFile : null;
}

    // ========================
    // EKSPOR KE JSON
    // ========================
    public static String exportJson(List<Question> soalList, String judulQuiz) {
        String namaFile = getFolderOutput() + "quiz_" + judulQuiz + "_" + getTanggal() + ".json";
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"judul\": \"").append(judulQuiz).append("\",\n");
        sb.append("  \"tanggal\": \"").append(getTanggal()).append("\",\n");
        sb.append("  \"soal\": [\n");

        for (int i = 0; i < soalList.size(); i++) {
            Question q = soalList.get(i);
            sb.append("    {\n");
            sb.append("      \"nomor\": ").append(i + 1).append(",\n");
            sb.append("      \"tipe\": \"").append(q.getClass().getSimpleName()).append("\",\n");
            sb.append("      \"pertanyaan\": \"").append(escapeJson(q.getPertanyaan())).append("\",\n");
            sb.append("      \"kunci\": \"").append(escapeJson(q.getKunciJawaban())).append("\"\n");
            sb.append("    }");
            if (i < soalList.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n}");

        return tulisFile(namaFile, sb.toString()) ? namaFile : null;
    }

    // ========================
    // HELPER
    // ========================
    private static boolean tulisFile(String namaFile, String konten) {
    try {
        FileWriter writer = new FileWriter(namaFile);
        writer.write(konten);
        writer.close();
        // Auto-open file setelah disimpan
        java.awt.Desktop.getDesktop().open(new File(namaFile));
        System.out.println("File disimpan di: " + namaFile);
        return true;
    } catch (IOException e) {
        System.out.println("Gagal tulis file: " + e.getMessage());
        return false;
    }
}

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}