package controller;

import model.Exam;
import model.Model;
import model.Option;
import model.Question;
import model.QuestionBackupIOException;
import model.QuestionCreatorException;
import model.RepositoryException;
import view.BaseView;

import java.util.List;
import java.util.Set;

public class Controller {

    private Model model;
    private BaseView view;

    public Controller(Model model, BaseView view) {
        this.model = model;
        this.view = view;
    }

    public void start() {
        try {
            int loaded = model.loadRepository();
            view.showMessage("Banco de preguntas cargado: " + loaded + " preguntas.");
        } catch (RepositoryException e) {
            view.showErrorMessage("Error al cargar el banco: " + e.getMessage());
        }
        view.init();
    }

    public void end() {
        try {
            int saved = model.saveRepository();
            view.showMessage("Banco guardado. Total preguntas: " + saved);
        } catch (RepositoryException e) {
            view.showErrorMessage("Error al guardar el banco: " + e.getMessage());
        }
        view.end();
    }

    public Question createQuestion(String author, Set<String> topics, String statement, List<Option> options)
            throws RepositoryException {
        return model.createQuestion(author, topics, statement, options);
    }

    public List<Question> getAllQuestions() throws RepositoryException {
        return model.getAllQuestionsOrdered();
    }

    public List<Question> getQuestionsByTopic(String topic) throws RepositoryException {
        return model.getQuestionsByTopic(topic);
    }

    public Question getQuestionByIndex(List<Question> list, int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    public Question modifyQuestion(Question q, String author, Set<String> topics, String statement, List<Option> options)
            throws RepositoryException {
        return model.modifyQuestion(q, author, topics, statement, options);
    }

    public void deleteQuestion(Question q) throws RepositoryException {
        model.deleteQuestion(q);
    }

    public void exportQuestions(String fileName) throws QuestionBackupIOException, RepositoryException {
        model.exportQuestions(fileName);
    }

    public int importQuestions(String fileName) throws QuestionBackupIOException, RepositoryException {
        return model.importQuestions(fileName);
    }

    public boolean hasQuestionCreators() {
        return model.hasQuestionCreators();
    }

    public List<String> getQuestionCreatorsDescriptions() {
        return model.getQuestionCreatorsDescriptions();
    }

    public Question createAutomaticQuestion(int creatorIndex, String topic)
            throws QuestionCreatorException, RepositoryException {
        return model.createAutomaticQuestion(creatorIndex, topic);
    }

    public Set<String> getAvailableTopics() throws RepositoryException {
        return model.getAvailableTopics();
    }

    public Exam configureExam(String topic, int numQuestions) throws RepositoryException {
        return model.configureExam(topic, numQuestions);
    }

    public Question getCurrentExamQuestion() {
        return model.getCurrentExamQuestion();
    }

    public boolean examHasMoreQuestions() {
        return model.examHasMoreQuestions();
    }

    public String answerCurrentExamQuestion(Integer optionIndex) {
        return model.answerCurrentExamQuestion(optionIndex);
    }

    public String getExamResultSummary() {
        return model.getExamResultSummary();
    }
}
