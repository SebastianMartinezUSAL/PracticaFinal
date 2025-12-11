package view;

import model.Exam;
import model.Option;
import model.Question;
import model.QuestionBackupIOException;
import model.QuestionCreatorException;
import model.RepositoryException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class InteractiveView extends BaseView {

    private Scanner scanner;

    public InteractiveView() {
        this.scanner = new Scanner(System.in);
    }

    public void init() {
        boolean exit = false;
        while (!exit) {
            showMainMenu();
            int option = readInt("Elige una opcion: ");
            switch (option) {
                case 1:
                    menuCrud();
                    break;
                case 2:
                    menuBackup();
                    break;
                case 3:
                    menuAutomaticQuestion();
                    break;
                case 4:
                    menuExam();
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    showErrorMessage("Opcion no valida.");
            }
        }
        controller.end();
    }

    private void showMainMenu() {
        System.out.println();
        System.out.println("==== Examinator 3000 ====");
        System.out.println("1. Gestion de preguntas (CRUD)");
        System.out.println("2. Exportar / Importar preguntas (JSON)");
        System.out.println("3. Crear pregunta automatica");
        System.out.println("4. Modo examen");
        System.out.println("0. Salir");
    }

    private void menuCrud() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("== CRUD Preguntas ==");
            System.out.println("1. Crear nueva pregunta");
            System.out.println("2. Listar todas las preguntas");
            System.out.println("3. Listar preguntas por tema");
            System.out.println("0. Volver");
            int option = readInt("Elige una opcion: ");
            switch (option) {
                case 1:
                    createQuestionFlow();
                    break;
                case 2:
                    listQuestionsFlow(false);
                    break;
                case 3:
                    listQuestionsFlow(true);
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    showErrorMessage("Opcion no valida.");
            }
        }
    }

    private void createQuestionFlow() {
        try {
            System.out.println("--- Crear pregunta ---");
            String author = readLine("Autor: ");
            String topicsInput = readLine("Temas (separados por coma): ");
            Set<String> topics = parseTopics(topicsInput);
            String statement = readLine("Enunciado: ");
            List<Option> options = new ArrayList<Option>();
            int correctCount = 0;
            for (int i = 0; i < 4; i++) {
                System.out.println("Opcion " + (i + 1));
                String text = readLine("Texto: ");
                String rationale = readLine("Razon: ");
                boolean correct = readBoolean("Es correcta? (s/n): ");
                if (correct) {
                    correctCount++;
                }
                options.add(new Option(text, rationale, correct));
            }
            if (correctCount != 1) {
                showErrorMessage("Debe haber exactamente una opcion correcta.");
                return;
            }
            Question q = controller.createQuestion(author, topics, statement, options);
            showMessage("Pregunta creada con id: " + q.getId());
        } catch (RepositoryException e) {
            showErrorMessage("Error al crear pregunta: " + e.getMessage());
        }
    }

    private void listQuestionsFlow(boolean byTopic) {
        try {
            List<Question> list;
            if (byTopic) {
                String topic = readLine("Tema a filtrar: ").toUpperCase().trim();
                list = controller.getQuestionsByTopic(topic);
            } else {
                list = controller.getAllQuestions();
            }
            if (list.isEmpty()) {
                showMessage("No hay preguntas para mostrar.");
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                Question q = list.get(i);
                System.out.println((i + 1) + ". " + q.getStatement());
            }
            int index = readInt("Elige numero de pregunta para ver detalle (0 para volver): ");
            if (index <= 0 || index > list.size()) {
                return;
            }
            Question selected = controller.getQuestionByIndex(list, index - 1);
            showQuestionDetail(selected);
            menuQuestionDetail(selected);
        } catch (RepositoryException e) {
            showErrorMessage("Error al listar preguntas: " + e.getMessage());
        }
    }

    private void showQuestionDetail(Question q) {
        System.out.println();
        System.out.println("Id: " + q.getId());
        System.out.println("Autor: " + q.getAuthor());
        System.out.println("Temas: " + q.getTopics());
        System.out.println("Enunciado: " + q.getStatement());
        List<Option> options = q.getOptions();
        for (int i = 0; i < options.size(); i++) {
            Option o = options.get(i);
            System.out.println((i + 1) + ") " + o.getText() + " [" + (o.isCorrect() ? "correcta" : "incorrecta") + "]");
            System.out.println("   Razon: " + o.getRationale());
        }
    }

    private void menuQuestionDetail(Question q) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("1. Modificar pregunta");
            System.out.println("2. Eliminar pregunta");
            System.out.println("0. Volver");
            int option = readInt("Elige una opcion: ");
            switch (option) {
                case 1:
                    modifyQuestionFlow(q);
                    back = true;
                    break;
                case 2:
                    deleteQuestionFlow(q);
                    back = true;
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    showErrorMessage("Opcion no valida.");
            }
        }
    }

    private void modifyQuestionFlow(Question q) {
        try {
            System.out.println("--- Modificar pregunta ---");
            String author = readLine("Autor (" + q.getAuthor() + "): ");
            if (author == null || author.trim().isEmpty()) {
                author = q.getAuthor();
            }
            String topicsInput = readLine("Temas separados por coma " + q.getTopics() + ": ");
            Set<String> topics;
            if (topicsInput == null || topicsInput.trim().isEmpty()) {
                topics = q.getTopics();
            } else {
                topics = parseTopics(topicsInput);
            }
            String statement = readLine("Enunciado (" + q.getStatement() + "): ");
            if (statement == null || statement.trim().isEmpty()) {
                statement = q.getStatement();
            }
            List<Option> newOptions = new ArrayList<Option>();
            int correctCount = 0;
            for (int i = 0; i < 4; i++) {
                Option old = q.getOptions().get(i);
                System.out.println("Opcion " + (i + 1));
                String text = readLine("Texto (" + old.getText() + "): ");
                if (text == null || text.trim().isEmpty()) {
                    text = old.getText();
                }
                String rationale = readLine("Razon (" + old.getRationale() + "): ");
                if (rationale == null || rationale.trim().isEmpty()) {
                    rationale = old.getRationale();
                }
                String defaultCorrect = old.isCorrect() ? "s" : "n";
                String correctStr = readLine("Es correcta? (s/n, actual " + defaultCorrect + "): ");
                boolean correct;
                if (correctStr == null || correctStr.trim().isEmpty()) {
                    correct = old.isCorrect();
                } else {
                    correct = correctStr.trim().toLowerCase().startsWith("s");
                }
                if (correct) {
                    correctCount++;
                }
                newOptions.add(new Option(text, rationale, correct));
            }
            if (correctCount != 1) {
                showErrorMessage("Debe haber exactamente una opcion correcta.");
                return;
            }
            controller.modifyQuestion(q, author, topics, statement, newOptions);
            showMessage("Pregunta modificada.");
        } catch (RepositoryException e) {
            showErrorMessage("Error al modificar pregunta: " + e.getMessage());
        }
    }

    private void deleteQuestionFlow(Question q) {
        try {
            boolean confirm = readBoolean("Seguro que deseas eliminar la pregunta? (s/n): ");
            if (!confirm) {
                return;
            }
            controller.deleteQuestion(q);
            showMessage("Pregunta eliminada.");
        } catch (RepositoryException e) {
            showErrorMessage("Error al eliminar pregunta: " + e.getMessage());
        }
    }

    private void menuBackup() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("== Backup JSON ==");
            System.out.println("1. Exportar preguntas a JSON");
            System.out.println("2. Importar preguntas desde JSON");
            System.out.println("0. Volver");
            int option = readInt("Elige una opcion: ");
            switch (option) {
                case 1:
                    exportFlow();
                    break;
                case 2:
                    importFlow();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    showErrorMessage("Opcion no valida.");
            }
        }
    }

    private void exportFlow() {
        try {
            String fileName = readLine("Nombre de fichero JSON (sin ruta): ");
            controller.exportQuestions(fileName);
            showMessage("Exportacion completada.");
        } catch (QuestionBackupIOException e) {
            showErrorMessage("Error en exportacion: " + e.getMessage());
        } catch (RepositoryException e) {
            showErrorMessage("Error al leer preguntas: " + e.getMessage());
        }
    }

    private void importFlow() {
        try {
            String fileName = readLine("Nombre de fichero JSON (sin ruta): ");
            int imported = controller.importQuestions(fileName);
            showMessage("Importadas " + imported + " preguntas nuevas.");
        } catch (QuestionBackupIOException e) {
            showErrorMessage("Error en importacion: " + e.getMessage());
        } catch (RepositoryException e) {
            showErrorMessage("Error al importar preguntas: " + e.getMessage());
        }
    }

    private void menuAutomaticQuestion() {
        if (!controller.hasQuestionCreators()) {
            showMessage("No hay question creators disponibles.");
            return;
        }
        List<String> descriptions = controller.getQuestionCreatorsDescriptions();
        System.out.println();
        System.out.println("== Creacion automatica de preguntas ==");
        for (int i = 0; i < descriptions.size(); i++) {
            System.out.println((i + 1) + ". " + descriptions.get(i));
        }
        int option = readInt("Elige un creador (0 para volver): ");
        if (option <= 0 || option > descriptions.size()) {
            return;
        }
        String topic = readLine("Tema para la pregunta: ");
        try {
            Question q = controller.createAutomaticQuestion(option - 1, topic);
            showMessage("Pregunta generada:");
            showQuestionDetail(q);
            boolean add = readBoolean("Anadir al banco de preguntas? (s/n): ");
            if (add) {
                controller.createQuestion(q.getAuthor(), q.getTopics(), q.getStatement(), q.getOptions());
                showMessage("Pregunta automatica anadida al banco.");
            }
        } catch (QuestionCreatorException e) {
            showErrorMessage("Error al generar pregunta: " + e.getMessage());
        } catch (RepositoryException e) {
            showErrorMessage("Error al guardar pregunta: " + e.getMessage());
        }
    }

    private void menuExam() {
        try {
            System.out.println();
            System.out.println("== Modo examen ==");
            Set<String> topics = controller.getAvailableTopics();
            if (topics.isEmpty()) {
                showMessage("No hay preguntas para hacer examen.");
                return;
            }
            List<String> topicList = new ArrayList<String>(topics);
            for (int i = 0; i < topicList.size(); i++) {
                System.out.println((i + 1) + ". " + topicList.get(i));
            }
            int allIndex = topicList.size() + 1;
            System.out.println(allIndex + ". TODOS");
            int topicOption = readInt("Elige tema: ");
            String selectedTopic;
            if (topicOption == allIndex) {
                selectedTopic = "ALL";
            } else if (topicOption > 0 && topicOption <= topicList.size()) {
                selectedTopic = topicList.get(topicOption - 1);
            } else {
                showErrorMessage("Opcion de tema no valida.");
                return;
            }
            int maxQuestions = controller.configureExam(selectedTopic, 0).getTotalQuestions();
            int num = readInt("Numero de preguntas (1.." + maxQuestions + "): ");
            if (num < 1 || num > maxQuestions) {
                showErrorMessage("Numero no valido.");
                return;
            }
            Exam exam = controller.configureExam(selectedTopic, num);
            showMessage("Examen configurado con " + exam.getTotalQuestions() + " preguntas.");

            while (controller.examHasMoreQuestions()) {
                Question q = controller.getCurrentExamQuestion();
                System.out.println();
                System.out.println("Pregunta:");
                System.out.println(q.getStatement());
                List<Option> options = q.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    System.out.println((i + 1) + ") " + options.get(i).getText());
                }
                System.out.println("0) Saltar sin responder");
                int opt = readInt("Respuesta: ");
                Integer answerIndex = null;
                if (opt > 0 && opt <= options.size()) {
                    answerIndex = opt - 1;
                }
                String feedback = controller.answerCurrentExamQuestion(answerIndex);
                System.out.println(feedback);
            }
            String summary = controller.getExamResultSummary();
            System.out.println(summary);

        } catch (RepositoryException e) {
            showErrorMessage("Error en modo examen: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String line = scanner.nextLine();
                return Integer.parseInt(line.trim());
            } catch (Exception e) {
                System.out.println("Introduce un numero valido.");
            }
        }
    }

    private boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null) {
                continue;
            }
            line = line.trim().toLowerCase();
            if (line.startsWith("s")) {
                return true;
            }
            if (line.startsWith("n")) {
                return false;
            }
            System.out.println("Responde s o n.");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private Set<String> parseTopics(String input) {
        Set<String> set = new HashSet<String>();
        if (input == null) {
            return set;
        }
        String[] parts = input.split(",");
        for (String p : parts) {
            String t = p.trim().toUpperCase();
            if (!t.isEmpty()) {
                set.add(t);
            }
        }
        return set;
    }

    public void showMessage(String msg) {
        System.out.println(msg);
    }

    public void showErrorMessage(String msg) {
        System.err.println(msg);
    }

    public void end() {
        System.out.println("Saliendo de Examinator 3000. Hasta luego.");
    }
}
