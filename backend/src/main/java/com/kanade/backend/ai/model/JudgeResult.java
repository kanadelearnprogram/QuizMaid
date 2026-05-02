package com.kanade.backend.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("")
public class JudgeResult {
    Integer score;
    String reason;
}
