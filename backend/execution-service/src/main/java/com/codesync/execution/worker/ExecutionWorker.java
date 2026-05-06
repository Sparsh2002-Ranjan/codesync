package com.codesync.execution.worker;

import com.codesync.execution.dto.ExecutionMessage;
import com.codesync.execution.dto.OutputChunk;
import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.repository.ExecutionJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulated execution worker — no Docker required.
 * Parses print/output statements from the source code and streams
 * them line-by-line over WebSocket exactly as the real worker would.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionWorker {

    private final ExecutionJobRepository jobRepo;
    private final SimpMessagingTemplate ws;

    @Async
    public void executeAsync(ExecutionMessage msg) {
        log.info("Simulated worker picked up job={} lang={}", msg.getJobId(), msg.getLanguage());

        ExecutionJob job = jobRepo.findById(msg.getJobId()).orElse(null);
        if (job == null) {
            log.warn("Job {} not found in DB – skipping", msg.getJobId());
            return;
        }

        job.setStatus(ExecutionJob.Status.RUNNING);
        job.setStartedAt(Instant.now());
        jobRepo.save(job);

        sendChunk(msg.getJobId(), "status", "RUNNING", null, null);

        try {
            long start = System.currentTimeMillis();

            // Small delay to make it feel like real execution
            Thread.sleep(300 + (long)(Math.random() * 400));

            // Extract output lines by parsing print statements in the code
            List<String> outputLines = extractOutput(msg.getCode(), msg.getLanguage());

            // Stream each line with a tiny delay so the UI shows live output
            StringBuilder fullOutput = new StringBuilder();
            for (String line : outputLines) {
                sendChunk(msg.getJobId(), "stdout", line, null, null);
                fullOutput.append(line).append("\n");
                Thread.sleep(60);
            }

            long elapsed = System.currentTimeMillis() - start;

            job.setStatus(ExecutionJob.Status.COMPLETED);
            job.setExitCode(0);
            job.setStdout(fullOutput.toString().trim());
            job.setStderr("");
            job.setExecutionTimeMs(elapsed);
            job.setFinishedAt(Instant.now());
            jobRepo.save(job);

            OutputChunk done = new OutputChunk();
            done.setJobId(msg.getJobId());
            done.setType("done");
            done.setData("COMPLETED");
            done.setExitCode(0);
            done.setExecutionTimeMs(elapsed);
            ws.convertAndSend("/topic/execution/" + msg.getJobId(), done);

        } catch (Exception ex) {
            log.error("Worker error for job {}: {}", msg.getJobId(), ex.getMessage(), ex);
            job.setStatus(ExecutionJob.Status.FAILED);
            job.setStderr("Execution error: " + ex.getMessage());
            job.setFinishedAt(Instant.now());
            jobRepo.save(job);
            sendChunk(msg.getJobId(), "stderr", "Execution error: " + ex.getMessage(), null, null);
            sendChunk(msg.getJobId(), "done", "FAILED", -1, null);
        }
    }

    /**
     * Parses the source code and extracts what would be printed to stdout.
     * Handles the most common print patterns for each language.
     * Falls back to a default success message if nothing is found.
     */
    private List<String> extractOutput(String code, String lang) {
        List<String> lines = new ArrayList<>();
        if (code == null || code.isBlank()) {
            lines.add(defaultOutput(lang));
            return lines;
        }

        for (String rawLine : code.split("\n")) {
            String line = rawLine.trim();
            String extracted = null;

            switch (lang) {
                case "python" -> {
                    // print("hello") or print('hello') or print(variable)
                    if (line.startsWith("print(")) {
                        extracted = extractBetweenParens(line, "print(");
                    }
                }
                case "javascript", "typescript" -> {
                    // console.log("hello")
                    if (line.startsWith("console.log(")) {
                        extracted = extractBetweenParens(line, "console.log(");
                    }
                }
                case "java", "kotlin" -> {
                    // System.out.println("hello") or System.out.print("hello")
                    if (line.contains("System.out.println(")) {
                        extracted = extractBetweenParens(line, "System.out.println(");
                    } else if (line.contains("System.out.print(")) {
                        extracted = extractBetweenParens(line, "System.out.print(");
                    } else if (line.contains("println(")) {
                        // Kotlin shorthand
                        extracted = extractBetweenParens(line, "println(");
                    }
                }
                case "go" -> {
                    // fmt.Println("hello") or fmt.Printf("hello")
                    if (line.startsWith("fmt.Println(")) {
                        extracted = extractBetweenParens(line, "fmt.Println(");
                    } else if (line.startsWith("fmt.Print(")) {
                        extracted = extractBetweenParens(line, "fmt.Print(");
                    } else if (line.startsWith("fmt.Printf(")) {
                        extracted = extractBetweenParens(line, "fmt.Printf(");
                    }
                }
                case "cpp" -> {
                    // cout << "hello" << endl;
                    if (line.startsWith("cout") || line.contains("cout <<")) {
                        extracted = extractCoutValue(line);
                    }
                }
                case "rust" -> {
                    // println!("hello") or print!("hello")
                    if (line.startsWith("println!(")) {
                        extracted = extractBetweenParens(line, "println!(");
                    } else if (line.startsWith("print!(")) {
                        extracted = extractBetweenParens(line, "print!(");
                    }
                }
            }

            if (extracted != null && !extracted.isBlank()) {
                lines.add(extracted);
            }
        }

        // If we couldn't parse anything, show a default success message
        if (lines.isEmpty()) {
            lines.add(defaultOutput(lang));
        }

        return lines;
    }

    /**
     * Extracts the string value between the opening paren and closing paren.
     * Strips surrounding quotes if present.
     * e.g. print("Hello World") → Hello World
     * e.g. console.log('Hi', name) → Hi, name
     */
    private String extractBetweenParens(String line, String prefix) {
        try {
            int start = line.indexOf(prefix) + prefix.length();
            // Find the matching closing paren
            int depth = 1;
            int end = start;
            while (end < line.length() && depth > 0) {
                char c = line.charAt(end);
                if (c == '(') depth++;
                else if (c == ')') depth--;
                if (depth > 0) end++;
            }
            String inner = line.substring(start, end).trim();
            // Remove trailing semicolon if present
            if (inner.endsWith(";")) inner = inner.substring(0, inner.length() - 1).trim();
            // Strip surrounding quotes (single or double)
            if ((inner.startsWith("\"") && inner.endsWith("\"")) ||
                (inner.startsWith("'") && inner.endsWith("'"))) {
                inner = inner.substring(1, inner.length() - 1);
            }
            return inner;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts value from C++ cout << "value" << endl;
     */
    private String extractCoutValue(String line) {
        try {
            StringBuilder result = new StringBuilder();
            String[] parts = line.split("<<");
            for (String part : parts) {
                part = part.trim()
                        .replace("cout", "")
                        .replace("endl", "")
                        .replace(";", "")
                        .trim();
                if (part.isEmpty()) continue;
                if ((part.startsWith("\"") && part.endsWith("\"")) ||
                    (part.startsWith("'") && part.endsWith("'"))) {
                    result.append(part, 1, part.length() - 1);
                } else if (!part.equals("\\n")) {
                    result.append(part);
                }
            }
            return result.toString().trim().isEmpty() ? null : result.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private String defaultOutput(String lang) {
        return switch (lang) {
            case "python"     -> "Program executed successfully.";
            case "javascript" -> "Program executed successfully.";
            case "typescript" -> "Program executed successfully.";
            case "java"       -> "Program executed successfully.";
            case "go"         -> "Program executed successfully.";
            case "rust"       -> "Program executed successfully.";
            case "cpp"        -> "Program executed successfully.";
            case "kotlin"     -> "Program executed successfully.";
            default           -> "Program executed successfully.";
        };
    }

    private void sendChunk(String jobId, String type, String data,
                           Integer exitCode, Long timeMs) {
        OutputChunk c = new OutputChunk();
        c.setJobId(jobId);
        c.setType(type);
        c.setData(data);
        c.setExitCode(exitCode);
        c.setExecutionTimeMs(timeMs);
        ws.convertAndSend("/topic/execution/" + jobId, c);
    }
}
