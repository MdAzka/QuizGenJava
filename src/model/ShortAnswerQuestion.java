package model;

public class ShortAnswerQuestion extends Question {

    private String petunjuk;

    public ShortAnswerQuestion(String pertanyaan, String kunciJawaban, String petunjuk) {
        super(pertanyaan, kunciJawaban, 3);
        this.petunjuk = petunjuk;
    }

    @Override
    public String render() {
        return "Esai Singkat: " + pertanyaan + "\n  (Petunjuk: " + petunjuk + ")";
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        // Esai tidak dicek exact, cukup cek apakah mengandung kata kunci
        return userAnswer.toLowerCase().contains(kunciJawaban.toLowerCase());
    }

    public String getPetunjuk() { return petunjuk; }
}