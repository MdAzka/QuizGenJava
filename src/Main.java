import model.FillInBlankQuestion;
import model.MultipleChoiceQuestion;
import model.Question;
import model.ShortAnswerQuestion;
import model.TrueFalseQuestion;
import analyzer.TextAnalyzer;
import generator.RuleBasedGenerator;
import config.QuizConfig;
import java.util.List;
import model.Question;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== TEST SEMUA JENIS SOAL ===\n");

        // 1. Fill in the Blank
        FillInBlankQuestion q1 = new FillInBlankQuestion(
            "Java dikembangkan oleh James Gosling pada tahun 1995.",
            "James Gosling"
        );
        System.out.println(q1.render());
        System.out.println("Kunci: " + q1.getKunciJawaban());
        System.out.println("Cek jawaban benar: " + q1.checkAnswer("James Gosling"));
        System.out.println("Cek jawaban salah: " + q1.checkAnswer("Bill Gates"));
        System.out.println();

        // 2. True/False
        TrueFalseQuestion q2 = new TrueFalseQuestion(
            "Polymorphism adalah konsep dasar dalam OOP.",
            true
        );
        System.out.println(q2.render());
        System.out.println("Kunci: " + q2.getKunciJawaban());
        System.out.println("Cek jawaban benar: " + q2.checkAnswer("benar"));
        System.out.println("Cek jawaban salah: " + q2.checkAnswer("salah"));
        System.out.println();

        // 3. Multiple Choice
        MultipleChoiceQuestion q3 = new MultipleChoiceQuestion(
            "Bahasa pemrograman apa yang dikembangkan oleh James Gosling?",
            Arrays.asList("Python", "Java", "C++", "Ruby"),
            1 // index 1 = "Java" = jawaban B
        );
        System.out.println(q3.render());
        System.out.println("Kunci: " + q3.getKunciJawaban());
        System.out.println("Cek jawaban benar: " + q3.checkAnswer("B"));
        System.out.println("Cek jawaban salah: " + q3.checkAnswer("A"));
        System.out.println();

        // 4. Short Answer
        ShortAnswerQuestion q4 = new ShortAnswerQuestion(
            "Jelaskan apa itu inheritance dalam OOP!",
            "inheritance",
            "Hubungan antara class induk dan class anak"
        );
        System.out.println(q4.render());
        System.out.println("Kunci: " + q4.getKunciJawaban());
        System.out.println("Cek jawaban benar: " + q4.checkAnswer("inheritance adalah pewarisan sifat"));
        System.out.println();

        System.out.println("=== SEMUA SOAL BERHASIL DIBUAT ===");

        // Test TextAnalyzer
        System.out.println("\n=== TEST TEXT ANALYZER ===\n");

        String contohTeks = "Java adalah bahasa pemrograman yang dikembangkan oleh James Gosling. " +
            "Java menggunakan konsep OOP seperti inheritance, polymorphism, dan encapsulation. " +
            "Inheritance memungkinkan class anak mewarisi sifat dari class induk. " +
            "Polymorphism memungkinkan satu method berperilaku berbeda di tiap class.";

        TextAnalyzer analyzer = new TextAnalyzer(contohTeks);

        System.out.println("KALIMAT DITEMUKAN:");
        for (String k : analyzer.getKalimat()) {
            System.out.println("- " + k);
        }

        System.out.println("\nKATA KUNCI TERATAS:");
        for (String kata : analyzer.getKataKunci()) {
            System.out.println("- " + kata);
        }

        // Test RuleBasedGenerator
        System.out.println("\n=== TEST RULE BASED GENERATOR ===\n");

        QuizConfig config = new QuizConfig(2, 2, 2, 2, 1);
        RuleBasedGenerator generator = new RuleBasedGenerator();
        List<Question> soalList = generator.generate(contohTeks, config);

        int nomor = 1;
        for (Question q : soalList) {
            System.out.println("Soal " + nomor + ":");
            System.out.println(q.render());
            System.out.println("Kunci: " + q.getKunciJawaban());
            System.out.println();
            nomor++;
        }
    }

    
    
}