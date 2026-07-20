package com.dozenflow.be.boardsettings;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = "rate-limit.max-requests-per-window=100000")
@AutoConfigureMockMvc
@Transactional
class BoardSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void get_returnsSeededDefault_whenNothingChangedYet() throws Exception {
        mockMvc.perform(get("/api/board-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backgroundColorId").value("ocean"))
                .andExpect(jsonPath("$.hasBackgroundImage").value(false));
    }

    @Test
    void updateColor_setsColorAndClearsAnyImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "beach.png", "image/png", "fake-bytes".getBytes());
        mockMvc.perform(multipart("/api/board-settings/background-image").file(file))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/board-settings")
                        .contentType("application/json")
                        .content("{\"backgroundColorId\":\"forest\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backgroundColorId").value("forest"))
                .andExpect(jsonPath("$.hasBackgroundImage").value(false));

        mockMvc.perform(get("/api/board-settings/background-image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateColor_returnsBadRequest_whenBackgroundColorIdIsBlank() throws Exception {
        mockMvc.perform(put("/api/board-settings")
                        .contentType("application/json")
                        .content("{\"backgroundColorId\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadImage_setsImageAndClearsColor() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "beach.png", "image/png", "fake-bytes".getBytes());

        mockMvc.perform(multipart("/api/board-settings/background-image").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.backgroundColorId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.hasBackgroundImage").value(true));

        mockMvc.perform(get("/api/board-settings/background-image"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes("fake-bytes".getBytes()));
    }

    @Test
    void uploadImage_returnsBadRequest_whenContentTypeIsNotAnImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "fake-bytes".getBytes());

        mockMvc.perform(multipart("/api/board-settings/background-image").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getImage_returnsNotFound_whenNoImageWasEverUploaded() throws Exception {
        mockMvc.perform(get("/api/board-settings/background-image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImage_removesImageAndReturnsUpdatedSettings() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "beach.png", "image/png", "fake-bytes".getBytes());
        mockMvc.perform(multipart("/api/board-settings/background-image").file(file))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/board-settings/background-image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasBackgroundImage").value(false));

        mockMvc.perform(get("/api/board-settings/background-image"))
                .andExpect(status().isNotFound());
    }
}
