import controller.Controller;
import model.BinaryRepository;
import model.JSONQuestionBackupIO;
import model.Model;
import model.QuestionBackupIO;
import model.QuestionCreator;
import model.GeminiQuestionCreator;
import model.IRepository;
import view.BaseView;
import view.InteractiveView;

import java.util.ArrayList;

public class App {

    public static void main(String[] args) {
        IRepository repository = new BinaryRepository();
        QuestionBackupIO backupIO = new JSONQuestionBackupIO();
        ArrayList<QuestionCreator> creators = new ArrayList<QuestionCreator>();

        String questionCreatorModelId = "Modelo demo local";
        QuestionCreator creator = new GeminiQuestionCreator(questionCreatorModelId);
        creators.add(creator);

        Model model = new Model(repository, backupIO, creators);
        BaseView view = new InteractiveView();
        Controller controller = new Controller(model, view);
        view.setController(controller);

        controller.start();
    }
}


