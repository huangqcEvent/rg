package com.hqc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hqc.reggie.dto.DishDto;
import com.hqc.reggie.pojo.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品口味数据，操作两张表
    public void savaWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新口味信息
    public void updateWithFlavor(DishDto dishDto);

    //删除菜品信息，同时删除口味信息
//    void deleteWithFlavor(Long ids);
    //删除菜品信息，同时删除口味信息
    boolean deleteWithFlavors(List<Long> ids);
}
