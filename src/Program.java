import FastScanner.FastScanner;

import java.io.*;
import java.util.*;

class Program {
    // dictionary - pair of word and it meanings
    private Map<String, String> mainDict = new TreeMap<>();
    private Map<String, List<String>> nounDict = new TreeMap<>();
    private Map<String, List<String>> verbDict = new TreeMap<>();
    private Map<String, List<String>> adjDict = new TreeMap<>();
    private Map<String, List<String>> advDict = new TreeMap<>();

    // words - list of all words have in dictionary
    private List<String> nounWords = new ArrayList<>();
    private List<String> verbWords = new ArrayList<>();
    private List<String> adjWords = new ArrayList<>();
    private List<String> advWords = new ArrayList<>();

    // users - list of logged in users
    private Map<String, List<Object>> users = new TreeMap<>();

    // username - String, name of current user.
    // If it's "Anonymous" then user hasn't registered and it doesn't changes rating
    // userStatistics - list of statistics of the current user
    private String username;
    private List<Object> userStatistics;

    // onlineScanner - scanner with System.in input stream
    // interact with user
    private FastScanner onlineScanner = new FastScanner();

    // ranking and naming - pair of arrays needed to name current rating
    private double[] ranking = {1200.0 / 3800.0, 1400.0 / 3800.0, 1600.0 / 3800.0, 1900.0 / 3800.0,
            2200.0 / 3800.0, 2600.0 / 3800.0, 2900.0 / 3800.0, 3300.0 / 3800.0, 3600.0 / 3800.0};
    private String[] naming = {"Newbie", "Pupil", "Specialist", "Expert",
            "Candidate master", "Master", "International master", "Grandmaster", "International grandmaster", "Legendary grandmaster"};


    /**
     * @param s = String
     * @return s without whitespaces in suffix
     */
    private String removeWhiteSpaces(String s) {
        int leftPos = 0;
        while (leftPos < s.length() && Character.isWhitespace(s.charAt(leftPos))) {
            ++leftPos;
        }
        int rightPos = s.length() - 1;
        while (rightPos > -1 && Character.isWhitespace(s.charAt(rightPos))) {
            --rightPos;
        }
        ++rightPos;

        return s.substring(leftPos, rightPos);
    }

