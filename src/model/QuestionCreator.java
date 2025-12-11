package model;

public interface QuestionCreator {

    Question createQuestion(String topic) throws QuestionCreatorException;

    String getQuestionCreatorDescription();
}
