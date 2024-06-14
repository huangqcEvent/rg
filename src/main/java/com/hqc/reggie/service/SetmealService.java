package com.hqc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hqc.reggie.dto.SetmealDto;
import com.hqc.reggie.pojo.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    boolean removeWithDish(List<Long> ids);
}
