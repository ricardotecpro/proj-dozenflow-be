package com.dozenflow.be.list;

import com.dozenflow.be.task.Task;
import com.dozenflow.be.task.TaskRepository;
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
class TaskListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void getAllLists_returnsTheSeededColumns() throws Exception {
        mockMvc.perform(get("/api/lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void createList_persistsAndReturnsCreatedList() throws Exception {
        String payload = """
                {"name":"Backlog","position":3}
                """;

        mockMvc.perform(post("/api/lists")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Backlog"))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void createList_returnsBadRequest_whenNameIsBlank() throws Exception {
        String payload = """
                {"name":"","position":0}
                """;

        mockMvc.perform(post("/api/lists")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateList_renamesAndReordersExistingList() throws Exception {
        TaskList existing = new TaskList();
        existing.setName("Old name");
        existing.setPosition(0);
        existing = taskListRepository.save(existing);

        String payload = """
                {"name":"Renamed","position":5}
                """;

        mockMvc.perform(put("/api/lists/{id}", existing.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.position").value(5));
    }

    @Test
    void updateList_returnsNotFound_whenListDoesNotExist() throws Exception {
        String payload = """
                {"name":"Renamed","position":0}
                """;

        mockMvc.perform(put("/api/lists/{id}", 999_999L)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void archiveList_cascadesToItsTasks() throws Exception {
        TaskList list = new TaskList();
        list.setName("Sprint 1");
        list.setPosition(3);
        list = taskListRepository.save(list);

        Task task = new Task();
        task.setTitle("Card in the list");
        task.setListId(list.getId());
        taskRepository.save(task);

        mockMvc.perform(post("/api/lists/{id}/archive", list.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        mockMvc.perform(get("/api/lists"))
                .andExpect(jsonPath("$", hasSize(3)));
        mockMvc.perform(get("/api/lists/archived"))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/tasks"))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/tasks/archived"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void archiveList_returnsBadRequest_whenOnlyOneActiveListRemains() throws Exception {
        // The DB starts with the 3 seeded lists; archive 2 of them first so exactly 1 remains active.
        var seeded = taskListRepository.findAllByArchivedFalseOrderByPositionAsc();
        mockMvc.perform(post("/api/lists/{id}/archive", seeded.get(0).getId())).andExpect(status().isOk());
        mockMvc.perform(post("/api/lists/{id}/archive", seeded.get(1).getId())).andExpect(status().isOk());

        mockMvc.perform(post("/api/lists/{id}/archive", seeded.get(2).getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void restoreList_clearsArchivedFlag_butLeavesItsTasksArchived() throws Exception {
        TaskList list = new TaskList();
        list.setName("Sprint 1");
        list.setPosition(3);
        list.setArchived(true);
        list = taskListRepository.save(list);

        Task task = new Task();
        task.setTitle("Archived card");
        task.setListId(list.getId());
        task.setArchived(true);
        taskRepository.save(task);

        mockMvc.perform(post("/api/lists/{id}/restore", list.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(false));

        mockMvc.perform(get("/api/tasks/archived"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deleteList_permanentlyRemovesListAndItsTasks() throws Exception {
        TaskList list = new TaskList();
        list.setName("Temp list");
        list.setPosition(3);
        list.setArchived(true);
        list = taskListRepository.save(list);

        Task task = new Task();
        task.setTitle("Card to be deleted");
        task.setListId(list.getId());
        task.setArchived(true);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/lists/{id}", list.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/lists/archived"))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/tasks/archived"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteList_returnsNotFound_whenListDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/lists/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }
}
