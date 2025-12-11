package model;

import java.io.*;
import java.util.*;

public class JSONQuestionBackupIO implements QuestionBackupIO {

    @Override
    public void exportQuestions(List<Question> questions, String fileName) throws QuestionBackupIOException {
        try {
            if (!fileName.endsWith(".json")) fileName += ".json";

            File file = new File(fileName);
            FileWriter fw = new FileWriter(file);
            fw.write("[\n");

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);

                fw.write("  {\n");
                fw.write("    \"id\": \"" + q.getId().toString() + "\",\n");
                fw.write("    \"author\": \"" + q.getAuthor() + "\",\n");

                fw.write("    \"topics\": [");
                int k = 0;
                for (String t : q.getTopics()) {
                    fw.write("\"" + t + "\"");
                    if (k < q.getTopics().size() - 1) fw.write(", ");
                    k++;
                }
                fw.write("],\n");

                fw.write("    \"statement\": \"" + q.getStatement() + "\",\n");

                fw.write("    \"options\": [\n");
                for (int j = 0; j < q.getOptions().size(); j++) {
                    Option o = q.getOptions().get(j);
                    fw.write("      {\n");
                    fw.write("        \"text\": \"" + o.getText() + "\",\n");
                    fw.write("        \"correct\": " + (o.isCorrect() ? "true" : "false") + ",\n");
                    fw.write("        \"rationale\": \"" + o.getRationale() + "\"\n");
                    fw.write("      }");
                    if (j < q.getOptions().size() - 1) fw.write(",");
                    fw.write("\n");
                }
                fw.write("    ]\n");
                fw.write("  }");

                if (i < questions.size() - 1) fw.write(",");
                fw.write("\n");
            }

            fw.write("]");
            fw.close();

        } catch (Exception e) {
            throw new QuestionBackupIOException("Error exportando JSON", e);
        }
    }

    @Override
    public List<Question> importQuestions(String fileName) throws QuestionBackupIOException {
        try {
            if (!fileName.endsWith(".json")) fileName += ".json";

            File file = findFileAnywhere(fileName);

            if (file == null || !file.exists()) {
                throw new FileNotFoundException("No se encontró el archivo en el sistema: " + fileName);
            }

            System.out.println("Leyendo JSON desde: " + file.getAbsolutePath());

            String json = readFile(file);

            return parseQuestions(json);

        } catch (FileNotFoundException fnf) {
            throw new QuestionBackupIOException(
                    "No se encontró el fichero JSON: " + fnf.getMessage(), fnf
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuestionBackupIOException(
                    "Error interno importando JSON: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public String getBackupIODescription() {
        return "JSON Backup IO";
    }

    private String readFile(File file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line.trim());
        }
        br.close();
        return sb.toString();
    }

    private List<Question> parseQuestions(String json) {
        List<Question> list = new ArrayList<>();

        if (json == null) return list;
        json = json.trim();
        if (json.length() < 2 || json.charAt(0) != '[' || json.charAt(json.length() - 1) != ']') {
            return list;
        }

        String inner = json.substring(1, json.length() - 1).trim();
        if (inner.isEmpty()) {
            return list;
        }

        List<String> blocks = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    blocks.add(inner.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        for (String block : blocks) {
            String id = extract(block, "\"id\": \"", "\"");
            String author = extract(block, "\"author\": \"", "\"");
            String statement = extract(block, "\"statement\": \"", "\"");

            String topicsRaw = extract(block, "\"topics\": [", "]");
            HashSet<String> topics = new HashSet<>();
            if (!topicsRaw.isEmpty()) {
                String[] ts = topicsRaw.split(",");
                for (String t : ts) {
                    String v = t.replace("\"", "").trim();
                    if (!v.isEmpty()) topics.add(v);
                }
            }

            String optsRaw = extract(block, "\"options\": [", "]");
            List<Option> options = new ArrayList<>();
            if (!optsRaw.isEmpty()) {
                String tmp = optsRaw.trim();
                String[] optBlocks = tmp.split("\\},\\{");
                for (String ob : optBlocks) {
                    String o = ob.trim();
                    if (!o.startsWith("{")) o = "{" + o;
                    if (!o.endsWith("}")) o = o + "}";
                    o = o.replace("{", "").replace("}", "");

                    String text = extract(o, "\"text\": \"", "\"");
                    String rationale = extract(o, "\"rationale\": \"", "\"");
                    boolean correct = o.contains("\"correct\": true");

                    options.add(new Option(text, rationale, correct));
                }
            }

            Question q = new Question(
                    java.util.UUID.fromString(id),
                    author,
                    topics,
                    statement,
                    options
            );

            list.add(q);
        }

        return list;
    }

    private String extract(String text, String start, String end) {
        int a = text.indexOf(start);
        if (a == -1) return "";
        a += start.length();
        int b = text.indexOf(end, a);
        if (b == -1) return "";
        return text.substring(a, b);
    }

    private File findFileAnywhere(String fileName) {
        File f = new File(fileName);
        if (f.isAbsolute() && f.exists()) return f;

        if (f.exists()) return f;

        String homePath = System.getProperty("user.home");
        File home = new File(homePath);
        File found = searchRecursive(home, fileName);
        if (found != null) return found;

        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                found = searchRecursive(root, fileName);
                if (found != null) return found;
            }
        }

        return null;
    }

    private File searchRecursive(File dir, String fileName) {
        if (dir == null) return null;
        if (!dir.exists()) return null;
        if (!dir.isDirectory()) return null;

        File[] files;
        try {
            files = dir.listFiles();
        } catch (SecurityException se) {
            return null;
        }

        if (files == null) return null;

        for (File f : files) {
            if (f.isFile() && f.getName().equals(fileName)) {
                return f;
            }
        }

        for (File f : files) {
            if (f.isDirectory()) {
                File found = searchRecursive(f, fileName);
                if (found != null) return found;
            }
        }

        return null;
    }
}
