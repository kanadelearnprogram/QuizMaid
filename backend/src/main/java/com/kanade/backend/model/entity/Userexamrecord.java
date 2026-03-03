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
 * 用户考试记录表 实体类。
 *
 * @author kanade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("userexamrecord")
public class Userexamrecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考试记录ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 试卷ID
     */
    @Column("paperId")
    private Long paperId;

    /**
     * 试卷总分
     */
    @Column("totalScore")
    private Integer totalScore;

    /**
     * 用户得分
     */
    @Column("userScore")
    private Integer userScore;

    /**
     * 状态 0-未完成 1-已完成 2-已批改
     */
    private Integer status;

    /**
     * 开始答题时间
     */
    @Column("startTime")
    private LocalDateTime startTime;

    /**
     * 交卷时间
     */
    @Column("endTime")
    private LocalDateTime endTime;

    @Column("createTime")
    private LocalDateTime createTime;

    @Column("updateTime")
    private LocalDateTime updateTime;

    @Column(value = "isDeleted", isLogicDelete = true)
    private Integer isDeleted;

}
