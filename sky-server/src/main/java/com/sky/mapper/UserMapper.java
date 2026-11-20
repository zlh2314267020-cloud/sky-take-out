package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from sky_take_out.user where openid=#{openid}")
    User getUserById(String openid);
    //插入数据
    void insert(User user);

    Integer countBymap(Map map);


}
