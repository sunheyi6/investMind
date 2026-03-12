package com.investmind.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.investmind.model.ReportCorrectionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 报告修正记录 Mapper 接口
 */
@Mapper
public interface ReportCorrectionLogMapper extends BaseMapper<ReportCorrectionLog> {

    /**
     * 根据报告ID查询修正记录
     */
    @Select("SELECT * FROM report_correction_log WHERE report_id = #{reportId} ORDER BY correct_time DESC")
    List<ReportCorrectionLog> selectByReportId(@Param("reportId") Long reportId);
}
