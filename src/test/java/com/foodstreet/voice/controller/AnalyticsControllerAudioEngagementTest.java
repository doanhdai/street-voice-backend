package com.foodstreet.voice.controller;

import com.foodstreet.voice.entity.FoodStall;
import com.foodstreet.voice.entity.UserActivity;
import com.foodstreet.voice.repository.FoodStallRepository;
import com.foodstreet.voice.repository.UserActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "VIETMAP_API_KEY_SERVICES=dummy",
        "AUDIO_BASE_URL=http://localhost",
        "spring.datasource.username=postgres",
        "spring.datasource.password=password"
})
public class AnalyticsControllerAudioEngagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodStallRepository foodStallRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @MockBean
    private com.foodstreet.voice.service.VietMapSyncService vietMapSyncService;

    @BeforeEach
    void setUp() {
        userActivityRepository.deleteAll();
    }

    @Test
    @DisplayName("AE-01: Audio Engagement trả đúng plays cho 1 quán được filter")
    void shouldReturnAudioEngagementForFilteredStall() throws Exception {
        FoodStall stall = foodStallRepository.save(
                FoodStall.builder()
                        .name("Test Audio Engagement Stall")
                        .description("Stub stall for analytics test")
                        .build()
        );

        userActivityRepository.saveAll(List.of(
                createActivity(stall, UserActivity.ActionType.PLAY_AUDIO_MANUAL),
                createActivity(stall, UserActivity.ActionType.PLAY_AUDIO_MANUAL),
                createActivity(stall, UserActivity.ActionType.PLAY_AUDIO_AUTO),
                createActivity(stall, UserActivity.ActionType.PLAY_AUDIO),
                createActivity(stall, UserActivity.ActionType.SKIP_AUDIO)
        ));

        mockMvc.perform(get("/api/v1/analytics/audio-engagement")
                        .param("stallId", stall.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.stallId").value(stall.getId().intValue()))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].stallId").value(stall.getId().intValue()))
                .andExpect(jsonPath("$.data[0].stallName").value("Test Audio Engagement Stall"))
                .andExpect(jsonPath("$.data[0].plays").value(4));
    }

    private UserActivity createActivity(FoodStall stall, UserActivity.ActionType actionType) {
        return UserActivity.builder()
                .deviceId("DEVICE_AUDIO_TEST")
                .sessionId("SESSION_AUDIO_TEST")
                .platform("android")
                .foodStall(stall)
                .actionType(actionType)
                .build();
    }
}
