package com.hqc.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hqc.reggie.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
