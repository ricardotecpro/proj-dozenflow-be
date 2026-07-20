package com.dozenflow.be.boardsettings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// Single fixed row (id always 1L, no @GeneratedValue) — this app has no
// Board entity (single-board kanban), so this table holds one global
// settings row instead of one per board.
//
// `backgroundImage` is deliberately plain byte[] (no @Lob), same reasoning
// as Attachment.data: Hibernate 6 maps byte[] to VARBINARY -> Postgres
// bytea, matching the migration. @Lob on byte[] would map to `oid` instead.
@Data
@Entity
@Table(name = "board_settings")
public class BoardSettings {

    @Id
    private Long id;

    // Id of one of BOARD_BACKGROUND_OPTIONS (frontend), e.g. "ocean" — always
    // an explicit id, including "default"; null means "an image is active"
    // (mutually exclusive with backgroundImage, see BoardSettingsService).
    @Column(name = "background_color_id")
    private String backgroundColorId;

    @Column(name = "background_image")
    private byte[] backgroundImage;

    @Column(name = "background_image_content_type")
    private String backgroundImageContentType;
}
