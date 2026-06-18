package analyzer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfReader {

    public static String bacaPdf(String pathFile) throws IOException {
        File file = new File(pathFile);

        if (!file.exists()) {
            throw new IOException("File tidak ditemukan: " + pathFile);
        }

        if (!pathFile.toLowerCase().endsWith(".pdf")) {
            throw new IOException("File bukan PDF!");
        }

        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String teks = stripper.getText(doc);

            if (teks == null || teks.trim().isEmpty()) {
                throw new IOException("PDF tidak mengandung teks (mungkin PDF scan/gambar).");
            }

            return teks.trim();
        }
    }
}