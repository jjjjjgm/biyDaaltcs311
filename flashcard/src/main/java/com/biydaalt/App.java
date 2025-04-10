package com.biydaalt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    private static final String DEFAULT_ORDER = "random";
    private static final int DEFAULT_REPETITIONS = 1;
    private static final boolean DEFAULT_INVERT_CARDS = false;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: No arguments provided.");
            printHelp();
            return;
        }

        // Check if help option is provided
        for (String arg : args) {
            if ("--help".equals(arg)) {
                printHelp();
                return;
            }
        }

        // Parse the first argument as the card file
        String cardFile = args[0];

        // Default values
        String orderType = DEFAULT_ORDER;
        int repetitions = DEFAULT_REPETITIONS;
        boolean invertCards = DEFAULT_INVERT_CARDS;

        // Parse other options
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--order":
                    if (i + 1 < args.length) {
                        orderType = args[++i];
                        if (!Arrays.asList("random", "worst-first", "recent-mistakes-first").contains(orderType)) {
                            System.out.println("Error: Invalid order type: " + orderType);
                            printHelp();
                            return;
                        }
                    } else {
                        System.out.println("Error: --order requires a value.");
                        printHelp();
                        return;
                    }
                    break;
                case "--repetitions":
                    if (i + 1 < args.length) {
                        try {
                            repetitions = Integer.parseInt(args[++i]);
                            if (repetitions <= 0) {
                                System.out.println("Error: Repetitions must be a positive number.");
                                printHelp();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Repetitions must be a number.");
                            printHelp();
                            return;
                        }
                    } else {
                        System.out.println("Error: --repetitions requires a value.");
                        printHelp();
                        return;
                    }
                    break;
                case "--invertCards":
                    invertCards = true;
                    break;
                default:
                    System.out.println("Error: Unknown option: " + args[i]);
                    printHelp();
                    return;
            }
        }

        try {
            // Load cards from file
            List<Flashcard> cards = loadCardsFromFile(cardFile);
            if (cards.isEmpty()) {
                System.out.println("No cards found in file: " + cardFile);
                return;
            }

            // Set up card organizer based on order type
            CardOrganizer organizer;
            switch (orderType) {
                case "random":
                    organizer = new RandomCardOrganizer();
                    break;
                case "worst-first":
                    organizer = new WorstFirstCardOrganizer();
                    break;
                case "recent-mistakes-first":
                    organizer = new RecentMistakesFirstSorter();
                    break;
                default:
                    organizer = new RandomCardOrganizer();
            }

            // Start the flashcard session
            FlashcardSession session = new FlashcardSession(cards, organizer, repetitions, invertCards);
            session.start();

        } catch (IOException e) {
            System.out.println("Error reading card file: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Usage: flashcard <cards-file> [options]");
        System.out.println("Options:");
        System.out.println("  --help                   Display this help information");
        System.out.println("  --order <order>          Organization type, default is \"random\"");
        System.out.println("                           [options: \"random\", \"worst-first\", \"recent-mistakes-first\"]");
        System.out.println("  --repetitions <num>      How many times a card must be answered correctly");
        System.out.println("                           Default: 1");
        System.out.println("  --invertCards            If set, question and answer will be swapped");
        System.out.println("                           Default: false");
    }

    private static List<Flashcard> loadCardsFromFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filename);
        }

        List<String> lines = Files.readAllLines(path);
        List<Flashcard> cards = new ArrayList<>();

        for (int i = 0; i < lines.size(); i += 2) {
            if (i + 1 < lines.size()) {
                String question = lines.get(i).trim();
                String answer = lines.get(i + 1).trim();
                if (!question.isEmpty() && !answer.isEmpty()) {
                    cards.add(new Flashcard(question, answer));
                }
            }
        }

        return cards;
    }
}