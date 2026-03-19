package com.kanade.backend.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String taskId;          // 任务ID（UUID）
    private String filePath;         // 文件绝对路径
    private Long creatorId;          // 创建人ID
}