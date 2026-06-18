package model;

public class TrueFalseQuestion extends Question {

    private boolean jawabanBenar;

    public TrueFalseQuestion(String pernyataan, boolean jawabanBenar) {
        super(
            pernyataan,
            jawabanBenar ? "Benar" : "Salah",
            1
        );
        this.jawabanBenar = jawabanBenar;
    }

    @Override
    public String render() {
        return "Benar/Salah: " + pertanyaan + " (B/S)";
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        String jawaban = userAnswer.trim().toLowerCase();
        if (jawabanBenar) {
            return jawaban.equals("benar") || jawaban.equals("b");
        } else {
            return jawaban.equals("salah") || jawaban.equals("s");
        }
    }
}