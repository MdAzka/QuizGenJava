package model;

public abstract class Question {
    protected String pertanyaan;
    protected String kunciJawaban;
    protected int tingkatKesulitan;

    public Question(String pertanyaan, String kunciJawaban, int tingkatKesulitan) {
        this.pertanyaan = pertanyaan;
        this.kunciJawaban = kunciJawaban;
        this.tingkatKesulitan = tingkatKesulitan;
    }

    public abstract String render();
    public abstract boolean checkAnswer(String userAnswer);

    public String getPertanyaan() { return pertanyaan; }
    public String getKunciJawaban() { return kunciJawaban; }
    public int getTingkatKesulitan() { return tingkatKesulitan; }
}
