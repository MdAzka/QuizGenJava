package gui;

import config.QuizConfig;
import config.QuizHistoryManager;
import exporter.QuizExporter;
import generator.ApiBasedGenerator;
import generator.QuestionGenerator;
import generator.RuleBasedGenerator;
import model.Question;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    // ── API Key ──────────────────────────────────────────────
    private final List<String> API_KEYS = config.AppConfig.getApiKeys();
    // ── Warna tema ───────────────────────────────────────────
    private static final String C_BG        = "#F7F8FA";
    private static final String C_PANEL     = "#FFFFFF";
    private static final String C_BORDER    = "#E2E6EA";
    private static final String C_PRIMARY   = "#2D6BE4";
    private static final String C_DANGER    = "#D63031";
    private static final String C_SUCCESS   = "#00B894";
    private static final String C_TEXT      = "#2D3436";
    private static final String C_SUBTEXT   = "#636E72";
    private static final String C_ACCENT    = "#FDCB6E";

    // ── Komponen ─────────────────────────────────────────────
    private TextArea        inputTeks;
    private ComboBox<String> comboMetode;
    private ComboBox<String> comboKesulitan;
    private Spinner<Integer> spinnerFIB, spinnerTF, spinnerMC, spinnerSA;
    private Button          btnGenerate, btnUploadPdf;
    private VBox            soalContainer;
    private ScrollPane      scrollSoal;
    private Label           labelStatus;
    private ListView<String> listRiwayat;
    private List<Question>  soalTerakhir = new ArrayList<>();
    private Label labelSaran;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Quiz Generator Otomatis");

        // ── PANEL KIRI: Input ────────────────────────────────
        VBox panelKiri = buildPanelKiri(stage);

        // ── PANEL TENGAH: Kontrol ────────────────────────────
        VBox panelTengah = buildPanelTengah();

        // ── PANEL KANAN: Output ──────────────────────────────
        VBox panelKanan = buildPanelKanan();

        // ── PANEL RIWAYAT ────────────────────────────────────
        VBox panelRiwayat = buildPanelRiwayat();

        // ── ROOT ─────────────────────────────────────────────
        HBox root = new HBox(12, panelKiri, panelTengah, panelKanan, panelRiwayat);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: " + C_BG + ";");

        Scene scene = new Scene(root, 1200, 680);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.show();

        refreshRiwayat();
    }

    // ════════════════════════════════════════════════════════
    // BUILD PANELS
    // ════════════════════════════════════════════════════════

    private VBox buildPanelKiri(Stage stage) {
        Label judul = sectionLabel("Teks Bacaan");

        btnUploadPdf = new Button("Upload PDF");
        btnUploadPdf.setMaxWidth(Double.MAX_VALUE);
        btnUploadPdf.setStyle(btnStyle(C_ACCENT, C_TEXT));
        btnUploadPdf.setOnAction(e -> uploadPdf(stage));

        inputTeks = new TextArea();
        inputTeks.setPromptText("Paste teks materi di sini, atau upload PDF di atas...");
        inputTeks.setWrapText(true);
        inputTeks.setStyle(
            "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;" +
            "-fx-background-color: " + C_PANEL + ";" +
            "-fx-border-color: " + C_BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;"
        );

        inputTeks.textProperty().addListener((obs, oldVal, newVal) -> updateSaranJumlahSoal(newVal));

        VBox panel = card();
        panel.setPrefWidth(370);
        VBox.setVgrow(inputTeks, Priority.ALWAYS);
        panel.getChildren().addAll(judul, btnUploadPdf, inputTeks);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildPanelTengah() {
        Label judulMetode = sectionLabel("Metode");
        comboMetode = new ComboBox<>();
        comboMetode.getItems().addAll("Rule-Based (Offline)", "Gemini API (Online)");
        comboMetode.setValue("Rule-Based (Offline)");
        comboMetode.setMaxWidth(Double.MAX_VALUE);
        comboMetode.setStyle(comboStyle());

        Label judulKesulitan = sectionLabel("Kesulitan");
        comboKesulitan = new ComboBox<>();
        comboKesulitan.getItems().addAll("1 — Mudah", "2 — Sedang", "3 — Sulit");
        comboKesulitan.setValue("1 — Mudah");
        comboKesulitan.setMaxWidth(Double.MAX_VALUE);
        comboKesulitan.setStyle(comboStyle());

        Label judulKonfig = sectionLabel("Jumlah Soal");
        spinnerFIB = spinner();
        spinnerTF  = spinner();
        spinnerMC  = spinner();
        spinnerSA  = spinner();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(spinnerLabel("Isian"),          0, 0); grid.add(spinnerFIB, 1, 0);
        grid.add(spinnerLabel("Benar / Salah"),  0, 1); grid.add(spinnerTF,  1, 1);
        grid.add(spinnerLabel("Pilihan Ganda"),  0, 2); grid.add(spinnerMC,  1, 2);
        grid.add(spinnerLabel("Esai"),           0, 3); grid.add(spinnerSA,  1, 3);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setHgrow(Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(80);
        grid.getColumnConstraints().addAll(c0, c1);

        labelSaran = new Label("Upload teks untuk lihat saran jumlah soal.");
        labelSaran.setWrapText(true);
        labelSaran.setStyle("-fx-font-size: 10px; -fx-text-fill: " + C_SUBTEXT + "; -fx-font-style: italic;");

        Button btnPakaiSaran = new Button("Pakai Saran");
        btnPakaiSaran.setMaxWidth(Double.MAX_VALUE);
        btnPakaiSaran.setStyle(btnStyle("#00897B", "#FFFFFF") + "-fx-font-size: 11px;");
        btnPakaiSaran.setOnAction(e -> pakaiSaranJumlahSoal());

        Separator sep1 = sep();
        Separator sep2 = sep();

        btnGenerate = new Button("Generate Quiz");
        btnGenerate.setMaxWidth(Double.MAX_VALUE);
        btnGenerate.setStyle(btnStyle(C_PRIMARY, "#FFFFFF") + "-fx-font-size: 14px; -fx-font-weight: bold;");
        btnGenerate.setOnAction(e -> generateQuiz());

        Label judulEkspor = sectionLabel("Ekspor");
        Button btnTxt  = eksporBtn("Simpan TXT",      "#455A64", e -> eksporTxt());
        Button btnHtml = eksporBtn("Simpan HTML/PDF",  "#455A64", e -> eksporHtml());
        Button btnJson = eksporBtn("Simpan JSON",      "#455A64", e -> eksporJson());

        Button btnHapusBuruk = new Button("Hapus Soal Ditandai");
        btnHapusBuruk.setMaxWidth(Double.MAX_VALUE);
        btnHapusBuruk.setStyle(btnStyle(C_DANGER, "#FFFFFF"));
        btnHapusBuruk.setOnAction(e -> hapusSoalDitandai());

        VBox panel = card();
        panel.setPrefWidth(230);
        panel.getChildren().addAll(
            judulMetode, comboMetode,
            sep1,
            judulKesulitan, comboKesulitan,
            sep2,
            judulKonfig, grid,
            labelSaran, btnPakaiSaran,
            new Separator(),
            btnGenerate,
            new Separator(),
            judulEkspor, btnTxt, btnHtml, btnJson,
            new Separator(),
            btnHapusBuruk
        );
        return panel;
    }

    private VBox buildPanelKanan() {
        Label judul = sectionLabel("Hasil Soal");

        labelStatus = new Label("Hasil soal akan muncul di sini setelah Generate.");
        labelStatus.setStyle("-fx-text-fill: " + C_SUBTEXT + "; -fx-font-size: 12px;");
        labelStatus.setWrapText(true);

        soalContainer = new VBox(10);
        soalContainer.setPadding(new Insets(4));

        scrollSoal = new ScrollPane(soalContainer);
        scrollSoal.setFitToWidth(true);
        scrollSoal.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollSoal, Priority.ALWAYS);

        VBox panel = card();
        panel.setPrefWidth(420);
        VBox.setVgrow(panel, Priority.ALWAYS);
        panel.getChildren().addAll(judul, labelStatus, scrollSoal);
        return panel;
    }

    private VBox buildPanelRiwayat() {
        Label judul = sectionLabel("Riwayat Quiz");

        listRiwayat = new ListView<>();
        listRiwayat.setStyle(
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_BORDER + "; -fx-border-radius: 6;"
        );
        VBox.setVgrow(listRiwayat, Priority.ALWAYS);

        // Klik kanan -> context menu
        ContextMenu menu = new ContextMenu();
        MenuItem itemBukaFolder = new MenuItem("Buka Folder Output");
        itemBukaFolder.setOnAction(e -> bukaFolderOutput());
        MenuItem itemHapusSatu = new MenuItem("Hapus dari Riwayat");
        itemHapusSatu.setOnAction(e -> hapusSatuRiwayat());
        menu.getItems().addAll(itemBukaFolder, itemHapusSatu);
        listRiwayat.setContextMenu(menu);
Button btnBukaFolder = new Button("Buka Folder Output");
    btnBukaFolder.setMaxWidth(Double.MAX_VALUE);
    btnBukaFolder.setStyle(btnStyle("#455A64", "#FFFFFF"));
    btnBukaFolder.setOnAction(e -> bukaFolderOutput());

    Button btnHapus = new Button("Hapus Riwayat");
    btnHapus.setMaxWidth(Double.MAX_VALUE);
    btnHapus.setStyle(btnStyle(C_DANGER, "#FFFFFF"));
    btnHapus.setOnAction(e -> {
        QuizHistoryManager.hapusSemua();
        refreshRiwayat();
    });

    VBox panel = card();
    panel.setPrefWidth(200);
    VBox.setVgrow(panel, Priority.ALWAYS);
    panel.getChildren().addAll(judul, listRiwayat, btnBukaFolder, btnHapus);
    return panel;
}
    

    // ════════════════════════════════════════════════════════
    // LOGIC
    // ════════════════════════════════════════════════════════

    private void generateQuiz() {
        String teks = inputTeks.getText().trim();
        if (teks.isEmpty()) {
            labelStatus.setText("⚠ Teks tidak boleh kosong.");
            return;
        }

        int kesulitan = comboKesulitan.getValue().startsWith("1") ? 1
                      : comboKesulitan.getValue().startsWith("2") ? 2 : 3;

        QuizConfig config = new QuizConfig(
            spinnerFIB.getValue(), spinnerTF.getValue(),
            spinnerMC.getValue(),  spinnerSA.getValue(),
            kesulitan
        );

        btnGenerate.setDisable(true);
        btnGenerate.setText("Memproses...");
        soalContainer.getChildren().clear();
        labelStatus.setText("Membuat soal, mohon tunggu...");

        Task<List<Question>> task = new Task<>() {
            @Override protected List<Question> call() throws Exception {
                QuestionGenerator gen = comboMetode.getValue().contains("Gemini")
                ? new ApiBasedGenerator(API_KEYS)
                : new RuleBasedGenerator();
                return gen.generate(teks, config);
            }
        };

        task.setOnSucceeded(e -> {
            List<Question> soalList = task.getValue();
            soalTerakhir = soalList;
            if (soalList.isEmpty()) {
                labelStatus.setText("⚠ Teks terlalu pendek. Coba tambah lebih banyak materi.");
            } else {
                labelStatus.setText(soalList.size() + " soal berhasil dibuat.");
                tampilkanSoalInteraktif(soalList);
            }
            btnGenerate.setDisable(false);
            btnGenerate.setText("Generate Quiz");
        });

        task.setOnFailed(e -> {
            labelStatus.setText("Error: " + task.getException().getMessage());
            btnGenerate.setDisable(false);
            btnGenerate.setText("Generate Quiz");
        });

        new Thread(task).start();
    }

    private VBox buatKartuSoal(Question q, boolean isBaru) {
    Label lblNomor = new Label(); // teks diisi nanti oleh renomorSemuaKartu()
    lblNomor.setStyle(
        "-fx-font-weight: bold; -fx-font-size: 12px;" +
        "-fx-text-fill: " + (isBaru ? C_SUCCESS : C_PRIMARY) + ";"
    );

    Label lblIsi = new Label(q.render());
    lblIsi.setWrapText(true);
    lblIsi.setStyle("-fx-font-size: 13px; -fx-text-fill: " + C_TEXT + ";");

    Label lblKunci = new Label("Jawaban: " + q.getKunciJawaban());
    lblKunci.setStyle("-fx-font-size: 11px; -fx-text-fill: " + C_SUCCESS + ";");

    CheckBox cb = new CheckBox("Tandai soal ini perlu diperbaiki");
    cb.setStyle("-fx-font-size: 11px; -fx-text-fill: " + C_DANGER + ";");

    VBox kartu = new VBox(6, lblNomor, lblIsi, lblKunci, cb);
    kartu.setPadding(new Insets(12));
    kartu.setStyle(kartuStyle(false));
    kartu.setUserData(new Object[]{q, cb, lblNomor, isBaru});

    cb.selectedProperty().addListener((obs, ov, nv) ->
        kartu.setStyle(kartuStyle(nv))
    );

    return kartu;
}

    private void tampilkanSoalInteraktif(List<Question> soalList) {
    soalContainer.getChildren().clear();
    for (Question q : soalList) {
        VBox kartu = buatKartuSoal(q, false); // false = bukan soal baru
        soalContainer.getChildren().add(kartu);
    }
    renomorSemuaKartu();
}

   private void tampilkanSoalBaruDenganAnimasi(List<Question> soalBaru) {
    for (Question q : soalBaru) {
        VBox kartu = buatKartuSoal(q, true); // true = soal baru (label hijau + animasi)
        kartu.setOpacity(0);
        soalContainer.getChildren().add(kartu); // selalu ditambah di akhir = paling bawah

        FadeTransition fade = new FadeTransition(Duration.seconds(0.3), kartu);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    renomorSemuaKartu(); // re-render nomor 1, 2, 3... dari awal
}

// Renomor ulang semua kartu yang ada di soalContainer secara berurutan
private void renomorSemuaKartu() {
    int nomor = 1;
    for (javafx.scene.Node node : soalContainer.getChildren()) {
        if (node instanceof VBox kartu) {
            Object[] data = (Object[]) kartu.getUserData();
            if (data != null && data.length >= 3) {
                Question q = (Question) data[0];
                Label lblNomor = (Label) data[2];

                String tipeRaw = q.getClass().getSimpleName()
                    .replace("FillInBlankQuestion",   "Isian")
                    .replace("TrueFalseQuestion",      "Benar / Salah")
                    .replace("MultipleChoiceQuestion", "Pilihan Ganda")
                    .replace("ShortAnswerQuestion",    "Esai");

                boolean isBaru = data.length >= 4 && Boolean.TRUE.equals(data[3]);
                String suffix = isBaru ? "  (baru)" : "";

                lblNomor.setText("Soal " + nomor + "  ·  " + tipeRaw + suffix);
                nomor++;
            }
        }
    }
}

    private void hapusSoalDitandai() {
    List<javafx.scene.Node> toRemove = new ArrayList<>();
    int fib = 0, tf = 0, mc = 0, sa = 0;

    for (javafx.scene.Node node : soalContainer.getChildren()) {
        if (node instanceof VBox) {
            Object[] data = (Object[]) ((VBox) node).getUserData();
            if (data != null && ((CheckBox) data[1]).isSelected()) {
                toRemove.add(node);
                Question q = (Question) data[0];
                soalTerakhir.remove(q);

                String tipe = q.getClass().getSimpleName();
                switch (tipe) {
                    case "FillInBlankQuestion"   -> fib++;
                    case "TrueFalseQuestion"      -> tf++;
                    case "MultipleChoiceQuestion" -> mc++;
                    case "ShortAnswerQuestion"    -> sa++;
                }
            }
        }
    }

    int totalDihapus = toRemove.size();
    soalContainer.getChildren().removeAll(toRemove);
        renomorSemuaKartu(); // renomor ulang setelah ada yang dihapus


    if (totalDihapus == 0) {
        labelStatus.setText("Tidak ada soal yang ditandai untuk dihapus.");
        return;
    }

    if (fib == 0 && tf == 0 && mc == 0 && sa == 0) {
        labelStatus.setText(totalDihapus + " soal dihapus.");
        return;
    }

    // Regenerasi soal pengganti sejumlah yang dihapus, di background thread
    String teks = inputTeks.getText().trim();
    if (teks.isEmpty()) {
        labelStatus.setText(totalDihapus + " soal dihapus. (Tidak bisa regenerasi: teks input kosong)");
        return;
    }

    int kesulitan = comboKesulitan.getValue().startsWith("1") ? 1
                  : comboKesulitan.getValue().startsWith("2") ? 2 : 3;

    QuizConfig configPengganti = new QuizConfig(fib, tf, mc, sa, kesulitan);

    labelStatus.setText(totalDihapus + " soal dihapus. Membuat soal pengganti...");

    final int fFib = fib, fTf = tf, fMc = mc, fSa = sa;

    Task<List<Question>> task = new Task<>() {
        @Override protected List<Question> call() throws Exception {
            QuestionGenerator gen = comboMetode.getValue().contains("Gemini")
                ? new ApiBasedGenerator(API_KEYS)
                : new RuleBasedGenerator();
            return gen.generate(teks, configPengganti);
        }
    };

    task.setOnSucceeded(e -> {
        List<Question> soalBaru = task.getValue();
        soalTerakhir.addAll(soalBaru);
        tampilkanSoalBaruDenganAnimasi(soalBaru);
        labelStatus.setText(totalDihapus + " soal dihapus -> " + soalBaru.size() +
            " soal baru digenerate sebagai pengganti.");
    });

    task.setOnFailed(e -> {
        labelStatus.setText(totalDihapus + " soal dihapus. Gagal membuat pengganti: " +
            task.getException().getMessage());
    });

    new Thread(task).start();
}

    private void uploadPdf(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih File PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        btnUploadPdf.setDisable(true);
        btnUploadPdf.setText("Membaca PDF...");

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception {
                return analyzer.PdfReader.bacaPdf(file.getAbsolutePath());
            }
        };

        task.setOnSucceeded(e -> {
            inputTeks.setText(task.getValue());
            labelStatus.setText("PDF berhasil dibaca. Klik Generate Quiz untuk mulai.");
            btnUploadPdf.setDisable(false);
            btnUploadPdf.setText("Upload PDF");
        });

        task.setOnFailed(e -> {
            labelStatus.setText("Gagal baca PDF: " + task.getException().getMessage());
            btnUploadPdf.setDisable(false);
            btnUploadPdf.setText("Upload PDF");
        });

        new Thread(task).start();
    }

    private void eksporTxt() {
        if (soalTerakhir.isEmpty()) { labelStatus.setText("Generate soal dulu sebelum ekspor."); return; }
        String hasil = QuizExporter.exportTxt(soalTerakhir, "quiz");
        if (hasil != null) {
            QuizHistoryManager.simpan("quiz", soalTerakhir.size(), hasil);
            refreshRiwayat();
            labelStatus.setText("Tersimpan: " + hasil);
        } else labelStatus.setText("Gagal ekspor TXT.");
    }

    private void eksporHtml() {
        if (soalTerakhir.isEmpty()) { labelStatus.setText("Generate soal dulu sebelum ekspor."); return; }
        String hasil = QuizExporter.exportHtml(soalTerakhir, "quiz");
        if (hasil != null) {
            QuizHistoryManager.simpan("quiz", soalTerakhir.size(), hasil);
            refreshRiwayat();
            labelStatus.setText("Tersimpan: " + hasil);
        } else labelStatus.setText("Gagal ekspor HTML.");
    }

    private void eksporJson() {
        if (soalTerakhir.isEmpty()) { labelStatus.setText("Generate soal dulu sebelum ekspor."); return; }
        String hasil = QuizExporter.exportJson(soalTerakhir, "quiz");
        if (hasil != null) {
            QuizHistoryManager.simpan("quiz", soalTerakhir.size(), hasil);
            refreshRiwayat();
            labelStatus.setText("Tersimpan: " + hasil);
        } else labelStatus.setText("Gagal ekspor JSON.");
    }

    private void refreshRiwayat() {
        listRiwayat.getItems().clear();
        List<QuizHistoryManager.RiwayatQuiz> semua = QuizHistoryManager.loadSemua();
        if (semua.isEmpty()) {
            listRiwayat.getItems().add("Belum ada riwayat.");
        } else {
            for (QuizHistoryManager.RiwayatQuiz r : semua)
                listRiwayat.getItems().add(r.toString());
        }
    }

    private int[] saranTerakhir = new int[]{0, 0, 0, 0};

    private void updateSaranJumlahSoal(String teks) {
        if (teks == null || teks.trim().length() < 20) {
            labelSaran.setText("Upload teks untuk lihat saran jumlah soal.");
            saranTerakhir = new int[]{0, 0, 0, 0};
            return;
        }
        saranTerakhir = analyzer.TextAnalyzer.sarankanJumlahSoal(teks);
        labelSaran.setText(String.format(
            "Saran: Isian=%d, B/S=%d, PG=%d, Esai=%d",
            saranTerakhir[0], saranTerakhir[1], saranTerakhir[2], saranTerakhir[3]
        ));
    }

    private void pakaiSaranJumlahSoal() {
        if (saranTerakhir[0] == 0 && saranTerakhir[1] == 0 &&
            saranTerakhir[2] == 0 && saranTerakhir[3] == 0) {
            labelStatus.setText("Belum ada saran tersedia. Isi teks dulu.");
            return;
        }
        spinnerFIB.getValueFactory().setValue(saranTerakhir[0]);
        spinnerTF.getValueFactory().setValue(saranTerakhir[1]);
        spinnerMC.getValueFactory().setValue(saranTerakhir[2]);
        spinnerSA.getValueFactory().setValue(saranTerakhir[3]);
        labelStatus.setText("Jumlah soal diatur sesuai saran.");
    }

    private void bukaFolderOutput() {
    try {
        String folderPath = System.getProperty("user.home") + "\\Desktop\\QuizOutput";
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();
        java.awt.Desktop.getDesktop().open(folder);
    } catch (Exception e) {
        labelStatus.setText("Gagal membuka folder: " + e.getMessage());
    }
}

private void hapusSatuRiwayat() {
    int index = listRiwayat.getSelectionModel().getSelectedIndex();
    if (index < 0) {
        labelStatus.setText("Pilih dulu item riwayat yang mau dihapus.");
        return;
    }
    List<QuizHistoryManager.RiwayatQuiz> semua = QuizHistoryManager.loadSemua();
    if (index < semua.size()) {
        semua.remove(index);
        QuizHistoryManager.simpanUlangSemua(semua);
        refreshRiwayat();
        labelStatus.setText("Item riwayat dihapus.");
    }
}

    // ════════════════════════════════════════════════════════
    // STYLE HELPERS
    // ════════════════════════════════════════════════════════

    private VBox card() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle(
            "-fx-background-color: " + C_PANEL + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 10; -fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );
        return box;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-text-fill: " + C_SUBTEXT + "; -fx-letter-spacing: 1px;"
        );
        return l;
    }

    private Label spinnerLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + C_TEXT + ";");
        return l;
    }

    private Spinner<Integer> spinner() {
        Spinner<Integer> s = new Spinner<>(0, 30, 2);
        s.setEditable(true);
        s.setPrefWidth(80);
        s.setStyle("-fx-font-size: 12px;");
        return s;
    }

    private Separator sep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: " + C_BORDER + ";");
        return s;
    }

    private String btnStyle(String bg, String fg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
               "-fx-background-radius: 6; -fx-padding: 8 14; -fx-cursor: hand;" +
               "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px;";
    }

    private String comboStyle() {
        return "-fx-background-color: " + C_BG + "; -fx-border-color: " + C_BORDER + ";" +
               "-fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px;";
    }

    private String kartuStyle(boolean ditandai) {
        String bg     = ditandai ? "#FFF5F5" : C_PANEL;
        String border = ditandai ? C_DANGER  : C_BORDER;
        return "-fx-background-color: " + bg + ";" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-radius: 8; -fx-background-radius: 8;";
    }

    private Button eksporBtn(String label, String color,
                             javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button(label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(btnStyle(color, "#FFFFFF"));
        b.setOnAction(handler);
        return b;
    }

    public static void main(String[] args) { launch(args); }
}