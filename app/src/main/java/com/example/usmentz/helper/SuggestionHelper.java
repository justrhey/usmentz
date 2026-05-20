package com.example.usmentz.helper;

import com.example.usmentz.date.DateLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes past moments to generate simple suggestions for the Guide layer.
 * Looks for patterns in feelings and suggests similar or complementary experiences.
 */
public class SuggestionHelper {

    private static final String[] SUGGESTIONS_COZY = {
        "Try a new cafe with warm lighting and comfy seats",
        "Cook a meal together with candles and soft music",
        "Build a blanket fort and watch your favorite movie"
    };

    private static final String[] SUGGESTIONS_ROMANTIC = {
        "Book a sunset walk at a spot you haven't visited",
        "Write each other love letters and read them aloud",
        "Find a rooftop or balcony for stargazing"
    };

    private static final String[] SUGGESTIONS_FUN = {
        "Try an arcade or board game cafe together",
        "Have a mini photo scavenger hunt around town",
        "Visit a local fair or pop-up event"
    };

    private static final String[] SUGGESTIONS_ADVENTUROUS = {
        "Explore a hiking trail you've never been to",
        "Try a new sport or activity together",
        "Take a day trip to a nearby town"
    };

    private static final String[] SUGGESTIONS_RELAXING = {
        "Book a couple's massage or spa afternoon",
        "Find a quiet park and bring a picnic",
        "Do a puzzle or craft project together"
    };

    private static final String[] SUGGESTIONS_EXCITING = {
        "Try an escape room or interactive experience",
        "Go to a live show or concert together",
        "Take a cooking or dance class as a pair"
    };

    private static final String[] SUGGESTIONS_DEFAULT = {
        "Revisit your favorite spot from last month",
        "Try something completely new together",
        "Plan a surprise date for each other"
    };

    public static class Suggestion {
        public final String title;
        public final String description;
        public final String feeling;

        public Suggestion(String title, String description, String feeling) {
            this.title = title;
            this.description = description;
            this.feeling = feeling;
        }
    }

    /**
     * Analyzes a list of moments and returns a suggestion based on patterns.
     */
    public static Suggestion generateSuggestion(List<DateLocation> moments) {
        if (moments == null || moments.isEmpty()) {
            return new Suggestion(
                "Start your journey together",
                "Create your first category and add your first moment. Every great story starts with a single step.",
                ""
            );
        }

        // Count feelings
        Map<String, Integer> feelingCounts = new HashMap<>();
        for (DateLocation m : moments) {
            String feeling = m.getFeeling();
            if (feeling != null && !feeling.isEmpty()) {
                feelingCounts.put(feeling, feelingCounts.getOrDefault(feeling, 0) + 1);
            }
        }

        if (feelingCounts.isEmpty()) {
            return new Suggestion(
                "Add some feelings to your moments",
                "When you capture a moment, note how it felt. We'll use that to suggest your next perfect date.",
                ""
            );
        }

        // Find most frequent feeling
        String topFeeling = Collections.max(feelingCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
        int count = feelingCounts.get(topFeeling);

        String title = String.format("You've had %d %s date%s", count, topFeeling.toLowerCase(), count > 1 ? "s" : "");
        String[] suggestions = getSuggestionsForFeeling(topFeeling);
        String description = suggestions[new java.util.Random().nextInt(suggestions.length)];

        return new Suggestion(title, description, topFeeling);
    }

    private static String[] getSuggestionsForFeeling(String feeling) {
        String lower = feeling.toLowerCase();
        if (lower.contains("cozy")) return SUGGESTIONS_COZY;
        if (lower.contains("romantic")) return SUGGESTIONS_ROMANTIC;
        if (lower.contains("fun")) return SUGGESTIONS_FUN;
        if (lower.contains("adventur")) return SUGGESTIONS_ADVENTUROUS;
        if (lower.contains("relax")) return SUGGESTIONS_RELAXING;
        if (lower.contains("excit")) return SUGGESTIONS_EXCITING;
        return SUGGESTIONS_DEFAULT;
    }
}
