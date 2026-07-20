package com.dozenflow.be.label;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = "rate-limit.max-requests-per-window=100000")
@AutoConfigureMockMvc
@Transactional
class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Test
    void getAllLabels_returnsTheSeededTrelloPalette() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)));
    }

    @Test
    void createLabel_persistsAndReturnsCreatedLabel() throws Exception {
        String payload = """
                {"name":"Urgente","colorHex":"#eb5a46"}
                """;

        mockMvc.perform(post("/api/labels")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Urgente"))
                .andExpect(jsonPath("$.colorHex").value("#eb5a46"));
    }

    @Test
    void createLabel_returnsBadRequest_whenColorHexIsInvalid() throws Exception {
        String payload = """
                {"name":"Urgente","colorHex":"not-a-color"}
                """;

        mockMvc.perform(post("/api/labels")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLabel_updatesExistingLabel() throws Exception {
        Label existing = new Label();
        existing.setColorHex("#61bd4f");
        existing = labelRepository.save(existing);

        String payload = """
                {"name":"Renomeada","colorHex":"#0079bf"}
                """;

        mockMvc.perform(put("/api/labels/{id}", existing.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renomeada"))
                .andExpect(jsonPath("$.colorHex").value("#0079bf"));
    }

    @Test
    void updateLabel_returnsNotFound_whenLabelDoesNotExist() throws Exception {
        String payload = """
                {"name":"Renomeada","colorHex":"#0079bf"}
                """;

        mockMvc.perform(put("/api/labels/{id}", 999_999L)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLabel_removesExistingLabel() throws Exception {
        Label existing = new Label();
        existing.setColorHex("#61bd4f");
        existing = labelRepository.save(existing);

        mockMvc.perform(delete("/api/labels/{id}", existing.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLabel_returnsNotFound_whenLabelDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/labels/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }
}
