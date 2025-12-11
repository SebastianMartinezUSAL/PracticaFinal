package model;

import java.io.Serializable;

public class Option implements Serializable {

    private String text;
    private String rationale;
    private boolean correct;

    public Option(String text, String rationale, boolean correct) {
        this.text = text;
        this.rationale = rationale;
        this.correct = correct;
    }

    public String getText() {
        return text;
    }

    public String getRationale() {
        return rationale;
    }

    public boolean isCorrect() {
        return correct;
    }
}
