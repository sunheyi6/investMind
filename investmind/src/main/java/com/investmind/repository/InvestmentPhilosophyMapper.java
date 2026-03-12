package com.investmind.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.investmind.model.InvestmentPhilosophy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 投资理念配置Mapper接口
 */
@Mapper
public interface InvestmentPhilosophyMapper extends BaseMapper<InvestmentPhilosophy> {

    /**
     * 根据用户ID查询投资理念配置
     *
     * @param userId 用户ID
     * @return 投资理念配置
     */
    @Select("SELECT * FROM user_investment_philosophy WHERE user_id = #{userId}")
    InvestmentPhilosophy selectByUserId(Long userId);

    /**
     * 检查用户是否已有理念配置
     *
     * @param userId 用户ID
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM user_investment_philosophy WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