    /**
     * reads all words in file "fileName"
     * and initialize dictionary to be get ready of adding new words
     * or write words and they descriptions
     *
     * @param dictionary - one of four: noun, verb, adjective, adverb; type: Map<Str, List<Str>>
     * @param words      - one of four: noun, verb, adjective, adverb; type: List<Str>
     * @param fileName   - one of four: Noun, Verb, Adjective, Adverb + 'Dict.ota'
     */
    private void initializeDictionary(Map<String, List<String>> dictionary, List<String> words, String fileName) {
        // try to connect to file "MainDictionary.ota" and initialize dictionary
        try (FastScanner scanner = new FastScanner(new File(fileName))) {
            // reading all words from file "MainDictionary.ota"
            while (scanner.hasNext()) {
                // reading word
                String currWord = scanner.next();

                // reading some descriptions for the word
                List<String> descriptions = new ArrayList<>();
                while (true) {
                    String currDescription = scanner.next();
                    if (currDescription.equals(System.lineSeparator())) {
                        break;
                    }

                    descriptions.add(removeWhiteSpaces(currDescription));
                }
                // read all descriptions for the word

                // adding word and its descriptions in dictionary
                dictionary.put(currWord, descriptions);
            }
            // all words and its descriptions are added in dictionary
        } catch (IOException e) {
            // attempt to connect the file was unsuccessful
            System.err.println("File '" + fileName + "' not found.");
            e.printStackTrace();
            System.exit(0);
        }

        // initialize list of words
        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            words.add(entry.getKey());
        }
    }

    /**
     * initializes all dictionaries: main, noun, verb, adjective, adverb
     */
    private void initializeDictionaries() {
        try (FastScanner scanner = new FastScanner(new File("MainDictionary.ota"))) {
            // reading all words from file "MainDictionary.ota"
            while (scanner.hasNext()) {
                // reading word
                String currWord = scanner.next();

                // reading some descriptions for the word
                String description = "";
                while (scanner.hasNext()) {
                    String tmp = scanner.next();
                    if (tmp.equals(System.lineSeparator())) {
                        break;
                    }

                    description = tmp;
                }

                // adding word and it description in dictionary
                mainDict.put(currWord, removeWhiteSpaces(description));
            }
            // all words and its descriptions are added in dictionary
        } catch (IOException e) {
            // attempt to connect the file was unsuccessful
            System.err.println("File 'MainDictionary.ota' not found.");
            e.printStackTrace();
            System.exit(0);
        }

        initializeDictionary(nounDict, nounWords, "NounDict.ota");
        initializeDictionary(verbDict, verbWords, "VerbDict.ota");
        initializeDictionary(adjDict, adjWords, "AdjectiveDict.ota");
        initializeDictionary(advDict, advWords, "AdverbDict.ota");
    }

    /**
     * use this method to run this program
     */
    public void run() {
        initializeDictionaries();
        userLogin();
        waitForQuery();
        updateFiles();
    }

    /**
     * reads all users from respective file.
     * each user is recorded as like this: "-Name-: -correct answers-, 'Total-"
     * -Name- is String
     * -correct answers- and -Total- is Integer
     */
    private void readUsers() {
        try (FastScanner scanner = new FastScanner(new File("Users.ota"))) {
            while (scanner.hasNext()) {
                String user = removeWhiteSpaces(scanner.next());
                List<Object> status = new ArrayList<>();
                while (scanner.hasNext()) {
                    String currStatus = scanner.next();
                    if (currStatus.equals(System.lineSeparator())) {
                        break;
                    }

                    currStatus = removeWhiteSpaces(currStatus);
                    boolean isNumber = true;
                    for (int i = 0; i < currStatus.length(); ++i) {
                        if (!(Character.isDigit(currStatus.charAt(i)) || currStatus.charAt(i) == '-')) {
                            isNumber = false;
                        }
                    }

                    if (isNumber) {
                        status.add(Integer.parseInt(currStatus));
                    } else {
                        status.add(currStatus);
                    }
                }

                users.put(user, status);
            }
        } catch (IOException e) {
            System.err.println("Couldn't read users");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * uses when program want answer from user: positive or negative
     *
     * @param scanner - may be onlineScanner?
     * @return 'true' if user types 'y' or 'yes'
     * 'false' if user types 'n' or 'no'
     * @throws IOException (in case that FastScanner could be broken)
     */
    private boolean positiveAnswer(FastScanner scanner) throws IOException {
        System.out.println("(y or n)");
        while (true) {
            String guestAns = removeWhiteSpaces(scanner.nextNoLineSeparate()).toLowerCase();
            switch (guestAns) {
                case "y":
                case "yes":
                    return true;

                case "n":
                case "no":
                    return false;

                default:
                    System.out.println("Please input y or n");
            }
        }
    }

    /**
     * logs in user to this System
     * if user doesn't want to log in
     * then his name would be 'Anonymous' and his statistics wouldn't be saved
     */
    private void userLogin() {
        readUsers();

        System.out.println("Would you like to save your rating (log in)?");
        try {
            if (!positiveAnswer(onlineScanner)) {
                username = "Anonymous";
                return;
            }

            System.out.println("Please write your name:");
            username = removeWhiteSpaces(onlineScanner.nextNoLineSeparate()).toLowerCase();

            if (users.keySet().contains(username)) {
                userStatistics = users.get(username);
                if ((int) userStatistics.get(1) == 0) {
                    welcomeToMyProgram(username, -1);
                } else {
                    int correct = (int) userStatistics.get(0), total = (int) userStatistics.get(1);
                    welcomeToMyProgram(username, (double) correct / total);
                }
            } else {
                System.out.println("Username \"" + username + "\" not exists in system");
                System.out.println("Do you want to register?");

                if (positiveAnswer(onlineScanner)) {
                    userStatistics = new ArrayList<>();
                    userStatistics.add(0);
                    userStatistics.add(0);
                    System.out.println("\"" + username + "\" successfully registered!");
                } else {
                    username = "Anonymous";
                    userStatistics = users.get(username);
                    System.out.println("Ok.");
                }
                welcomeToMyProgram(username, -1);
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while logging in");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void showRating() {
        if (username.equals("Anonymous")) {
            return;
        }

        int correct = (int) userStatistics.get(0), total = (int) userStatistics.get(1);
        if (correct == 0 && total == 0) {
            System.out.println("Not participated yet");
            return;
        }

        double res = (double) correct / total;
        System.out.print("Your current rank is: ");
        int pos = 0;
        for (; pos < ranking.length; ++pos) {
            if (res < ranking[pos]) {
                break;
            }
        }
        System.out.println(naming[pos]);
    }

    /**
     * writes sentences when run this program
     */
    private void welcomeToMyProgram(String name, double grade) {
        System.out.println("Welcome to ota program, " + name + "!");
        if (grade < 0) {
            System.out.println("You are beginner, not participated in quiz mode");
        } else {
            System.out.println("Your rating is " + Integer.toString((int) Math.ceil(grade * 3800)));
        }
        System.out.println("There are " + mainDict.size() + " words in dictionary");
    }

    /**
     * writes all meanings of word in some dictionary
     *
     * @param dictionary - one of 4 dictionaries: noun, verb, adjective, adverb
     * @param word       - String, word in dictionary
     * @param part       - String, possible: "Noun", "Verb", "Adjective", "Adverb"
     */
    private void writeMeanings(Map<String, List<String>> dictionary, String word, String part) {
        List<String> descriptions = dictionary.get(word);
        if (descriptions == null) {
            return;
        }

        System.out.println("===" + part + "===");
        System.out.println(descriptions.get(0));
        for (int i = 1; i < descriptions.size(); ++i) {
            System.out.println(" " + descriptions.get(i));
        }
    }

    /**
     * writes all meanings in all dictionaries
     *
     * @param word - String, word in dictionary
     */
    private void writeAllMeanings(String word) {
        System.out.println("===MAIN MEANING===");
        System.out.println(mainDict.get(word).toUpperCase());

        writeMeanings(nounDict, word, "Noun");
        writeMeanings(verbDict, word, "Verb");
        writeMeanings(adjDict, word, "Adjective");
        writeMeanings(advDict, word, "Adverb");
    }

    private void waitForQuery() {
        System.out.println("Write something to contact with this program");
        System.out.println("    case positive number: move to quiz mode with respective quantity of questions");
        System.out.println("    case negative number: move to list mode with respective quantity of words");
        System.out.println("    case -: stop running this program");
        System.out.println("    case ': move to quiz mode");
        System.out.println("    case .: move to list mode");

        try {
            boolean haveToRead = true;
            while (haveToRead && onlineScanner.hasNext()) {
                System.out.println("Please send query");

                String query = removeWhiteSpaces(onlineScanner.nextNoLineSeparate());
                boolean isNumber = true;
                int minusQty = 0, digitQty = 0;
                for (int i = 0; i < query.length(); ++i) {
                    if (query.charAt(i) != '-' && !Character.isDigit(query.charAt(i))) {
                        isNumber = false;
                    }
                    if (query.charAt(i) == '-') {
                        ++minusQty;
                    }
                    if (Character.isDigit(query.charAt(i))) {
                        ++digitQty;
                    }
                }

                if (isNumber && digitQty > 0) {
                    if (query.startsWith("-") && minusQty == 1) {
                        writeListMode(onlineScanner, -Integer.parseInt(query));
                    } else {
                        quizMode(onlineScanner, Integer.parseInt(query));
                    }
                    continue;
                }

                switch (query) {
                    case "-":
                        haveToRead = false;
                        break;
                    case "'":
                        quizMode(onlineScanner, -1);
                        break;
                    case ".":
                        writeListMode(onlineScanner, -1);
                        break;
                    default:
                        if (mainDict.get(query) == null) {
                            addMode(query, onlineScanner);
                        } else {
                            writeAllMeanings(query);
                        }
                }
            }
        } catch (IOException e) {
            // I don't know what's happen
            System.err.println("Something gone wrong while waiting for query");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void addMeanings(FastScanner scanner, Map<String, List<String>> dictionary,
                             String word, String part) throws IOException {
        System.out.println();
        System.out.println("Input description as " + part);

        List<String> descriptions = new ArrayList<>();

        while (true) {
            String currDescription = removeWhiteSpaces(scanner.nextNoLineSeparate());
            if (currDescription.equals("-")) {
                break;
            }

            descriptions.add(currDescription);
        }

        if (!descriptions.isEmpty()) {
            dictionary.put(word, descriptions);
        }
    }

    private void addMode(String word, FastScanner scanner) throws IOException {
        System.out.println("Not found '" + word + "' in dictionary");
        System.out.println("Would you like to add this word to it?");

        if (positiveAnswer(scanner)) {
            System.out.println("Please input main description");
            mainDict.put(word, removeWhiteSpaces(scanner.nextNoLineSeparate()));

            System.out.println("Please input another descriptions");
            System.out.println("input '-' for finish inputting");
            addMeanings(scanner, nounDict, word, "noun");
            addMeanings(scanner, verbDict, word, "verb");
            addMeanings(scanner, adjDict, word, "adjective");
            addMeanings(scanner, advDict, word, "adverb");

            System.out.println("Successfully added!");
        } else {
            System.out.println("Ok.");
        }
    }

    private String chooseWord(Set<String> written, Random rand) {
        int type;
        String word = "";
        do {
            type = rand.nextInt(4);
            switch (type) {
                case 0:
                    word = nounWords.get(rand.nextInt(nounWords.size())) + "0";
                    break;
                case 1:
                    word = verbWords.get(rand.nextInt(verbWords.size())) + "1";
                    break;
                case 2:
                    word = adjWords.get(rand.nextInt(adjWords.size())) + "2";
                    break;
                case 3:
                    word = advWords.get(rand.nextInt(advWords.size())) + "3";
                    break;
            }
        } while (written.contains(word));

        written.add(word);

        return word;
    }

    private int readInteger(FastScanner scanner) throws IOException {
        System.out.println("Input quantity of words you want to guess");
        System.out.println("You can input number not bigger than " + mainDict.size());

        int qty = 0;
        boolean integer = false;
        while (!integer) {
            try {
                qty = Integer.parseInt(scanner.nextNoLineSeparate());
                if (qty <= 0) {
                    System.err.println("Please input positive value");
                } else {
                    integer = true;
                }
            } catch (NumberFormatException e) {
                System.err.println("Input a number");
            }
        }

        return qty;
    }

    private void quizMode(FastScanner scanner, int guestQty) throws IOException {
        int qty;
        if (guestQty == -1) {
            qty = readInteger(scanner);
        } else {
            qty = guestQty;
        }

        Random rand = new Random();
        Set<String> written = new TreeSet<>();
        int correct = 0;
        for (int i = 0; i < qty; ++i) {
            System.out.print(i + ") ");
            String currWord = chooseWord(written, rand);
            int type = Integer.parseInt(currWord.substring(currWord.length() - 1));
            currWord = currWord.substring(0, currWord.length() - 1);

            switch (type) {
                case 0:
                    System.out.println(currWord + " as noun");
                    break;
                case 1:
                    System.out.println(currWord + " as verb");
                    break;
                case 2:
                    System.out.println(currWord + " as adjective");
                    break;
                case 3:
                    System.out.println(currWord + " as adverb");
                    break;
                default:
                    System.err.println("expected 0..3, found another for random: " + type);
                    System.exit(0);
            }

            String guestAns = removeWhiteSpaces(scanner.nextNoLineSeparate());
            if (guestAns.equals("-")) {
                qty = i;
                break;
            }

            switch (type) {
                case 0:
                    if (!nounDict.get(currWord).contains(guestAns)) {
                        System.out.println("Unfortunately, it's wrong answer");
                        System.out.println();
                        writeMeanings(nounDict, currWord, "Noun");
                        System.out.println();
                    } else {
                        System.out.println("Yes");
                        ++correct;
                        System.out.println();
                    }
                    break;
                case 1:
                    if (!verbDict.get(currWord).contains(guestAns)) {
                        System.out.println("Unfortunately, it's wrong answer");
                        System.out.println();
                        writeMeanings(verbDict, currWord, "Verb");
                        System.out.println();
                    } else {
                        System.out.println("Yes");
                        ++correct;
                        System.out.println();
                    }
                    break;
                case 2:
                    if (!adjDict.get(currWord).contains(guestAns)) {
                        System.out.println("Unfortunately, it's wrong answer");
                        System.out.println();
                        writeMeanings(adjDict, currWord, "Adjective");
                        System.out.println();
                    } else {
                        System.out.println("Yes");
                        ++correct;
                        System.out.println();
                    }
                    break;
                case 3:
                    if (!advDict.get(currWord).contains(guestAns)) {
                        System.out.println("Unfortunately, it's wrong answer");
                        System.out.println();
                        writeMeanings(advDict, currWord, "Adverb");
                        System.out.println();
                    } else {
                        System.out.println("Yes");
                        ++correct;
                        System.out.println();
                    }
                    break;
                default:
                    System.err.println("expected 0..3, found another for random: " + type);
                    System.exit(0);
            }
        }

        System.out.println(Integer.toString(correct) + " correct / " + Integer.toString(qty) + " total");
        int prevCorrect = (int) userStatistics.get(0), prevTotal = (int) userStatistics.get(1);
        int currCorrect = prevCorrect + correct, currTotal = prevTotal + qty;
        userStatistics.set(0, currCorrect);
        userStatistics.set(1, currTotal);

        double percentage = (double) correct / qty * 100;
        System.out.println("It's about " + Double.toString(percentage) + "%");

        int prevRating = (int) Math.ceil((double) prevCorrect / prevTotal * 3800);
        int currRating = (int) Math.ceil((double) currCorrect / currTotal * 3800);
        if (prevRating < currRating) {
            System.out.println("You got +" + Integer.toString(currRating - prevRating) + " to your rating!");
        } else {
            System.out.println("You got " + Integer.toString(currRating - prevRating) + " to your rating...");
        }
        System.out.println(Integer.toString(prevRating) + " -> " + Integer.toString(currRating));
        showRating();
        System.out.println();
    }

    private void writeListMode(FastScanner scanner, int guestQty) throws IOException {
        int qty;
        if (guestQty == -1) {
            qty = readInteger(scanner);
        } else {
            qty = guestQty;
        }

        Set<String> written = new TreeSet<>();
        Random rand = new Random();
        for (int i = 0; i < qty; ++i) {
            String currWord = chooseWord(written, rand);
            int type = Integer.parseInt(currWord.substring(currWord.length() - 1));
            currWord = currWord.substring(0, currWord.length() - 1);

            switch (type) {
                case 0:
                    System.out.println(currWord + " as noun");
                    break;
                case 1:
                    System.out.println(currWord + " as verb");
                    break;
                case 2:
                    System.out.println(currWord + " as adjective");
                    break;
                case 3:
                    System.out.println(currWord + " as adverb");
                    break;
                default:
                    System.err.println("expected 0..3, found another for random: " + type);
                    System.exit(0);
            }
        }
    }

    private void updateNotMain(Map<String, List<String>> dictionary, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)))) {
            for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
                writer.write(entry.getKey() + ": ");

                writer.write(entry.getValue().get(0));
                for (int i = 1; i < entry.getValue().size(); ++i) {
                    writer.write(", " + entry.getValue().get(i));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating file '" + fileName + "'");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void updateFiles() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("MainDictionary.ota")))) {
            for (Map.Entry<String, String> entry : mainDict.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating file");
            e.printStackTrace();
            System.exit(0);
        }

        updateNotMain(nounDict, "NounDict.ota");
        updateNotMain(verbDict, "VerbDict.ota");
        updateNotMain(adjDict, "AdjectiveDict.ota");
        updateNotMain(advDict, "AdverbDict.ota");

        if (!username.equals("Anonymous")) {
            users.put(username, userStatistics);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Users.ota")))) {
            for (Map.Entry<String, List<Object>> entry : users.entrySet()) {
                writer.write(entry.getKey() + ": ");

                List<Object> statistics = entry.getValue();
                writer.write(statistics.get(0).toString());
                for (int i = 1; i < statistics.size(); ++i) {
                    writer.write(", " + statistics.get(i).toString());
                }

                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating 'Users.ota'");
            e.printStackTrace();
            System.exit(0);
        }
    }
}