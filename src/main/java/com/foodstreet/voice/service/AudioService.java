package com.foodstreet.voice.service;

import com.foodstreet.voice.entity.FoodStall;
import com.foodstreet.voice.entity.FoodStallLocalization;
import com.foodstreet.voice.exception.ResourceNotFoundException;
import com.foodstreet.voice.repository.FoodStallRepository;
import com.foodstreet.voice.repository.FoodStallLocalizationRepository;
import com.foodstreet.voice.service.audio.AudioProviderStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.foodstreet.voice.config.AudioProperties;

@Service
@RequiredArgsConstructor
public class AudioService {
    private final AudioProviderStrategy audioProvider;
    private final AudioProperties audioProperties;

    @Autowired
    @Lazy
    private FoodStallRepository foodStallRepository;

    @Autowired
    @Lazy
    private FoodStallLocalizationRepository localizationRepository;

    private final ConcurrentHashMap<String, CompletableFuture<String>> inProgressTasks = new ConcurrentHashMap<>();

    private String getUploadDir() {
        String path = audioProperties.getLocalPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    @SuppressWarnings("null")
    public String getOrCreateAudio(@NonNull String text, @NonNull String languageCode) {
        try {
            Files.createDirectories(Paths.get(getUploadDir()));
            // Dung MD5 hash de tinh ten file (khong bi collision nhu hashCode)
            String hash = DigestUtils.md5DigestAsHex(text.getBytes());
            String fileName = hash + "_" + languageCode + ".mp3";
            Path filePath = Paths.get(getUploadDir() + fileName);

            if (Files.exists(filePath)) {
                return "/audio/" + fileName;
            }

            // Use explicit cache key pattern to strictly differentiate by text hash and language
            String cacheKey = hash + "_" + languageCode;
            return inProgressTasks.computeIfAbsent(cacheKey, key -> generateAudioAsync(fileName, text, languageCode, filePath)).join();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private CompletableFuture<String> generateAudioAsync(String cacheKey, String text, String languageCode, Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] audioData = audioProvider.generateAudio(text, languageCode);
                FileCopyUtils.copy(audioData, filePath.toFile());
                return "/audio/" + cacheKey;
            } catch (Exception e) {
                throw new RuntimeException("Error generating audio", e);
            }
        }).whenComplete((result, ex) -> {
            inProgressTasks.remove(cacheKey);
        });
    }

    public List<String> listAllAudioFiles() {
        try (Stream<Path> stream = Files.list(Paths.get(getUploadDir()))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    public boolean deleteAudioFile(String fileName) {
        try {
            Path filePath = Paths.get(getUploadDir() + fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    public String regenerateAudio(Long stallId) {
        FoodStall stall = foodStallRepository.findById(stallId)
                .orElseThrow(() -> new ResourceNotFoundException("Quan an khong ton tai: " + stallId));

        // Xoa file cu neu co
        if (stall.getAudioUrl() != null && !stall.getAudioUrl().isEmpty()) {
            String oldFileName = stall.getAudioUrl().replace("/audio/", "");
            deleteAudioFile(oldFileName);
        }

        // Tao audio moi tu description
        String text = stall.getName() + ". " + stall.getDescription();
        String newAudioUrl = getOrCreateAudio(text, "vi");

        stall.setAudioUrl(newAudioUrl);
        foodStallRepository.save(stall);

        return newAudioUrl;
    }

    public List<String> getOrphanedAudioFiles() {
        List<String> allFiles = listAllAudioFiles();
        List<String> linkedFiles = foodStallRepository.findAll().stream()
                .map(FoodStall::getAudioUrl)
                .filter(url -> url != null && url.startsWith("/audio/"))
                .map(url -> url.replace("/audio/", ""))
                .collect(Collectors.toList());

        List<String> locFiles = localizationRepository.findAll().stream()
                .map(FoodStallLocalization::getAudioUrl)
                .filter(url -> url != null && url.startsWith("/audio/"))
                .map(url -> url.replace("/audio/", ""))
                .collect(Collectors.toList());

        linkedFiles.addAll(locFiles);

        return allFiles.stream()
                .filter(file -> !linkedFiles.contains(file))
                .collect(Collectors.toList());
    }
}
