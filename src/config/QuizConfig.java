package config;

public class QuizConfig {
    private int jumlahFillInBlank;
    private int jumlahTrueFalse;
    private int jumlahMultipleChoice;
    private int jumlahShortAnswer;
    private int tingkatKesulitan; // 1=mudah, 2=sedang, 3=sulit

    // Constructor default
    public QuizConfig() {
        this.jumlahFillInBlank = 3;
        this.jumlahTrueFalse = 3;
        this.jumlahMultipleChoice = 3;
        this.jumlahShortAnswer = 2;
        this.tingkatKesulitan = 1;
    }

    // Constructor custom
    public QuizConfig(int fillInBlank, int trueFalse, int multipleChoice, int shortAnswer, int kesulitan) {
        this.jumlahFillInBlank = fillInBlank;
        this.jumlahTrueFalse = trueFalse;
        this.jumlahMultipleChoice = multipleChoice;
        this.jumlahShortAnswer = shortAnswer;
        this.tingkatKesulitan = kesulitan;
    }

    // Getters
    public int getJumlahFillInBlank() { return jumlahFillInBlank; }
    public int getJumlahTrueFalse() { return jumlahTrueFalse; }
    public int getJumlahMultipleChoice() { return jumlahMultipleChoice; }
    public int getJumlahShortAnswer() { return jumlahShortAnswer; }
    public int getTingkatKesulitan() { return tingkatKesulitan; }

    // Setters
    public void setJumlahFillInBlank(int n) { this.jumlahFillInBlank = n; }
    public void setJumlahTrueFalse(int n) { this.jumlahTrueFalse = n; }
    public void setJumlahMultipleChoice(int n) { this.jumlahMultipleChoice = n; }
    public void setJumlahShortAnswer(int n) { this.jumlahShortAnswer = n; }
    public void setTingkatKesulitan(int n) { this.tingkatKesulitan = n; }
}