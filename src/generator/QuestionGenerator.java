package generator;

import model.Question;
import config.QuizConfig;
import java.util.List;

public interface QuestionGenerator {
    List<Question> generate(String teks, QuizConfig config);
}