package com.kanade.backend.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("AI批改结果")
public class JudgeResult {
    
    @Description("是否正确")
    private Boolean correct;
    
    @Description("得分")
    private Integer score;
    
    @Description("评语")
    private String comment;
}
