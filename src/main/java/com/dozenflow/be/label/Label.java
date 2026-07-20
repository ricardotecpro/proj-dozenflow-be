package com.dozenflow.be.label;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

}
