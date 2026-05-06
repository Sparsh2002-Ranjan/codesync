package com.codesync.execution.repository;

import com.codesync.execution.entity.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, String> {

    @Query("SELECT l FROM SupportedLanguage l WHERE l.isEnabled = true")
    List<SupportedLanguage> findAllEnabled();
}
