package com.investmind.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.investmind.model.InvestReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 投资报告 Mapper 接口
 */
@Mapper
public interface InvestReportMapper extends BaseMapper<InvestReport> {

    /**
     * 根据日期范围查询报告
     */
    @Select("SELECT * FROM invest_report WHERE deleted = 0 AND user_id = #{userId} AND report_date BETWEEN #{startDate} AND #{endDate} ORDER BY report_date DESC")
    List<InvestReport> selectByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询需要向量化的报告（已人工修正但未向量化）
     */
    @Select("SELECT * FROM invest_report WHERE deleted = 0 AND user_id = #{userId} AND human_content IS NOT NULL AND is_vectorized = 0")
    List<InvestReport> selectNeedVectorizeReports(@Param("userId") Long userId);

    /**
     * 查询可用于学习的报告（已向量化但未用于学习）
     */
    @Select("SELECT * FROM invest_report WHERE deleted = 0 AND user_id = #{userId} AND is_vectorized = 1 AND is_used_for_learning = 0")
    List<InvestReport> selectReportsForLearning(@Param("userId") Long userId);

    /**
     * 分页查询报告列表
     */
    IPage<InvestReport> selectReportPage(Page<InvestReport> page,
                                         @Param("userId") Long userId,
                                         @Param("reportType") String reportType,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 更新向量化状态
     */
    @Update("UPDATE invest_report SET is_vectorized = 1, vectorized_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int updateVectorizedStatus(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 更新学习状态
     */
    @Update("UPDATE invest_report SET is_used_for_learning = 1, learning_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int updateLearningStatus(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询指定日期的报告
     */
    @Select("SELECT * FROM invest_report WHERE deleted = 0 AND user_id = #{userId} AND report_date = #{date} LIMIT 1")
    InvestReport selectByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询指定用户指定日期的报告
     */
    @Select("SELECT * FROM invest_report WHERE deleted = 0 AND user_id = #{userId} AND report_date = #{date} AND report_type = #{reportType} LIMIT 1")
    InvestReport selectByUserIdAndDateAndType(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("reportType") String reportType);
}
