package com.dozenflow.be.task;

import com.dozenflow.be.label.Label;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "task_order")
    private int taskOrder;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // EAGER (not the JPA default LAZY) because open-in-view is disabled:
    // TaskMapper reads this collection from the controller layer, after the
    // service's transaction/session has already closed. FetchMode.SUBSELECT
    // turns what would otherwise be an EAGER N+1 (one extra query per task)
    // into a single extra query for the whole result set.
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(
            name = "task_labels",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Label> labels = new LinkedHashSet<>();

}