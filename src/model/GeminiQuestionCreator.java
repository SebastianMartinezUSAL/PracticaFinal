package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class GeminiQuestionCreator implements QuestionCreator {

    private String modelId;

    public GeminiQuestionCreator(String modelId) {
        this.modelId = modelId;
    }

    public Question createQuestion(String topic) throws QuestionCreatorException {
        try {
            HashSet<String> topics = new HashSet<String>();
            topics.add(topic.toUpperCase());
            String author = "Gemini-" + modelId;
            String statement = "Pregunta generada automaticamente sobre el tema: " + topic;
            List<Option> options = new ArrayList<Option>();
            options.add(new Option("Opcion 1", "Razon de ejemplo 1", true));
            options.add(new Option("Opcion 2", "Razon de ejemplo 2", false));
            options.add(new Option("Opcion 3", "Razon de ejemplo 3", false));
            options.add(new Option("Opcion 4", "Razon de ejemplo 4", false));
            return new Question(UUID.randomUUID(), author, topics, statement, options);
        } catch (Exception e) {
            throw new QuestionCreatorException("Error creando pregunta automatica", e);
        }
    }

    public String getQuestionCreatorDescription() {
        return "Gemini Question Creator (" + modelId + ")";
    }
}
