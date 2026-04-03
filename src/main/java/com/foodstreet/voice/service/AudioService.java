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
import java.util.Comparator;
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
        return audioProperties.getResolvedLocalPath();
    }

    public String getOrCreateAudio(@NonNull String text, @NonNull String languageCode) {
        String hash = DigestUtils.md5DigestAsHex(text.getBytes());
        String fileName = hash + "_" + languageCode + ".mp3";
        return getOrCreateAudioInternal(fileName, text, languageCode, false);
    }

    public String getOrCreateAudioForStall(@NonNull Long stallId, @NonNull String text, @NonNull String languageCode) {
<<<<<<< HEAD
        return getOrCreateAudioForStall(stallId, text, languageCode, false);
    }

    public String getOrCreateAudioForStall(@NonNull Long stallId, @NonNull String text, @NonNull String languageCode, boolean forceRegenerate) {
        String fileName = stallId + "_" + languageCode + ".mp3";
        return getOrCreateAudioInternal(fileName, text, languageCode, forceRegenerate);
    }

    private String getOrCreateAudioInternal(String fileName, String text, String languageCode, boolean forceRegenerate) {
=======
        String fileName = stallId + "_" + languageCode + ".mp3";
        return getOrCreateAudioInternal(fileName, text, languageCode, false);
    }

    /**
     * Generate audio and overwrite the existing file (if any).
     * Used after admin approval to ensure the mp3 matches the latest approved content.
     */
    public String generateAndOverwriteAudioForStall(@NonNull Long stallId, @NonNull String text, @NonNull String languageCode) {
        String fileName = stallId + "_" + languageCode + ".mp3";
        return getOrCreateAudioInternal(fileName, text, languageCode, true);
    }

    /**
     * Generate a new versioned audio file for a stall and remove stale files of the same language.
     * This avoids CDN/browser serving a cached file with the same URL after content updates.
     */
    public String generateVersionedAudioForStall(@NonNull Long stallId, @NonNull String text, @NonNull String languageCode) {
        String fileName = stallId + "_" + languageCode + "_" + System.currentTimeMillis() + ".mp3";
        String audioUrl = getOrCreateAudioInternal(fileName, text, languageCode, false);
        if (audioUrl != null) {
            cleanupOldStallAudioVersions(stallId, languageCode, fileName);
        }
        return audioUrl;
    }

    private String getOrCreateAudioInternal(String fileName, String text, String languageCode, boolean overwrite) {
>>>>>>> 812848c8544f0f9ed1d9960c4e63f7642a8209fc
        try {
            Files.createDirectories(Paths.get(getUploadDir()));
            Path filePath = Paths.get(getUploadDir() + fileName);

<<<<<<< HEAD
            if (!forceRegenerate && Files.exists(filePath)) {
                return "/audio/" + fileName;
            }

            // Neu bat buoc tao lai, xoa file cu neu ton tai de chac chan du lieu moi
            if (forceRegenerate) {
                Files.deleteIfExists(filePath);
            }

=======
            if (!overwrite && Files.exists(filePath)) {
                return "/audio/" + fileName;
            }

             if (overwrite) {
                Files.deleteIfExists(filePath);
            }

            // Same file name is used as the task key to avoid concurrent regenerations.
>>>>>>> 812848c8544f0f9ed1d9960c4e63f7642a8209fc
            return inProgressTasks.computeIfAbsent(fileName, key -> generateAudioAsync(fileName, text, languageCode, filePath)).join();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private CompletableFuture<String> generateAudioAsync(String fileName, String text, String languageCode, Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] audioData = audioProvider.generateAudio(text, languageCode);
                FileCopyUtils.copy(audioData, filePath.toFile());
                return "/audio/" + fileName;
            } catch (Exception e) {
                throw new RuntimeException("Error generating audio", e);
            }
        }).whenComplete((result, ex) -> {
            inProgressTasks.remove(fileName);
        });
    }

    private void cleanupOldStallAudioVersions(Long stallId, String languageCode, String keepFileName) {
        String versionPrefix = stallId + "_" + languageCode + "_";
        String legacyFileName = stallId + "_" + languageCode + ".mp3";

        try (Stream<Path> stream = Files.list(Paths.get(getUploadDir()))) {
            List<Path> oldFiles = stream
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return (name.startsWith(versionPrefix) || name.equals(legacyFileName))
                                && !name.equals(keepFileName);
                    })
                    .sorted(Comparator.comparingLong(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException e) {
                            return Long.MIN_VALUE;
                        }
                    }))
                    .collect(Collectors.toList());

            for (Path oldFile : oldFiles) {
                Files.deleteIfExists(oldFile);
            }
        } catch (IOException ignored) {
            // Best effort cleanup only. Audio generation should not fail because cleanup failed.
        }
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

        // Xoa file cu neu co (file theo format moi se bi overwrite nen xoa file hash cu la chinh)
        if (stall.getAudioUrl() != null && !stall.getAudioUrl().isEmpty()) {
            String oldFileName = stall.getAudioUrl().replace("/audio/", "");
            deleteAudioFile(oldFileName);
        }

        // Tao audio moi tu description dung format stallId_lang.mp3
        String text = stall.getName() + ". " + stall.getDescription();
        String newAudioUrl = generateAndOverwriteAudioForStall(stallId, text, "vi");

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
