import scanner.FastScanner;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

class Program {
    // dictionary - pair of word and it meanings
    private Dictionary main;
    private Dictionary noun;
    private Dictionary verb;
    private Dictionary adj;
    private Dictionary adv;

    // dictionaries - array of all dictionaries
    private Dictionary[] dictionaries = new Dictionary[DICT_QTY];

    // dictNames - mapping from name to position in dictionaries
    private Map<String, Integer> dictNames = new TreeMap<>();

    // DICT_QTY - quantity of dictionaries (dictionaries size)
    private static final int DICT_QTY = 5;

    // allDictSize - size of all dictionaries (exclude main)
    private BigInteger allDictSizeB;

    // lineSeparate - String that
    private final String lineSeparate = System.lineSeparator();

    // users - list of logged in users
    private Map<String, List<Object>> users = new TreeMap<>();

    // direction - name of direction including all dictionaries
    private final String direction = "dict/";

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
     * initializes all dictionaries: main, noun, verb, adjective, adverb
     */
    private void initializeDictionaries() {
        dictionaries[0] = main = new Dictionary(new File(direction + "MainDictionary.ota"), "main");
        dictionaries[1] = noun = new Dictionary(new File(direction + "NounDict.ota"), "noun");
        dictionaries[2] = verb = new Dictionary(new File(direction + "VerbDict.ota"), "verb");
        dictionaries[3] = adj = new Dictionary(new File(direction + "AdjectiveDict.ota"), "adjective");
        dictionaries[4] = adv = new Dictionary(new File(direction + "AdverbDict.ota"), "adverb");

        for (int i = 0; i < DICT_QTY; ++i) {
            String currName = dictionaries[i].getType();
            dictNames.put(currName, i);
            dictNames.put(reduce(currName), i);
        }

        // allDictSize - size of all dictionaries
        int allDictSize = noun.size() + verb.size() + adj.size() + adv.size();
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
                String user = scanner.next().trim();
                List<Object> status = new ArrayList<>();
                while (scanner.hasNext()) {
                    String currStatus = scanner.next();
                    if (lineSeparate.contains(currStatus)) {
                        break;
                    }

                    currStatus = currStatus.trim();
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
     * @return 'true' if user types 'y' or 'yes'
     * 'false' if user types 'n' or 'no'
     * @throws IOException (in case that scanner could be broken)
     */
    private boolean positiveAnswer() throws IOException {
        System.out.println("(y or n)");
        while (true) {
            String guestAns = onlineScanner.nextNoLineSeparate().trim().toLowerCase();
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
            if (!positiveAnswer()) {
                username = "Anonymous";
                userStatistics = users.get(username);
                welcomeToMyProgram(username, -1);
                return;
            }

            System.out.println("Please write your name:");
            username = onlineScanner.nextNoLineSeparate().trim().toLowerCase();

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

                if (positiveAnswer()) {
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
        System.out.println("There are " + main.size() + " words in dictionary");
    }

    /**
     * writes all meanings of word in some dictionary
     *
     * @param dictionary - one of 4 dictionaries: noun, verb, adjective, adverb
     * @param word       - String, word in dictionary
     */
    private void writeMeanings(Dictionary dictionary, String word) {
        List<String> descriptions = dictionary.get(word);
        if (descriptions == null) {
            return;
        }

        System.out.println("===" + dictionary.getType() + "===");
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
        if (main.get(word) == null) {
            return;
        }

        System.out.println("===MAIN MEANING===");
        System.out.println(main.get(word).get(0).toUpperCase());

        writeMeanings(noun, word);
        writeMeanings(verb, word);
        writeMeanings(adj, word);
        writeMeanings(adv, word);
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
            BigInteger res = new BigInteger(number.trim());
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

    private int getLastWordPos(String s) {
        int pos = s.length() - 1;
        while (pos >= 0 && s.charAt(pos) != ' ') {
            --pos;
        }
        while (pos >= 0 && s.charAt(pos) == ' ') {
            --pos;
        }
        return s.charAt(pos) != ' ' ? pos + 1 : pos;
    }

    private void waitForQuery() {
        System.out.println("Write something to contact with this program");
        help();

        try {
            boolean haveToRead = true;
            while (haveToRead && onlineScanner.hasNext()) {
                System.out.println("Please send query");

                String query = onlineScanner.nextNoLineSeparate().trim();
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
                                quizMode(qty);
                            } else {
                                listMode(qty);
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
                            String currWord = query.substring(pos).trim();
                            if (query.startsWith("/e")) {
                                editMode(currWord);
                            } else if (query.startsWith("/a")) {
                                createDict(currWord);
                            } else {
                                main.remove(currWord);
                                noun.remove(currWord);
                                verb.remove(currWord);
                                adj.remove(currWord);
                                adv.remove(currWord);
                                System.out.println("Successfully removed " + currWord + " from all dictionaries");
                            }
                            writeAllMeanings(currWord);
                        } else {
                            System.err.println("Expected word after " + first);
                        }
                        break;

                    default:
                        if (query.startsWith("/")) {
                            System.err.println("Unknown command " + first);
                            System.err.println("Input /h to see commands list");
                        } else if (main.get(query) == null) {
                            addMode(query);
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

    private Dictionary chooseAndWriteDict(String type, String word) {
        if (dictNames.get(type) == null) {
            System.out.println("Don't have dictionaries such kind of '" + word + "'");
            return null;
        }

        Dictionary dictionary = dictionaries[dictNames.get(type)];
        writeMeanings(dictionary, word);
        return dictionary;
    }

    private String getNextNotEmpty() {
        String answer = "";
        do {
            try {
                answer = onlineScanner.nextNoLineSeparate().trim();
            } catch (IOException e) {
                System.err.println("Scanner had broken at reading your query");
                updateFiles();
                System.exit(0);
            }
        } while (answer.isEmpty());

        return answer;
    }

    private void createDict(String word) {
        if (main.get(word) == null) {
            System.out.println("Main meaning for this word wasn't found");
            System.out.println("Please input main meaning");
            main.put(word, List.of(getNextNotEmpty()));
        }
        System.out.println("Input a dictionary you want to create");
        String answer;
        List<String> dictionaries = List.of("noun", "verb", "adj", "adv");
        do {
            System.out.println("(input noun or verb or adj or adv)");
            answer = getNextNotEmpty();

            if (answer.equals("-")) {
                return;
            }
        } while (!dictionaries.contains(answer));

        Dictionary currDict = chooseAndWriteDict(answer, word);
        if (currDict == null) {
            return;
        }

        addMeanings(currDict, word);
    }

    private String reduce(String part) {
        if (part.length() <= 4) {
            return part;
        }
        return part.substring(0, 3);
    }

    private void editMode(String word) {
        System.out.print("The word '" + word + "' ");
        if (main.get(word) == null) {
            System.out.println("not found in dictionaries");
            return;
        }

        List<String> existDict = new ArrayList<>();
        for (int i = 0; i < DICT_QTY; ++i) {
            if (dictionaries[i].get(word) != null) {
                existDict.add(dictionaries[i].getType());
            }
        }

        System.out.print("found as ");
        for (int i = 1; i < existDict.size() - 1; ++i) {
            System.out.print(existDict.get(i) + ", ");
        }
        System.out.println(existDict.get(existDict.size() - 1));
        for (int i = 0; i < existDict.size(); ++i) {
            existDict.set(i, reduce(existDict.get(i)));
        }

        System.out.println("Which dictionary do you want to edit?");
        String answer;
        do {
            System.out.print("(input ");
            for (int i = 0; i < existDict.size() - 1; ++i) {
                System.out.print(existDict.get(i) + " or ");
            }
            System.out.println(existDict.get(existDict.size() - 1) + ")");

            answer = getNextNotEmpty();
        } while (!existDict.contains(answer));

        if (answer.equals("main")) {
            System.out.println("===MAIN MEANING===");
            System.out.println(main.get(word).get(0).toUpperCase());

            System.out.println("Input new meaning for " + word);
            main.put(word, List.of(getNextNotEmpty()));
            return;
        }

        System.out.println("This is all meanings as " + answer);
        Dictionary currDict = chooseAndWriteDict(answer, word);
        if (currDict == null) {
            return;
        }

        do {
            System.out.println("Input 'del all' to delete all meanings");
            System.out.println("      'edit' to change meanings");

            answer = getNextNotEmpty();
        } while (!(answer.equals("edit") || answer.equals("del all")));

        switch (answer) {
            case "del all":
                currDict.remove(word);
                break;

            case "edit":
                editMeanings(currDict, word);
                break;

            default:
                System.err.println("Found unknown command '" + answer + "'");
                break;
        }
    }

    private void editMeanings(Dictionary dictionary, String word) {
        while (true) {
            List<String> meanings = dictionary.get(word);

            System.out.println("Input '+ <word>' to add meaning");
            System.out.println("      '- <number>' to delete meaning at position <number>");
            System.out.println("      '~ <number>' to edit meaning at position <number>");
            System.out.println("      '-' to quit");
            String answer = getNextNotEmpty();
            if (answer.equals("-")) {
                break;
            }

            int pos = getSecondWordPos(answer);
            String second = answer.substring(pos).trim();
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
                    if (--value >= meanings.size()) {
                        System.out.println("Number should be less than " + (meanings.size() + 1));
                        continue;
                    }

                    if (answer.equals("-")) {
                        meanings.remove(value);
                    } else {
                        System.out.println("Input new meaning");
                        String newMeaning = getNextNotEmpty();
                        meanings.set(value, newMeaning);
                    }
                    break;

                default:
                    System.err.println("Unknown command '" + answer + "'");
                    break;
            }

            dictionary.put(word, meanings);
            writeMeanings(dictionary, word);
        }
    }

    private void addMeanings(Dictionary dictionary, String word) {
        System.out.println();
        System.out.println("Input '-' to quit");
        System.out.println("      '--' to delete previous meaning");
        System.out.println("Input description as " + dictionary.getType());

        List<String> descriptions = dictionary.get(word) == null ? new ArrayList<>() : dictionary.get(word);

        while (true) {
            String currDescription = getNextNotEmpty();
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

    private void addMode(String word) throws IOException {
        System.out.println("Not found '" + word + "' in dictionary");
        System.out.println("Would you like to add this word to it?");

        if (positiveAnswer()) {
            System.out.println("Please input main description");
            main.put(word, List.of(getNextNotEmpty()));

            System.out.println("Please input another descriptions");
            addMeanings(noun, word);
            addMeanings(verb, word);
            addMeanings(adj, word);
            addMeanings(adv, word);

            System.out.println("Successfully added!");
        } else {
            System.out.println("Ok.");
        }
    }

    private String chooseWord(Set<String> written, Random rand) {
        int type;
        String word;
        do {
            type = rand.nextInt(4) + 1;
            word = dictionaries[type].getWords().get(rand.nextInt(dictionaries[type].size())) + " " + type;
        } while (written.contains(word));

        written.add(word);

        return word;
    }

    private void quizMode(int qty) {
        Random rand = new Random();
        Set<String> written = new TreeSet<>();
        int correct = 0;
        System.out.println(qty + " words would be written");

        for (int i = 1; i <= qty; ++i) {
            System.out.print(i + ") ");
            String currWord = chooseWord(written, rand);
            int pos = getLastWordPos(currWord);
            int type = Integer.parseInt(currWord.substring(pos).trim());
            currWord = currWord.substring(0, pos);

            Dictionary currDict = dictionaries[type];
            System.out.println(currWord + " as " + currDict.getType());

            String guestAns = getNextNotEmpty();
            if (guestAns.equals("-")) {
                qty = i;
                break;
            }

            if (!currDict.get(currWord).contains(guestAns)) {
                System.out.println("Unfortunately, it's wrong answer");
                System.out.println();
                System.out.println("===MAIN MEANING===");
                System.out.println(main.get(currWord).get(0).toUpperCase());
                writeMeanings(currDict, currWord);
                System.out.println();
            } else {
                System.out.println("Yes");
                ++correct;
                System.out.println();
            }
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

    private void listMode(int qty) {
        List<String> words = main.getWords();
        Random random = new Random();
        for (int i = 0; i < qty; ++i) {
            String currWord = words.remove(random.nextInt(words.size()));
            System.out.println((i + 1) + ") " + currWord);
        }
    }

    private void updateFiles() {
        main.update(new File(direction + "MainDictionary.ota"));
        noun.update(new File(direction + "NounDict.ota"));
        verb.update(new File(direction + "VerbDict.ota"));
        adj.update(new File(direction + "AdjectiveDict.ota"));
        adv.update(new File(direction + "AdverbDict.ota"));

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