package com.investmind.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.investmind.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND deleted = 0")
    int countByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email} AND deleted = 0")
    int countByEmail(String email);
}
