package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BinaryRepository implements IRepository {

    private ArrayList<Question> questions;
    private File file;

    public BinaryRepository() {
        this.questions = new ArrayList<Question>();
        String home = System.getProperty("user.home");
        this.file = new File(home, "questions.bin");
    }

    public int load() throws RepositoryException {
        if (!file.exists()) {
            this.questions = new ArrayList<Question>();
            return 0;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            if (obj instanceof ArrayList) {
                this.questions = (ArrayList<Question>) obj;
            } else {
                this.questions = new ArrayList<Question>();
            }
            return this.questions.size();
        } catch (Exception e) {
            throw new RepositoryException("Error al leer fichero binario", e);
        }
    }

    public int save() throws RepositoryException {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(questions);
            oos.close();
            return questions.size();
        } catch (Exception e) {
            throw new RepositoryException("Error al guardar fichero binario", e);
        }
    }

    public Question addQuestion(Question q) throws RepositoryException {
        questions.add(q);
        return q;
    }

    public void removeQuestion(Question q) throws RepositoryException {
        questions.remove(q);
    }

    public Question modifyQuestion(Question q) throws RepositoryException {
        for (int i = 0; i < questions.size(); i++) {
            Question current = questions.get(i);
            if (current.getId().equals(q.getId())) {
                questions.set(i, q);
                return q;
            }
        }
        throw new RepositoryException("Pregunta no encontrada");
    }

    public List<Question> getAllQuestions() throws RepositoryException {
        return new ArrayList<Question>(questions);
    }
}
