package model;

import java.util.List;

public class MultipleChoiceQuestion extends Question {

    private List<String> pilihan; // [A, B, C, D]
    private int indexJawaban;     // index pilihan yang benar (0=A, 1=B, dst)

    public MultipleChoiceQuestion(String pertanyaan, List<String> pilihan, int indexJawaban) {
        super(
            pertanyaan,
            String.valueOf((char) ('A' + indexJawaban)), // "A", "B", "C", atau "D"
            2
        );
        this.pilihan = pilihan;
        this.indexJawaban = indexJawaban;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pilihan Ganda: ").append(pertanyaan).append("\n");
        char huruf = 'A';
        for (String p : pilihan) {
            sb.append("  ").append(huruf).append(") ").append(p).append("\n");
            huruf++;
        }
        return sb.toString();
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        return userAnswer.trim().equalsIgnoreCase(kunciJawaban);
    }

    public List<String> getPilihan() { return pilihan; }
    public int getIndexJawaban() { return indexJawaban; }
}