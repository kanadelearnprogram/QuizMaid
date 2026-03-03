package com.kanade.backend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答题详情表 实体类。
 *
 * @author kanade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("useranswerdetail")
public class Useranswerdetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 考试记录ID
     */
    @Column("recordId")
    private Long recordId;

    /**
     * 试卷ID
     */
    @Column("paperId")
    private Long paperId;

    /**
     * 试题ID
     */
    @Column("questionId")
    private Long questionId;

    /**
     * 试题类型
     */
    @Column("questionType")
    private Integer questionType;

    /**
     * 用户答案
     */
    @Column("userAnswer")
    private String userAnswer;

    /**
     * 试题总分
     */
    @Column("questionScore")
    private Integer questionScore;

    /**
     * 实际得分
     */
    @Column("actualScore")
    private Integer actualScore;

    /**
     * 批改状态 0-待批改 1-正确 2-错误
     */
    @Column("correctStatus")
    private Integer correctStatus;

    /**
     * AI批改评语
     */
    @Column("aiReviewMsg")
    private String aiReviewMsg;

    @Column("createTime")
    private LocalDateTime createTime;

    @Column("updateTime")
    private LocalDateTime updateTime;

    @Column(value = "isDeleted", isLogicDelete = true)
    private Integer isDeleted;

}
