package com.dozenflow.be.task;

import com.dozenflow.be.checklist.ChecklistItem;
import com.dozenflow.be.comment.Comment;
import com.dozenflow.be.label.Label;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    @Column(name = "list_id", nullable = false)
    private Long listId;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(name = "task_order")
    private int taskOrder;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // Cor de capa do cartão (hex, ex.: "#0079bf"), estilo Trello. Nulo = sem capa.
    @Column(name = "cover_color")
    private String coverColor;

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

    // Read-only from Task's side: checklist items are created/updated/deleted
    // through ChecklistItemService directly (each sets its own `task`
    // reference), not via this collection. It only exists here so TaskMapper
    // can compute checklistTotal/checklistDone without a separate query.
    // Same EAGER + SUBSELECT reasoning as `labels` above.
    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("itemOrder ASC")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<ChecklistItem> checklistItems = new ArrayList<>();

    // Read-only from Task's side, same reasoning as checklistItems above:
    // comments are managed through CommentService, this collection only
    // exists so TaskMapper can compute commentCount.
    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    // Attachments can hold up to 5MB of bytes each, so — unlike labels/
    // checklistItems/comments above — loading them as an entity collection
    // just to count them would pull megabytes of blob data into memory on
    // every board fetch. A correlated-subquery formula gets the count in
    // the same query as the rest of the row, without ever touching
    // Attachment.data. Read-only: Hibernate excludes @Formula fields from
    // INSERT/UPDATE automatically.
    @Formula("(SELECT COUNT(*) FROM attachments a WHERE a.task_id = id)")
    private int attachmentCount;

}