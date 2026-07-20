package com.dozenflow.be.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByArchivedFalseOrderByTaskOrderAsc();
    List<Task> findAllByArchivedTrueOrderByTaskOrderAsc();

    // flushAutomatically is required, not just clearAutomatically: without it, a pending
    // (unflushed) change to another managed entity in the same transaction — e.g. the
    // TaskList.archived flip that TaskListService.archive() saves right before calling this —
    // gets silently discarded by the clear() below instead of being written first.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.archived = true WHERE t.listId = :listId AND t.archived = false")
    int archiveAllByListId(@Param("listId") Long listId);
}
