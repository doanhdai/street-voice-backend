package com.foodstreet.voice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    private static final String MYMEMORY_URL =
            "https://api.mymemory.translated.net/get?q={text}&langpair={langpair}";

    private final RestTemplate restTemplate;

    /**
     * Dich van ban tu tieng Viet sang ngon ngu dich.
     *
     * @param text       Van ban tieng Viet can dich
     * @param targetLang Ma ngon ngu dich (en, ja, ko, zh)
     * @return Van ban da dich, hoac text goc neu loi
     */
    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank() || "vi".equalsIgnoreCase(targetLang)) {
            return text;
        }

        // MyMemory has a 500 character limit for free tier.
        // We split into chunks of ~450 chars to be safe.
        if (text.length() <= 450) {
            return callTranslationApi(text, targetLang);
        }

        log.info("[Translation] Text too long ({} chars), splitting into chunks", text.length());
        StringBuilder translatedResult = new StringBuilder();
        
        // Simple splitting by length, trying to avoid cutting in the middle of a word
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + 450, text.length());
            
            // Try to find a space or punctuation to split at
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }
            
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                String translatedChunk = callTranslationApi(chunk, targetLang);
                translatedResult.append(translatedChunk).append(" ");
            }
            
            start = end;
        }

        return translatedResult.toString().trim();
    }

    private String callTranslationApi(String text, String targetLang) {
        try {
            String langpair = "vi|" + targetLang;

            @SuppressWarnings("rawtypes")
            Map response = restTemplate.getForObject(
                    MYMEMORY_URL,
                    Map.class,
                    text, langpair
            );

            if (response != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseData = (Map<String, Object>) response.get("responseData");
                if (responseData != null) {
                    String translated = (String) responseData.get("translatedText");
                    if (translated != null && !translated.isBlank()) {
                        return translated;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[Translation] MyMemory API failed for lang={}: {}", targetLang, e.getMessage());
        }

        // Fallback: tra ve text goc
        return text;
    }
}
