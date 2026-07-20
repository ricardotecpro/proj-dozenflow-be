package com.dozenflow.be.list;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "task_lists")
public class TaskList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean archived = false;

}
