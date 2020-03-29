package flashcards;

import java.io.*;
import java.util.*;

public class Main {
    private static String exportFileArgs;
    private static boolean exportFile = false;
    private static Map<String, String> flashcards = new HashMap<>();
    private static Map<String, Integer> mistakes = new HashMap<>();
    private static List<String> log = new LinkedList<>();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String action;
        if (args.length == 2) {
            introAPI(args);
        } else if (args.length == 4) {
            introAPI(args);
            if (args[2].equals("-export")) {
                exportFile = true;
                exportFileArgs = args[3];
            } else if (args[2].equals("-import")) {
                File file = new File(args[3]);
                if (file.isFile()) {
                    int cnt = importFromFile(file);
                    System.out.print(toLog("" + cnt + " cards have been loaded.\n\n"));
                } else {
                    System.out.print(toLog("File not found.\n\n"));
                }
            }
        }
        while (true) {
            System.out.print(toLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):\n"));
            action = toLog(sc.nextLine());
            toLog("\n");

            switch (action) {
                case "add":
                    add();
                    break;
                case "remove":
                    remove();
                    break;
                case "import":
                    importCards();
                    break;
                case "export":
                    exportCards();
                    break;
                case "ask":
                    ask();
                    break;
                case "exit":
                    exit();
                    break;
                case "log":
                    logToFile();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
                default:
                    unknownAction();
                    break;
            }
        }
    }

    private static void introAPI(String[] args) {
        if (args[0].equals("-import")) {
            File file = new File(args[1]);
            if (file.isFile()) {
                int cnt = importFromFile(file);
                System.out.print(toLog("" + cnt + " cards have been loaded.\n\n"));
            } else {
                System.out.print(toLog("File not found.\n\n"));
            }
        } else if (args[0].equals("-export")) {
            exportFile = true;
            exportFileArgs = args[1];
        }
    }

    private static String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static void add() {
        System.out.print(toLog("The card:\n"));

        String term = toLog(sc.nextLine()); toLog("\n");
        if (flashcards.containsKey(term)) {
            System.out.print(toLog("The card \"" + term + "\" already exists.\n\n"));
            return;
        }

        System.out.print(toLog("The definition of the card:\n"));
        String definition = toLog(sc.nextLine()); toLog("\n");
        if (flashcards.containsValue(definition)) {
            System.out.print(toLog("The definition \"" + definition + "\" already exists.\n\n"));
            return;
        }

        flashcards.put(term, definition);
        mistakes.put(term, 0);
        System.out.print(toLog("The pair (\"" + term + "\":\"" + definition + "\") has been added.\n\n"));
    }

    public static void remove() {
        System.out.print(toLog("The card:\n"));

        String term = toLog(sc.nextLine()); toLog("\n");
        if (flashcards.containsKey(term)) {
            flashcards.remove(term);
            mistakes.remove(term);
            System.out.print(toLog("The card has been removed.\n\n"));
        } else {
            System.out.print(toLog("Can't remove \"" + term + "\": there is no such card.\n\n"));
        }
    }

    public static void importCards() {
        System.out.print(toLog("File name:\n"));

        String fileName = toLog(sc.nextLine()); toLog("\n");
        File file = new File(fileName);
        if (file.isFile()) {
            int cnt = importFromFile(file);
            System.out.print(toLog("" + cnt + " cards have been loaded.\n\n"));
        } else {
            System.out.print(toLog("File not found.\n\n"));
        }
    }

    private static int importFromFile(File file) {
        String term;
        String definition;
        int mistake;
        int cnt = 0;

        try (Scanner readFile = new Scanner(file)) {
            while (readFile.hasNext()) {
                term = readFile.nextLine();
                definition = readFile.nextLine();
                mistake = Integer.parseInt(readFile.nextLine());

                if (flashcards.containsKey(term)) {
                    flashcards.replace(term, definition);
                    mistakes.replace(term, mistake);
                } else {
                    flashcards.put(term, definition);
                    mistakes.put(term, mistake);
                }

                cnt++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return cnt;
    }

    public static void exportCards() {
        System.out.print(toLog("File name:\n"));
        String fileName = toLog(sc.nextLine()); toLog("\n");
        File file = new File(fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            flashcards.forEach((k, v) -> writer.printf("%s\r\n%s\r\n%d\r\n", k, v, mistakes.get(k)));
            System.out.print(toLog("" + flashcards.size() + " cards have been saved.\n\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ask() {
        System.out.print(toLog("How many times to ask?\n"));
        int count = Integer.parseInt(toLog(sc.nextLine())); toLog("\n");
        ArrayList<String> keys = new ArrayList<>(flashcards.keySet());
        Random rnd = new Random();
        String term, answer, definition;
        int mistake;

        for (int i = 0; i < count; i++) {
            term = keys.get(rnd.nextInt(keys.size()));
            System.out.print(toLog("Print the definition of \"" + term + "\":\n"));
            answer = toLog(sc.nextLine()); toLog("\n");
            definition = flashcards.get(term);
            mistake = mistakes.get(term);
            if (Objects.equals(answer, definition)) {
                System.out.print(toLog("Correct answer.\n"));
            } else {
                System.out.print(toLog("Wrong answer. The correct one is \"" + definition + "\""));
                if (flashcards.containsValue(answer)) {
                    System.out.print(toLog(", you've just written the definition of \"" + getKeyByValue(flashcards, answer) + "\".\n"));
                } else {
                    System.out.print(toLog(".\n"));
                }
                mistakes.replace(term, mistake+1);
            }
        }
        System.out.print(toLog("\n"));
    }

    public static void exit() {
        System.out.print(toLog("Bye bye!\n"));
        if (exportFile) {
            File file = new File(exportFileArgs);
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
                flashcards.forEach((k, v) -> writer.printf("%s\r\n%s\r\n%d\r\n", k, v, mistakes.get(k)));
                System.out.print(toLog("" + flashcards.size() + " cards have been saved.\n\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public static void logToFile() {
        System.out.print(toLog("File name:\n"));
        String fileName = toLog(sc.nextLine()); toLog("\n");
        File file = new File(fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            System.out.print(toLog("The log has been saved.\n\n"));
            log.forEach(s -> writer.print(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toLog(String s) {
        log.add(s);
        return s;
    }

    public static void hardestCard() {
        int maxMistakes = getMaxMistakes();
        if (maxMistakes == 0) {
            System.out.print(toLog("There are no cards with errors.\n\n"));
        } else {
            ArrayList<String> hardest = new ArrayList<>(getHardest(maxMistakes));
            String end, cards;
            StringBuilder countries = new StringBuilder();
            if (hardest.size() <= 1) {
                countries.append("\"" + hardest.get(0) + "\"");
                end = "it.\n\n";
                cards = "card is ";
            } else {
                end = "them.\n\n";
                cards = "cards are ";
                for (String s : hardest) {
                    countries.append("\"" + s + "\", ");
                }
                countries.delete(countries.length() - 2, countries.length());
            }

            System.out.print(toLog("The hardest " + cards + countries.toString() + ". You have " + maxMistakes + " errors answering " + end));
        }
    }

    private static int getMaxMistakes() {
        int max = 0;
        for (Map.Entry entry : mistakes.entrySet()) {
            int value = (int)entry.getValue();
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private static List<String> getHardest(int max) {
        List<String> hardest = new ArrayList<>();
        for (Map.Entry entry : mistakes.entrySet()) {
            if ((int) entry.getValue() == max) {
                hardest.add((String) entry.getKey());
            }
        }
        return hardest;
    }

    public static void resetStats() {
        for (Map.Entry e : mistakes.entrySet()) {
            e.setValue(0);
        }
        System.out.print(toLog("Card statistics has been reset.\n\n"));
    }

    public static void unknownAction() {
        System.out.print(toLog("Unknown command. Try again...\n\n"));
    }
}