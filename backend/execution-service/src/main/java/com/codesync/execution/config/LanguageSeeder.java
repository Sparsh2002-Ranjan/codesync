package com.codesync.execution.config;

import com.codesync.execution.entity.SupportedLanguage;
import com.codesync.execution.repository.SupportedLanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LanguageSeeder implements CommandLineRunner {

    private final SupportedLanguageRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        log.info("Seeding supported execution languages...");

        // FIX 4: method signature is lang(id, name, version, ext, image, compile, run)
        // Rust and C++ had compile/run arguments in the wrong order in the original seeder.
        // Also matched exactly with execution_schema.sql seed values.
        List<SupportedLanguage> langs = List.of(
            lang("python",     "Python",     "3.11",    ".py",   "python:3.11-slim",
                 null,
                 "python {file}"),

            lang("javascript", "JavaScript", "Node 20", ".js",   "node:20-slim",
                 null,
                 "node {file}"),

            lang("typescript", "TypeScript", "5.x",     ".ts",   "node:20-slim",
                 null,
                 "npx ts-node {file}"),

            lang("java",       "Java",       "21",      ".java", "openjdk:21-slim",
                 "javac {file}",
                 "java {classname}"),

            lang("go",         "Go",         "1.21",    ".go",   "golang:1.21-slim",
                 null,
                 "go run {file}"),

            // FIX 4: compile = "rustc {file} -o /tmp/program", run = "/tmp/program"
            lang("rust",       "Rust",       "1.75",    ".rs",   "rust:1.75-slim",
                 "rustc {file} -o /tmp/program",
                 "/tmp/program"),

            // FIX 4: compile = "g++ ...", run = "/tmp/program"
            lang("cpp",        "C++",        "C++17",   ".cpp",  "gcc:13-slim",
                 "g++ -std=c++17 {file} -o /tmp/program",
                 "/tmp/program"),

            lang("kotlin",     "Kotlin",     "1.9",     ".kt",   "openjdk:21-slim",
                 "kotlinc {file} -include-runtime -d /tmp/program.jar",
                 "java -jar /tmp/program.jar")
        );
        repo.saveAll(langs);
        log.info("Seeded {} languages.", langs.size());
    }

    /**
     * @param compile compile command template — null for interpreted languages
     * @param run     run command template — always required
     */
    private SupportedLanguage lang(String id, String name, String version, String ext,
                                   String image, String compile, String run) {
        SupportedLanguage l = new SupportedLanguage();
        l.setId(id);
        l.setName(name);
        l.setVersion(version);
        l.setExtension(ext);
        l.setSandboxImage(image);
        l.setCompileCommand(compile);
        l.setRunCommand(run);
        l.setEnabled(true);
        return l;
    }
}
