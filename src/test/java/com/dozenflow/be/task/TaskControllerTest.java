package com.dozenflow.be.task;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.checklist.ChecklistItemRepository;
import com.dozenflow.be.label.Label;
import com.dozenflow.be.label.LabelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
class TaskControllerTest {

    // Ids seeded by V8__add_task_lists_and_archiving.sql (1=A Fazer, 2=Em Andamento, 3=Concluída),
    // present in every environment this migration has run against.
    private static final long A_FAZER_LIST_ID = 1L;
    private static final long EM_ANDAMENTO_LIST_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Autowired
    private com.dozenflow.be.attachment.AttachmentRepository attachmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void getAllTasks_returnsEmptyList_whenNoTasksExist() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createTask_persistsAndReturnsCreatedTask() throws Exception {
        String payload = """
                {"title":"Write tests","description":"Cover the API","listId":%d,"taskOrder":1}
                """.formatted(A_FAZER_LIST_ID);

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.listId").value(A_FAZER_LIST_ID));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createTask_persistsAndReturnsDueDate_whenProvided() throws Exception {
        String payload = """
                {"title":"Plan release","description":"","listId":%d,"taskOrder":0,"dueDate":"2026-08-01"}
                """.formatted(A_FAZER_LIST_ID);

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value("2026-08-01"));
    }

    @Test
    void createTask_returnsNullDueDate_whenOmitted() throws Exception {
        String payload = """
                {"title":"No due date","description":"","listId":%d,"taskOrder":0}
                """.formatted(A_FAZER_LIST_ID);

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void createTask_returnsBadRequest_whenTitleIsBlank() throws Exception {
        String payload = """
                {"title":"","description":"No title","listId":%d,"taskOrder":1}
                """.formatted(A_FAZER_LIST_ID);

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTask_returnsNotFound_whenListDoesNotExist() throws Exception {
        String payload = """
                {"title":"Orphan task","description":"","listId":999999,"taskOrder":0}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_updatesExistingTask() throws Exception {
        Task existing = new Task();
        existing.setTitle("Original");
        existing.setListId(A_FAZER_LIST_ID);
        existing.setTaskOrder(0);
        existing = taskRepository.save(existing);

        String payload = """
                {"title":"Updated","description":"Now in progress","listId":%d,"taskOrder":2}
                """.formatted(EM_ANDAMENTO_LIST_ID);

        mockMvc.perform(put("/api/tasks/{id}", existing.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.listId").value(EM_ANDAMENTO_LIST_ID));
    }

    @Test
    void updateTask_setsCoverAttachmentAndClearsCoverColor_whenAttachmentBelongsToTask() throws Exception {
        Task existing = new Task();
        existing.setTitle("Original");
        existing.setListId(A_FAZER_LIST_ID);
        existing.setTaskOrder(0);
        existing.setCoverColor("#0079bf");
        existing = taskRepository.save(existing);

        com.dozenflow.be.attachment.Attachment attachment = new com.dozenflow.be.attachment.Attachment();
        attachment.setTask(existing);
        attachment.setFileName("cover.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachment = attachmentRepository.save(attachment);

        String payload = """
                {"title":"Original","description":"","listId":%d,"taskOrder":0,"coverAttachmentId":%d}
                """.formatted(A_FAZER_LIST_ID, attachment.getId());

        mockMvc.perform(put("/api/tasks/{id}", existing.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverAttachmentId").value(attachment.getId()))
                .andExpect(jsonPath("$.coverColor").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void updateTask_returnsNotFound_whenCoverAttachmentBelongsToAnotherTask() throws Exception {
        Task existing = new Task();
        existing.setTitle("Original");
        existing.setListId(A_FAZER_LIST_ID);
        existing.setTaskOrder(0);
        existing = taskRepository.save(existing);

        Task otherTask = new Task();
        otherTask.setTitle("Other task");
        otherTask.setListId(A_FAZER_LIST_ID);
        otherTask.setTaskOrder(0);
        otherTask = taskRepository.save(otherTask);

        com.dozenflow.be.attachment.Attachment attachment = new com.dozenflow.be.attachment.Attachment();
        attachment.setTask(otherTask);
        attachment.setFileName("cover.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachment = attachmentRepository.save(attachment);

        String payload = """
                {"title":"Original","description":"","listId":%d,"taskOrder":0,"coverAttachmentId":%d}
                """.formatted(A_FAZER_LIST_ID, attachment.getId());

        mockMvc.perform(put("/api/tasks/{id}", existing.getId())
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        String payload = """
                {"title":"Updated","description":"Now in progress","listId":%d,"taskOrder":2}
                """.formatted(EM_ANDAMENTO_LIST_ID);

        mockMvc.perform(put("/api/tasks/{id}", 999_999L)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_removesExistingTask() throws Exception {
        Task existing = new Task();
        existing.setTitle("To be deleted");
        existing.setListId(A_FAZER_LIST_ID);
        existing.setTaskOrder(0);
        existing = taskRepository.save(existing);

        mockMvc.perform(delete("/api/tasks/{id}", existing.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void archiveTask_setsArchivedAndHidesFromDefaultList() throws Exception {
        Task task = new Task();
        task.setTitle("To be archived");
        task.setListId(A_FAZER_LIST_ID);
        task = taskRepository.save(task);

        mockMvc.perform(post("/api/tasks/{id}/archive", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/tasks/archived"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void archiveTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/tasks/{id}/archive", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void restoreTask_clearsArchivedFlag() throws Exception {
        Task task = new Task();
        task.setTitle("Archived task");
        task.setListId(A_FAZER_LIST_ID);
        task.setArchived(true);
        task = taskRepository.save(task);

        mockMvc.perform(post("/api/tasks/{id}/restore", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(false));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void attachLabel_addsLabelToTaskResponse() throws Exception {
        Task task = new Task();
        task.setTitle("Labeled task");
        task.setListId(A_FAZER_LIST_ID);
        task = taskRepository.save(task);

        Label label = new Label();
        label.setColorHex("#eb5a46");
        label = labelRepository.save(label);

        mockMvc.perform(post("/api/tasks/{id}/labels/{labelId}", task.getId(), label.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(1)))
                .andExpect(jsonPath("$.labels[0].colorHex").value("#eb5a46"));
    }

    @Test
    void attachLabel_returnsNotFound_whenLabelDoesNotExist() throws Exception {
        Task task = new Task();
        task.setTitle("Task");
        task.setListId(A_FAZER_LIST_ID);
        task = taskRepository.save(task);

        mockMvc.perform(post("/api/tasks/{id}/labels/{labelId}", task.getId(), 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void detachLabel_removesLabelFromTaskResponse() throws Exception {
        Label label = new Label();
        label.setColorHex("#eb5a46");
        label = labelRepository.save(label);

        Task task = new Task();
        task.setTitle("Labeled task");
        task.setListId(A_FAZER_LIST_ID);
        task.getLabels().add(label);
        task = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/{id}/labels/{labelId}", task.getId(), label.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(0)));
    }

    @Test
    void getTask_includesChecklistTotalsFromRelatedItems() throws Exception {
        Task task = new Task();
        task.setTitle("Task with checklist");
        task.setListId(A_FAZER_LIST_ID);
        task = taskRepository.save(task);

        ChecklistItem done = new ChecklistItem();
        done.setTask(task);
        done.setTitle("Done item");
        done.setDone(true);
        checklistItemRepository.save(done);

        ChecklistItem pending = new ChecklistItem();
        pending.setTask(task);
        pending.setTitle("Pending item");
        pending.setDone(false);
        checklistItemRepository.save(pending);

        // Force a re-read from the DB: the `task` instance above is still
        // managed in this test's persistence context with its EAGER
        // checklistItems collection already resolved (empty, from before
        // the items existed) — without this, the identity map would hand
        // that stale instance back out instead of re-fetching it.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checklistTotal").value(2))
                .andExpect(jsonPath("$[0].checklistDone").value(1));
    }

    @Test
    void getTask_includesAttachmentCountFromFormula() throws Exception {
        Task task = new Task();
        task.setTitle("Task with attachment");
        task.setListId(A_FAZER_LIST_ID);
        task = taskRepository.save(task);

        com.dozenflow.be.attachment.Attachment attachment = new com.dozenflow.be.attachment.Attachment();
        attachment.setTask(task);
        attachment.setFileName("mockup.png");
        attachment.setContentType("image/png");
        attachment.setSizeBytes(10);
        attachment.setData("fake-bytes".getBytes());
        attachmentRepository.save(attachment);

        // Same identity-map staleness reasoning as the checklist test above
        // applies to @Formula fields: they're computed at load time, so a
        // cached instance won't reflect rows inserted after it was loaded.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attachmentCount").value(1));
    }
}
