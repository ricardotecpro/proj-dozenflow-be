package com.dozenflow.be.attachment;

import com.dozenflow.be.task.Task;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

// `data` is deliberately plain byte[] (no @Lob): Hibernate 6 maps byte[]
// to VARBINARY by default, which the PostgreSQL dialect renders as
// `bytea` — the type used in the migration. @Lob on a byte[] field maps
// to `oid` on Postgres instead, a large-object reference type that
// behaves very differently (separate storage, needs its own cleanup) and
// is not what we want for capped-at-5MB files.
@Data
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Task task;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    @ToString.Exclude
    private byte[] data;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
