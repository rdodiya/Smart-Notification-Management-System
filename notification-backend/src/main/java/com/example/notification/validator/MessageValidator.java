package com.example.notification.validator;

import com.example.notification.exception.InvalidMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MessageValidator {

    private static final int MAX_WORD_REPETITION = 3;

    public void validate(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new InvalidMessageException("Message cannot be empty");
        }

        validateWordRepetition(message);
    }

    private void validateWordRepetition(String message) {
        String[] words = message.toLowerCase().trim().split("\\s+");
        Map<String, Integer> wordCount = new HashMap<>();

        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);

                if (wordCount.get(word) > MAX_WORD_REPETITION) {
                    log.warn("Message validation failed - word '{}' repeated {} times",
                            word, wordCount.get(word));
                    throw new InvalidMessageException(
                            String.format("Word '%s' is repeated more than %d times",
                                    word, MAX_WORD_REPETITION)
                    );
                }
            }
        }
    }
}

