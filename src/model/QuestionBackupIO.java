package model;

import java.util.List;

public interface QuestionBackupIO {

    void exportQuestions(List<Question> questions, String fileName) throws QuestionBackupIOException;

    List<Question> importQuestions(String fileName) throws QuestionBackupIOException;

    String getBackupIODescription();
}
