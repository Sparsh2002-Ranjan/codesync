package com.codesync.execution.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "execution_supported_languages")
@Data
public class SupportedLanguage {

    @Id
    private String id;          // e.g. "python"
    private String name;        // "Python"
    private String version;     // "3.11"
    private String extension;   // ".py"
    private String sandboxImage;// "python:3.11-slim"

    @Column(name = "is_enabled", columnDefinition = "TINYINT(1)")
    private boolean isEnabled = true;

    /** Shell command template. {file} is replaced with the source file name. */
    @Column(columnDefinition = "TEXT")
    private String runCommand;

    /** Optional compile step before run. Null if interpreted. */
    @Column(columnDefinition = "TEXT")
    private String compileCommand;
}
