import scanner.FastScanner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Dictionary {
    private Map<String, List<String>> dict = new TreeMap<>();
    private List<String> words = new ArrayList<>();
    private final String type, lineSeparate = System.lineSeparator();

    public Dictionary(File f, String type) {
        this.type = type;
        try (FastScanner scanner = new FastScanner(f)) {
            // reading all words from file "MainDictionary.ota"
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
            System.err.println("File '" + f + "' not found.");
            e.printStackTrace();
            System.exit(0);
        }

        // initialize list of words
        for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
            words.add(entry.getKey());
        }
    }

    public void put(String key, List<String> value) {
        dict.put(key, value);
    }

    public String getType() {
        return this.type;
    }

    public int size() {
        return dict.size();
    }

    public List<String> get(String word) {
        return dict.get(word);
    }

    public String getFirst() {
        return dict.entrySet().iterator().next().getValue().get(0);
    }

    public List<String> remove(String word) {
        return dict.remove(word);
    }

    public List<String> getWords() {
        return words;
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return dict.entrySet();
    }

    public void update(File f) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
                writer.write(entry.getKey() + ": ");

                writer.write(entry.getValue().get(0));
                for (int i = 1; i < entry.getValue().size(); ++i) {
                    writer.write(", " + entry.getValue().get(i));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating file '" + f + "'");
            e.printStackTrace();
            System.exit(0);
        }
    }
}