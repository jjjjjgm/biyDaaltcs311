package com.biydaalt;

import java.util.*;
import java.util.stream.Collectors;


public class Flashcard {
    private String question;
    private String answer;
    private int timesAnswered = 0;
    private int timesCorrect = 0;
    private boolean lastAnswerCorrect = false;
    private List<Long> answerTimes = new ArrayList<>();

    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public void recordAnswer(boolean correct, long timeInMillis) {
        timesAnswered++;
        if (correct) {
            timesCorrect++;
        }
        lastAnswerCorrect = correct;
        answerTimes.add(timeInMillis);
    }

    public int getConsecutiveCorrectAnswers() {
        int consecutive = 0;
        for (int i = timesAnswered - 1; i >= 0; i--) {
            if (i < timesAnswered && lastAnswerCorrect) {
                consecutive++;
            } else {
                break;
            }
        }
        return consecutive;
    }

    public boolean wasLastAnswerCorrect() {
        return lastAnswerCorrect;
    }

    public int getTimesAnswered() {
        return timesAnswered;
    }

    public int getTimesCorrect() {
        return timesCorrect;
    }

    public double getCorrectRatio() {
        return timesAnswered == 0 ? 0 : (double) timesCorrect / timesAnswered;
    }

    public long getAverageAnswerTime() {
        if (answerTimes.isEmpty()) {
            return 0;
        }
        return answerTimes.stream().mapToLong(Long::longValue).sum() / answerTimes.size();
    }
}


interface CardOrganizer {
    List<Flashcard> organize(List<Flashcard> cards);
}

class RandomCardOrganizer implements CardOrganizer {
    @Override
    public List<Flashcard> organize(List<Flashcard> cards) {
        List<Flashcard> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        return shuffled;
    }
}

class WorstFirstCardOrganizer implements CardOrganizer {
    @Override
    public List<Flashcard> organize(List<Flashcard> cards) {
        List<Flashcard> sorted = new ArrayList<>(cards);
        sorted.sort(Comparator.comparingDouble(Flashcard::getCorrectRatio));
        return sorted;
    }
}

class RecentMistakesFirstSorter implements CardOrganizer {
    @Override
    public List<Flashcard> organize(List<Flashcard> cards) {
        // Sort cards so that incorrectly answered cards come first
        // while maintaining relative order within correct and incorrect groups
        List<Flashcard> incorrectCards = new ArrayList<>();
        List<Flashcard> correctCards = new ArrayList<>();
        
        for (Flashcard card : cards) {
            if (card.getTimesAnswered() > 0 && !card.wasLastAnswerCorrect()) {
                incorrectCards.add(card);
            } else {
                correctCards.add(card);
            }
        }
        
        // Combine lists with incorrect cards first
        List<Flashcard> result = new ArrayList<>(cards.size());
        result.addAll(incorrectCards);
        result.addAll(correctCards);
        
        return result;
    }
}

// Achievement class for tracking user achievements
class Achievement {
    public static final String SPEED_MASTER = "SPEED_MASTER";
    public static final String CORRECT = "CORRECT";
    public static final String REPEAT = "REPEAT";
    public static final String CONFIDENT = "CONFIDENT";
    
    private final String name;
    private final String description;
    private boolean achieved = false;
    
    public Achievement(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isAchieved() {
        return achieved;
    }
    
    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }
    
    @Override
    public String toString() {
        return name + ": " + description;
    }
}

// Achievement tracker for managing achievements
class AchievementTracker {
    private final Map<String, Achievement> achievements = new HashMap<>();
    
    public AchievementTracker() {
        // Initialize achievements
        achievements.put(Achievement.SPEED_MASTER, 
                new Achievement(Achievement.SPEED_MASTER, "Average response time under 5 seconds in a round"));
        achievements.put(Achievement.CORRECT, 
                new Achievement(Achievement.CORRECT, "All cards answered correctly in the latest round"));
        achievements.put(Achievement.REPEAT, 
                new Achievement(Achievement.REPEAT, "Answered a card more than 5 times"));
        achievements.put(Achievement.CONFIDENT, 
                new Achievement(Achievement.CONFIDENT, "Answered a card correctly at least 3 times"));
    }
    
    public void checkSpeedMasterAchievement(double averageTime) {
        if (averageTime < 5000) { // 5 seconds in milliseconds
            Achievement achievement = achievements.get(Achievement.SPEED_MASTER);
            if (!achievement.isAchieved()) {
                achievement.setAchieved(true);
                System.out.println("Achievement unlocked: " + achievement);
            }
        }
    }
    
    public void checkCorrectAchievement(boolean allCorrect) {
        if (allCorrect) {
            Achievement achievement = achievements.get(Achievement.CORRECT);
            if (!achievement.isAchieved()) {
                achievement.setAchieved(true);
                System.out.println("Achievement unlocked: " + achievement);
            }
        }
    }
    
    public void checkRepeatAchievement(Flashcard card) {
        if (card.getTimesAnswered() > 5) {
            Achievement achievement = achievements.get(Achievement.REPEAT);
            if (!achievement.isAchieved()) {
                achievement.setAchieved(true);
                System.out.println("Achievement unlocked: " + achievement);
            }
        }
    }
    
    public void checkConfidentAchievement(Flashcard card) {
        if (card.getTimesCorrect() >= 3) {
            Achievement achievement = achievements.get(Achievement.CONFIDENT);
            if (!achievement.isAchieved()) {
                achievement.setAchieved(true);
                System.out.println("Achievement unlocked: " + achievement);
            }
        }
    }
    
    public List<Achievement> getAchievedAchievements() {
        return achievements.values().stream()
                .filter(Achievement::isAchieved)
                .collect(Collectors.toList());
    }
}