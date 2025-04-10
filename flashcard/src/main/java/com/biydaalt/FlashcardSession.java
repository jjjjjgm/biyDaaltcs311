package com.biydaalt;

import java.util.*;

public class FlashcardSession {
    private final List<Flashcard> originalCards;
    private List<Flashcard> currentCards;
    private final CardOrganizer organizer;
    private final int requiredRepetitions;
    private final boolean invertCards;
    private final AchievementTracker achievementTracker;
    private final Scanner scanner;
    
    private long sessionStartTime;
    private long totalAnswerTime = 0;
    private int totalAnswers = 0;
    
    public FlashcardSession(List<Flashcard> cards, CardOrganizer organizer, 
                           int requiredRepetitions, boolean invertCards) {
        this.originalCards = new ArrayList<>(cards);
        this.currentCards = new ArrayList<>(cards);
        this.organizer = organizer;
        this.requiredRepetitions = requiredRepetitions;
        this.invertCards = invertCards;
        this.achievementTracker = new AchievementTracker();
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        System.out.println("Starting flashcard session with " + originalCards.size() + " cards.");
        System.out.println("Press Enter after typing your answer.");
        System.out.println("Type 'exit' to end session.");
        
        sessionStartTime = System.currentTimeMillis();
        Map<Flashcard, Integer> correctCount = new HashMap<>();
        
        // Initialize correct count for each card
        for (Flashcard card : originalCards) {
            correctCount.put(card, 0);
        }
        
        boolean allCorrectInLastRound = true;
        
        while (!currentCards.isEmpty()) {
            // Organize cards
            currentCards = organizer.organize(currentCards);
            
            List<Flashcard> remainingCards = new ArrayList<>();
            allCorrectInLastRound = true;
            
            for (Flashcard card : currentCards) {
                // Check if we should exit
                System.out.println("\nCards remaining: " + currentCards.size());
                
                // Display the card
                String question = invertCards ? card.getAnswer() : card.getQuestion();
                String answer = invertCards ? card.getQuestion() : card.getAnswer();
                
                System.out.println("Question: " + question);
                System.out.print("Your answer: ");
                
                long startTime = System.currentTimeMillis();
                String userAnswer = scanner.nextLine();
                long endTime = System.currentTimeMillis();
                
                if ("exit".equalsIgnoreCase(userAnswer)) {
                    System.out.println("Session ended by user.");
                    return;
                }
                
                long answerTime = endTime - startTime;
                totalAnswerTime += answerTime;
                totalAnswers++;
                
                boolean isCorrect = userAnswer.trim().equalsIgnoreCase(answer.trim());
                card.recordAnswer(isCorrect, answerTime);
                
                if (isCorrect) {
                    System.out.println("Correct! (Time: " + (answerTime / 1000.0) + "s)");
                    
                    int currentCorrect = correctCount.getOrDefault(card, 0) + 1;
                    correctCount.put(card, currentCorrect);
                    
                    if (currentCorrect < requiredRepetitions) {
                        remainingCards.add(card);
                    }
                } else {
                    System.out.println("Incorrect. The correct answer was: " + answer);
                    allCorrectInLastRound = false;
                    correctCount.put(card, 0);  // Reset correct count for this card
                    remainingCards.add(card);
                }
                
                // Check achievements
                achievementTracker.checkRepeatAchievement(card);
                achievementTracker.checkConfidentAchievement(card);
                
                double averageTimeInRound = totalAnswers > 0 ? totalAnswerTime / (double)totalAnswers : 0;
                achievementTracker.checkSpeedMasterAchievement(averageTimeInRound);
            }
            
            achievementTracker.checkCorrectAchievement(allCorrectInLastRound);
            
            currentCards = remainingCards;
            
            if (!currentCards.isEmpty()) {
                System.out.println("\nStarting next round with " + currentCards.size() + " cards.");
            }
        }
        
        long sessionTime = System.currentTimeMillis() - sessionStartTime;
        
        System.out.println("\nCongratulations! You've completed all cards.");
        System.out.println("Session time: " + (sessionTime / 1000.0) + " seconds");
        System.out.println("Average answer time: " + (totalAnswerTime / totalAnswers / 1000.0) + " seconds");
        
        List<Achievement> achieved = achievementTracker.getAchievedAchievements();
        if (!achieved.isEmpty()) {
            System.out.println("\nAchievements unlocked:");
            for (Achievement achievement : achieved) {
                System.out.println("- " + achievement);
            }
        }
    }
}
