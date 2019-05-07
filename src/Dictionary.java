import scanner.FastScanner;

import java.io.*;
import java.util.*;

public class Dictionary {
    private Map<String, List<String>> dict = new TreeMap<>();
    private Map<String, String> sample = new TreeMap<>();
    private List<String> words = new ArrayList<>();
    private final String type, lineSeparate = System.lineSeparator();

    public Dictionary(String type) {
        this.type = type;
        File dictFile = new File("dict/" + getTypeHeadWord() + "Dict.ota");
        try (FastScanner scanner = new FastScanner(dictFile)) {
            // reading all words from file "<type>Dict.ota"
            while (scanner.hasNext()) {
                // reading word
                String currWord = scanner.next();

                // reading some descriptions for the word
                List<String> descriptions = new ArrayList<>();
                while (true) {
                    String currDescription = scanner.next();
                    if (lineSeparate.contains(currDescription)) {
                        break;
                    }

                    descriptions.add(currDescription.trim());
                }
                // read all descriptions for the word

                // adding word and its descriptions in dictionary
                dict.put(currWord, descriptions);
            }
            // all words and its descriptions are added in dictionary
        } catch (IOException e) {
            // attempt to connect the file was unsuccessful
            System.err.println("File '" + dictFile + "' not found.");
            e.printStackTrace();
            System.exit(0);
        }

        // initialize list of words
        for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
            words.add(entry.getKey());
        }

        if (type.equals("main")) {
            return;
        }

        for (String word : words) {
            sample.put(word, "-");
        }

        File sampleFile = new File("sample/" + getTypeHeadWord() + "Sample.ota");
        try (FastScanner scanner = new FastScanner(sampleFile)) {
            while (scanner.hasNextNoLineSeparate()) {
                String currWord = scanner.nextNoLineSeparate(), currSentence = scanner.next();
                assert !lineSeparate.contains(currSentence) : "Sentence is lineSeparator";
                sample.put(currWord, currSentence.trim());
            }
        } catch (IOException e) {
            // attempt to connect the file was unsuccessful
            System.err.println("File '" + dictFile + "' not found.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void put(String key, List<String> value) {
        dict.put(key, value);
        words.add(key);
    }

    public void put(String word, String sentence) {
        char firstLetter = sentence.charAt(0);
        sample.put(word, Character.toUpperCase(firstLetter) + sentence.substring(1));
    }

    public String getType() {
        return type;
    }

    public String getClue(String word) {
        String sentence = sample.get(word);
        if ("-".equals(sentence)) {
            return "nothing";
        }
        return sentence;
    }

    public int size() {
        return dict.size();
    }

    public List<String> get(String word) {
        return dict.get(word);
    }

    public List<String> remove(String word) {
        sample.remove(word);
        return dict.remove(word);
    }

    public List<String> getWords() {
        return words;
    }

    public String getTypeHeadWord() {
        return this.type.substring(0, 1).toUpperCase() + this.type.substring(1);
    }

    public void update(String type) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("dict/" + type + "Dict.ota"))) {
            for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
                writer.write(entry.getKey() + ": ");

                writer.write(entry.getValue().get(0));
                for (int i = 1; i < entry.getValue().size(); ++i) {
                    writer.write("; " + entry.getValue().get(i));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating file 'dict/" + type + "Dict.ota'");
            e.printStackTrace();
            System.exit(0);
        }

        if (type.equals("Main")) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sample/" + type + "Sample.ota"))) {
            for (Map.Entry<String, String> entry : sample.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating file 'sample/" + type + "Sample.ota'");
            e.printStackTrace();
            System.exit(0);
        }
    }
}