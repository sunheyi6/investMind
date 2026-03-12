package com.investmind.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告修正记录实体类
 * 记录每次修正的详细变更
 */
@Data
@TableName("report_correction_log")
public class ReportCorrectionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的报告ID
     */
    private Long reportId;

    /**
     * 修正的字段名
     */
    private String fieldName;

    /**
     * 修正前的值
     */
    private String oldValue;

    /**
     * 修正后的值
     */
    private String newValue;

    /**
     * 修正人
     */
    private String correctedBy;

    /**
     * 修正时间
     */
    private LocalDateTime correctTime;

    /**
     * 修正原因
     */
    private String correctionReason;
}
