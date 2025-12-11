package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Model {

    private IRepository repository;
    private QuestionBackupIO backupHandler;
    private ArrayList<QuestionCreator> questionCreators;
    private Exam currentExam;

    public Model(IRepository repository, QuestionBackupIO backupHandler, ArrayList<QuestionCreator> questionCreators) {
        this.repository = repository;
        this.backupHandler = backupHandler;
        this.questionCreators = questionCreators;
    }

    public int loadRepository() throws RepositoryException {
        if (repository instanceof BinaryRepository) {
            return ((BinaryRepository) repository).load();
        }
        return 0;
    }

    public int saveRepository() throws RepositoryException {
        if (repository instanceof BinaryRepository) {
            return ((BinaryRepository) repository).save();
        }
        return 0;
    }

    public Question createQuestion(String author, Set<String> topics, String statement, List<Option> options)
            throws RepositoryException {
        HashSet<String> topicSet = new HashSet<String>();
        for (String t : topics) {
            topicSet.add(t.toUpperCase());
        }
        Question q = new Question(UUID.randomUUID(), author, topicSet, statement, options);
        return repository.addQuestion(q);
    }

    public List<Question> getAllQuestionsOrdered() throws RepositoryException {
        List<Question> list = repository.getAllQuestions();
        Collections.sort(list, new Comparator<Question>() {
            public int compare(Question o1, Question o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return list;
    }

    public List<Question> getQuestionsByTopic(String topic) throws RepositoryException {
        String t = topic.toUpperCase();
        List<Question> list = repository.getAllQuestions();
        List<Question> filtered = new ArrayList<Question>();
        for (int i = 0; i < list.size(); i++) {
            Question q = list.get(i);
            if (q.getTopics().contains(t)) {
                filtered.add(q);
            }
        }
        Collections.sort(filtered, new Comparator<Question>() {
            public int compare(Question o1, Question o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return filtered;
    }

    public Question modifyQuestion(Question original, String author, Set<String> topics, String statement,
                                   List<Option> options) throws RepositoryException {
        HashSet<String> topicSet = new HashSet<String>();
        for (String t : topics) {
            topicSet.add(t.toUpperCase());
        }
        Question updated = new Question(original.getId(), author, topicSet, statement, options);
        return repository.modifyQuestion(updated);
    }

    public void deleteQuestion(Question q) throws RepositoryException {
        repository.removeQuestion(q);
    }

    public void exportQuestions(String fileName) throws QuestionBackupIOException, RepositoryException {
        List<Question> list = repository.getAllQuestions();
        backupHandler.exportQuestions(list, fileName);
    }

    public int importQuestions(String fileName) throws QuestionBackupIOException, RepositoryException {
        List<Question> existing = repository.getAllQuestions();
        HashSet<UUID> ids = new HashSet<UUID>();
        for (int i = 0; i < existing.size(); i++) {
            ids.add(existing.get(i).getId());
        }
        List<Question> imported = backupHandler.importQuestions(fileName);
        int added = 0;
        for (int i = 0; i < imported.size(); i++) {
            Question q = imported.get(i);
            if (!ids.contains(q.getId())) {
                repository.addQuestion(q);
                ids.add(q.getId());
                added++;
            }
        }
        return added;
    }

    public boolean hasQuestionCreators() {
        return questionCreators != null && !questionCreators.isEmpty();
    }

    public List<String> getQuestionCreatorsDescriptions() {
        List<String> list = new ArrayList<String>();
        if (questionCreators == null) {
            return list;
        }
        for (int i = 0; i < questionCreators.size(); i++) {
            list.add(questionCreators.get(i).getQuestionCreatorDescription());
        }
        return list;
    }

    public Question createAutomaticQuestion(int creatorIndex, String topic)
            throws QuestionCreatorException {
        if (questionCreators == null || creatorIndex < 0 || creatorIndex >= questionCreators.size()) {
            throw new QuestionCreatorException("Question creator no disponible");
        }
        QuestionCreator creator = questionCreators.get(creatorIndex);
        return creator.createQuestion(topic);
    }

    public Set<String> getAvailableTopics() throws RepositoryException {
        List<Question> list = repository.getAllQuestions();
        HashSet<String> topics = new HashSet<String>();
        for (int i = 0; i < list.size(); i++) {
            topics.addAll(list.get(i).getTopics());
        }
        return topics;
    }

    public Exam configureExam(String topic, int numQuestions) throws RepositoryException {
        List<Question> base;
        if ("ALL".equals(topic)) {
            base = getAllQuestionsOrdered();
        } else {
            base = getQuestionsByTopic(topic);
        }
        if (numQuestions <= 0 || numQuestions > base.size()) {
            this.currentExam = new Exam(base);
        } else {
            List<Question> selected = new ArrayList<Question>();
            for (int i = 0; i < numQuestions && i < base.size(); i++) {
                selected.add(base.get(i));
            }
            this.currentExam = new Exam(selected);
        }
        return this.currentExam;
    }

    public Question getCurrentExamQuestion() {
        if (currentExam == null) {
            return null;
        }
        return currentExam.getCurrentQuestion();
    }

    public boolean examHasMoreQuestions() {
        if (currentExam == null) {
            return false;
        }
        return currentExam.hasMoreQuestions();
    }

    public String answerCurrentExamQuestion(Integer optionIndex) {
        if (currentExam == null) {
            return "No hay examen configurado.";
        }
        return currentExam.answerCurrentQuestion(optionIndex);
    }

    public String getExamResultSummary() {
        if (currentExam == null) {
            return "No hay examen.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Resultados del examen ===\n");
        sb.append("Preguntas correctas: ").append(currentExam.getCorrectCount()).append("\n");
        sb.append("Preguntas incorrectas: ").append(currentExam.getWrongCount()).append("\n");
        sb.append("Preguntas no contestadas: ").append(currentExam.getSkippedCount()).append("\n");
        sb.append("Nota sobre 10: ").append(String.format("%.2f", currentExam.getScoreOverTen())).append("\n");
        sb.append("Tiempo empleado (segundos): ").append(currentExam.getSecondsElapsed()).append("\n");
        return sb.toString();
    }
}
