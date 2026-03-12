package com.investmind.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.investmind.model.VectorIndexRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 向量索引记录 Mapper 接口
 */
@Mapper
public interface VectorIndexRecordMapper extends BaseMapper<VectorIndexRecord> {

    /**
     * 根据报告ID查询向量记录
     */
    @Select("SELECT * FROM vector_index_record WHERE report_id = #{reportId}")
    List<VectorIndexRecord> selectByReportId(@Param("reportId") Long reportId);

    /**
     * 根据向量ID查询记录
     */
    @Select("SELECT * FROM vector_index_record WHERE vector_id = #{vectorId}")
    VectorIndexRecord selectByVectorId(@Param("vectorId") String vectorId);

    /**
     * 根据内容类型查询所有记录
     */
    @Select("SELECT * FROM vector_index_record WHERE content_type = #{contentType}")
    List<VectorIndexRecord> selectByContentType(@Param("contentType") String contentType);
}
