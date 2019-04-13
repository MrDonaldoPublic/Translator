import jdk.swing.interop.SwingInterOpUtils;
import scanner.FastScanner;

import java.io.*;
import java.math.BigInteger;
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

    // allDictSize - size of all dictionaries (exclude main)
    private BigInteger allDictSizeB;

    // lineSeparate - String that
    private final String lineSeparate = System.lineSeparator();

    // users - list of logged in users
    private Map<String, List<Object>> users = new TreeMap<>();

    // folder - name of folder including all dictionaries
    private final String folder = "dict/";

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
     * Removes whitespaces from prefix and suffix like trim() in js
     *
     * @param s = String
     * @return s without whitespaces in prefix and suffix
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

        if (leftPos >= rightPos) {
            return "";
        } else {
            return s.substring(leftPos, rightPos);
        }
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
        try (FastScanner scanner = new FastScanner(new File(folder + fileName))) {
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

                    descriptions.add(removeWhiteSpaces(currDescription));
                }
                // read all descriptions for the word

                // adding word and its descriptions in dictionary
                dictionary.put(currWord, descriptions);
            }
            // all words and its descriptions are added in dictionary
        } catch (IOException e) {
            // attempt to connect the file was unsuccessful
            System.err.println("File '" + folder + fileName + "' not found.");
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
        try (FastScanner scanner = new FastScanner(new File(folder + "/MainDictionary.ota"))) {
            // reading all words from file "MainDictionary.ota"
            while (scanner.hasNext()) {
                // reading word
                String currWord = scanner.next();

                // reading some descriptions for the word
                String description = "";
                while (scanner.hasNext()) {
                    String tmp = scanner.next();
                    if (lineSeparate.contains(tmp)) {
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

        // allDictSize - size of all dictionaries
        int allDictSize = nounDict.size() + verbDict.size() + adjDict.size() + advDict.size();
        allDictSizeB = new BigInteger(Integer.toString(allDictSize));
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
                    if (lineSeparate.contains(currStatus)) {
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
     * @throws IOException (in case that scanner could be broken)
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
                userStatistics = users.get(username);
                welcomeToMyProgram(username, -1);
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

    /**
     * writes current user's rating
     */
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
            System.out.println("Your rating is " + (int) Math.ceil(grade * 3800));
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
        for (int i = 0; i < descriptions.size(); ++i) {
            System.out.println((i + 1) + ") " + descriptions.get(i));
        }
    }

    /**
     * writes all meanings in all dictionaries
     *
     * @param word - String, word in dictionary
     */
    private void writeAllMeanings(String word) {
        if (mainDict.get(word) == null) {
            return;
        }

        System.out.println("===MAIN MEANING===");
        System.out.println(mainDict.get(word).toUpperCase());

        writeMeanings(nounDict, word, "Noun");
        writeMeanings(verbDict, word, "Verb");
        writeMeanings(adjDict, word, "Adjective");
        writeMeanings(advDict, word, "Adverb");
    }

    private void help() {
        System.out.println("=====PROGRAM-RESPONSIBLE COMMANDS LIST=====");
        System.out.println("    case -: stop running this program");
        System.out.println("    case /q <number>: move to quiz mode");
        System.out.println("    case /l <number>: move to list mode");
        System.out.println("    case /e <word>: move to edit mode");
        System.out.println("    case /a <word>: create new dictionary for this word");
        System.out.println("    case /d <word>: delete all meanings for this word");
        System.out.println("    case /h: display this message");
        System.out.println("===========================================");
    }

    /**
     * Casts number to int from String
     *
     * @param number - String with whitespace in first pos
     * @return number casted to int or -1 if there is not number
     */
    private int toNumber(String number) {
        try {
            BigInteger res = new BigInteger(removeWhiteSpaces(number));
            res = res.min(allDictSizeB);
            return Integer.parseInt(res.toString());
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    private int getSecondWordPos(String s) {
        int pos = 0;
        while (pos < s.length() && s.charAt(pos) != ' ') {
            ++pos;
        }

        return pos;
    }

    private void waitForQuery() {
        System.out.println("Write something to contact with this program");
        help();

        try {
            boolean haveToRead = true;
            while (haveToRead && onlineScanner.hasNext()) {
                System.out.println("Please send query");

                String query = removeWhiteSpaces(onlineScanner.nextNoLineSeparate());
                if (query.isEmpty()) {
                    continue;
                }
                int pos = getSecondWordPos(query);
                String first = query.substring(0, pos);

                switch (first) {
                    case "-":
                        haveToRead = false;
                        break;
                    case "/q":
                    case "/quiz":
                    case "/l":
                    case "/list":
                        int qty = toNumber(query.substring(pos));
                        if (qty <= -1) {
                            System.err.println("Expected number after " + first);
                        } else {
                            if (first.startsWith("/q")) {
                                quizMode(onlineScanner, qty, false);
                            } else {
                                quizMode(onlineScanner, qty, true);
                            }
                        }
                        break;
                    case "/h":
                    case "/help":
                        help();
                        break;
                    case "/r":
                    case "/rate":
                        showRating();
                        break;
                    case "/e":
                    case "/edit":
                    case "/a":
                    case "/add":
                    case "/d":
                    case "/del":
                    case "/delete":
                        if (query.length() > first.length()) {
                            String currWord = removeWhiteSpaces(query.substring(pos));
                            if (query.startsWith("/e")) {
                                editMode(onlineScanner, currWord);
                            } else if (query.startsWith("/a")) {
                                createDict(onlineScanner, currWord);
                                writeAllMeanings(currWord);
                            } else {
                                mainDict.remove(currWord);
                                nounDict.remove(currWord);
                                verbDict.remove(currWord);
                                adjDict.remove(currWord);
                                advDict.remove(currWord);
                                System.out.println("Successfully removed " + currWord + " from all dictionaries");
                            }
                        } else {
                            System.err.println("Expected word after " + first);
                        }
                        break;
                    default:
                        if (query.startsWith("/")) {
                            System.err.println("Unknown command " + first);
                            System.err.println("Input /h to see commands list");
                        } else if (mainDict.get(query) == null) {
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

    private Map<String, List<String>> chooseAndWriteDict(String type, String word) {
        switch (type) {
            case "noun":
                writeMeanings(nounDict, word, "noun");
                return nounDict;
            case "verb":
                writeMeanings(verbDict, word, "verb");
                return verbDict;
            case "adj":
                writeMeanings(adjDict, word, "adjective");
                return adjDict;
            case "adv":
                writeMeanings(advDict, word, "adverb");
                return advDict;
            default:
                System.out.println("Don't have dictionaries such kind of '" + word + "'");
                return null;
        }
    }

    private String getNextNotEmpty(FastScanner scanner) {
        String answer = "";
        while (!answer.isEmpty()) {
            try {
                answer = removeWhiteSpaces(scanner.nextNoLineSeparate());
            } catch (IOException e) {
                System.err.println("Scanner had broken at reading your query");
                updateFiles();
                System.exit(0);
            }
        }

        return answer;
    }

    private void createDict(FastScanner scanner, String word) {
        if (mainDict.get(word) == null) {
            System.out.println("Main meaning for this word wasn't found");
            System.out.println("Please input main meaning");
            mainDict.put(word, getNextNotEmpty(scanner));
        }
        System.out.println("Input a dictionary you want to create");
        String answer;
        List<String> dictionaries = List.of("noun", "verb", "adj", "adv");
        do {
            System.out.println("(input noun or verb or adj or adv)");
            answer = getNextNotEmpty(scanner);

            if (answer.equals("-")) {
                return;
            }
        } while (!dictionaries.contains(answer));

        Map<String, List<String>> currDict = chooseAndWriteDict(answer, word);
        if (currDict == null) {
            return;
        }

        addMeanings(scanner, currDict, word, answer);
    }

    private String reduce(String part) {
        if (part.length() <= 4) {
            return part;
        }
        return part.substring(0, 3);
    }

    private void editMode(FastScanner scanner, String word) {
        System.out.print("The word '" + word + "' ");
        if (mainDict.get(word) == null) {
            System.out.println("not found in dictionaries");
            return;
        }

        List<String> dictionaries = new ArrayList<>();
        dictionaries.add("main");
        if (nounDict.get(word) != null) {
            dictionaries.add("noun");
        }
        if (verbDict.get(word) != null) {
            dictionaries.add("verb");
        }
        if (adjDict.get(word) != null) {
            dictionaries.add("adjective");
        }
        if (advDict.get(word) != null) {
            dictionaries.add("adverb");
        }

        System.out.print("found as ");
        for (int i = 0; i < dictionaries.size() - 1; ++i) {
            System.out.print(dictionaries.get(i) + ", ");
        }
        System.out.println(dictionaries.get(dictionaries.size() - 1));
        for (int i = 0; i < dictionaries.size(); ++i) {
            dictionaries.set(i, reduce(dictionaries.get(i)));
        }

        System.out.println("Which dictionary do you want to edit?");
        String answer;
        do {
            System.out.print("(input ");
            for (int i = 0; i < dictionaries.size() - 1; ++i) {
                System.out.print(dictionaries.get(i) + " or ");
            }
            System.out.println(dictionaries.get(dictionaries.size() - 1) + ")");

            answer = getNextNotEmpty(scanner);
        } while (!dictionaries.contains(answer));

        if (answer.equals("main")) {
            System.out.println("===MAIN MEANING===");
            System.out.println(mainDict.get(word).toUpperCase());

            System.out.println("Input new meaning for " + word);
            mainDict.put(word, getNextNotEmpty(scanner));
            return;
        }

        System.out.println("This is all meanings as " + answer);
        Map<String, List<String>> currDict = chooseAndWriteDict(answer, word);
        if (currDict == null) {
            return;
        }

        while (true) {
            System.out.println("Input 'del all' to delete all meanings");
            System.out.println("      'edit' to change meanings");

            answer = getNextNotEmpty(scanner);
            if (answer.equals("-")) {
                break;
            }
            switch (answer) {
                case "del all":
                    currDict.remove(word);
                    break;
                case "edit":
                    editMeanings(scanner, currDict, word);
                    break;
                default:
                    System.err.println("Found unknown command '" + answer + "'");
                    break;
            }
        }
    }

    private void editMeanings(FastScanner scanner, Map<String, List<String>> dictionary, String word) {
        while (true) {
            List<String> meanings = dictionary.get(word);

            System.out.println("Input '+ <word>' to add meaning");
            System.out.println("      '- <number>' to delete meaning at position <number>");
            System.out.println("      '~ <number>' to edit meaning at position <number>");
            System.out.println("      '-' to quit");
            String answer = getNextNotEmpty(scanner);
            if (answer.equals("-")) {
                break;
            }

            int pos = getSecondWordPos(answer);
            String second = removeWhiteSpaces(answer.substring(pos));
            answer = answer.substring(0, pos);

            switch (answer) {
                case "+":
                    if (second.isEmpty()) {
                        System.err.println("Expected word after + command");
                        continue;
                    }
                    meanings.add(second);
                    break;
                case "-":
                case "~":
                    int value = toNumber(second);
                    if (value < 0) {
                        System.err.println("Expected positive number");
                        continue;
                    }
                    if (value >= meanings.size()) {
                        System.out.println("Number should be less than " + (meanings.size() + 1));
                        continue;
                    }

                    if (answer.equals("-")) {
                        meanings.remove(value);
                    } else {
                        System.out.println("Input new meaning");
                        String newMeaning = getNextNotEmpty(scanner);
                        meanings.set(value, newMeaning);
                    }
                    break;
                default:
                    System.err.println("Unknown command '" + answer + "'");
                    break;
            }

            dictionary.put(word, meanings);
            writeAllMeanings(word);
        }
    }

    private void addMeanings(FastScanner scanner, Map<String, List<String>> dictionary,
                             String word, String part) {
        System.out.println();
        System.out.println("Input description as " + part);

        List<String> descriptions = dictionary.get(word) == null ? new ArrayList<>() : dictionary.get(word);

        while (true) {
            String currDescription = getNextNotEmpty(scanner);
            if (currDescription.equals("-")) {
                break;
            }

            if (currDescription.equals("--")) {
                if (descriptions.isEmpty()) {
                    System.out.println("Nothing to delete");
                } else {
                    System.out.println("Removed " + descriptions.remove(descriptions.size() - 1));
                }
            } else {
                descriptions.add(currDescription);
            }
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
            mainDict.put(word, getNextNotEmpty(scanner));

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

    private void quizMode(FastScanner scanner, int qty, boolean list) {
        Random rand = new Random();
        Set<String> written = new TreeSet<>();
        int correct = 0;
        System.out.println(qty + " words would be written");

        for (int i = 1; i <= qty; ++i) {
            System.out.print(i + ") ");
            String currWord = chooseWord(written, rand);
            int type = Integer.parseInt(currWord.substring(currWord.length() - 1));
            currWord = currWord.substring(0, currWord.length() - 1);

            Map<String, List<String>> currDict = new TreeMap<>();
            String dictType = "";
            switch (type) {
                case 0:
                    System.out.println(currWord + " as noun");
                    currDict = nounDict;
                    dictType = "Noun";
                    break;
                case 1:
                    System.out.println(currWord + " as verb");
                    currDict = verbDict;
                    dictType = "Verb";
                    break;
                case 2:
                    System.out.println(currWord + " as adjective");
                    currDict = adjDict;
                    dictType = "Adjective";
                    break;
                case 3:
                    System.out.println(currWord + " as adverb");
                    currDict = advDict;
                    dictType = "Adverb";
                    break;
                default:
                    System.err.println("expected 0..3, found another for random: " + type);
                    System.exit(0);
            }
            if (list) {
                continue;
            }

            String guestAns = getNextNotEmpty(scanner);
            if (guestAns.equals("-")) {
                qty = i;
                break;
            }

            if (!currDict.get(currWord).contains(guestAns)) {
                System.out.println("Unfortunately, it's wrong answer");
                System.out.println();
                System.out.println("===MAIN MEANING===");
                System.out.println(mainDict.get(currWord).toUpperCase());
                writeMeanings(currDict, currWord, dictType);
                System.out.println();
            } else {
                System.out.println("Yes");
                ++correct;
                System.out.println();
            }
        }

        if (list) {
            System.out.println();
            return;
        }

        System.out.println(correct + " correct / " + qty + " total");
        int prevCorrect = (int) userStatistics.get(0), prevTotal = (int) userStatistics.get(1);
        int currCorrect = prevCorrect + correct, currTotal = prevTotal + qty;
        userStatistics.set(0, currCorrect);
        userStatistics.set(1, currTotal);

        System.out.format("It's about %.2f%%%n", correct * 100.0 / qty);

        int prevRating = (int) Math.ceil((double) prevCorrect / prevTotal * 3800);
        int currRating = (int) Math.ceil((double) currCorrect / currTotal * 3800);
        if (prevRating > currRating) {
            System.out.println("You got " + (currRating - prevRating) + " to your rating...");
        } else {
            System.out.println("You got +" + (currRating - prevRating) + " to your rating!");
        }
        System.out.println(prevRating + " -> " + currRating);
        showRating();
        System.out.println();
    }

    private void updateNotMain(Map<String, List<String>> dictionary, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folder + fileName)))) {
            for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
                writer.write(entry.getKey() + ": ");

                writer.write(entry.getValue().get(0));
                for (int i = 1; i < entry.getValue().size(); ++i) {
                    writer.write(", " + entry.getValue().get(i));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Something gone wrong while updating file '" + folder + fileName + "'");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void updateFiles() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folder + "MainDictionary.ota")))) {
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

        if (username.equals("Anonymous")) {
            Collections.fill(userStatistics, 0);
        }
        users.put(username, userStatistics);
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