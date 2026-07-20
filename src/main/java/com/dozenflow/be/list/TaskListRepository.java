package com.dozenflow.be.list;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    List<TaskList> findAllByArchivedFalseOrderByPositionAsc();
    List<TaskList> findAllByArchivedTrueOrderByPositionAsc();
    long countByArchivedFalse();
}
