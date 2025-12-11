package model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Exam implements Serializable {

    private List<Question> questions;
    private int currentIndex;
    private int correctCount;
    private int wrongCount;
    private int skippedCount;
    private Instant startTime;
    private Instant endTime;

    public Exam(List<Question> questions) {
        this.questions = new ArrayList<Question>(questions);
        this.currentIndex = 0;
        this.correctCount = 0;
        this.wrongCount = 0;
        this.skippedCount = 0;
        this.startTime = Instant.now();
    }

    public Question getCurrentQuestion() {
        if (currentIndex >= 0 && currentIndex < questions.size()) {
            return questions.get(currentIndex);
        }
        return null;
    }

    public boolean hasMoreQuestions() {
        return currentIndex < questions.size();
    }

    public String answerCurrentQuestion(Integer optionIndex) {
        Question q = getCurrentQuestion();
        if (q == null) {
            return "No hay mas preguntas.";
        }
        StringBuilder sb = new StringBuilder();
        if (optionIndex == null) {
            skippedCount++;
            sb.append("Pregunta no contestada.\n");
        } else {
            List<Option> options = q.getOptions();
            if (optionIndex < 0 || optionIndex >= options.size()) {
                skippedCount++;
                sb.append("Respuesta no valida. Se marca como no contestada.\n");
            } else {
                Option selected = options.get(optionIndex);
                if (selected.isCorrect()) {
                    correctCount++;
                    sb.append("Correcto. ");
                } else {
                    wrongCount++;
                    sb.append("Incorrecto. ");
                }
                sb.append("Razon seleccionada: ").append(selected.getRationale()).append("\n");
                for (int i = 0; i < options.size(); i++) {
                    Option o = options.get(i);
                    if (o.isCorrect()) {
                        sb.append("Respuesta correcta: ").append(o.getText())
                                .append(". Razon: ").append(o.getRationale()).append("\n");
                        break;
                    }
                }
            }
        }
        currentIndex++;
        if (!hasMoreQuestions()) {
            endTime = Instant.now();
        }
        return sb.toString();
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public long getSecondsElapsed() {
        if (startTime == null) {
            return 0;
        }
        Instant end = endTime != null ? endTime : Instant.now();
        Duration d = Duration.between(startTime, end);
        return d.getSeconds();
    }

    public double getScoreOverTen() {
        if (questions.isEmpty()) {
            return 0.0;
        }
        double perQuestion = 10.0 / questions.size();
        double score = correctCount * perQuestion - wrongCount * (perQuestion / 3.0);
        if (score < 0.0) {
            score = 0.0;
        }
        if (score > 10.0) {
            score = 10.0;
        }
        return score;
    }
}
