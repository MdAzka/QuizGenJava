package model;

public class FillInBlankQuestion extends Question {

    public FillInBlankQuestion(String kalimat, String kataKunci) {
        super(
            kalimat.replace(kataKunci, "_____"),
            kataKunci,
            1
        );
    }

    @Override
    public String render() {
        return "Isian: " + pertanyaan;
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        return userAnswer.trim().equalsIgnoreCase(kunciJawaban);
    }
}