package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Question implements Serializable {

    private UUID id;
    private String author;
    private HashSet<String> topics;
    private String statement;
    private List<Option> options;

    public Question(UUID id, String author, HashSet<String> topics, String statement, List<Option> options) {
        this.id = id;
        this.author = author;
        this.topics = topics;
        this.statement = statement;
        this.options = options;
    }

    public UUID getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public HashSet<String> getTopics() {
        return topics;
    }

    public String getStatement() {
        return statement;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTopics(HashSet<String> topics) {
        this.topics = topics;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }
}
