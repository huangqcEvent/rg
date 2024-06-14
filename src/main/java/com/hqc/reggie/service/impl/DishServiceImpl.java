package com.hqc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hqc.reggie.dto.DishDto;
import com.hqc.reggie.mapper.DishMapper;
import com.hqc.reggie.pojo.Dish;
import com.hqc.reggie.pojo.DishFlavor;
import com.hqc.reggie.service.DishFlavorService;
import com.hqc.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
//    新增菜品，同时保存对应的口味数据
    @Transactional//操作多表，开启事务
    public void savaWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到菜品表
        this.save(dishDto);
        Long dishId = dishDto.getId();//菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor dishFlavor : flavors) {
            dishFlavor.setDishId(dishId);
        }
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);//saveBatch批量保存
    }

    //根据id查询菜品信息和对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        //查询当前菜品对应口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavorList);
        return dishDto;
    }

    //更新菜品信息，同时更新口味信息

    @Override
    @Transactional//事务
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据--dishflavor的delet操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据--dishflavor的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    //    @Override
//    @Transactional
//    public void deleteWithFlavor(Long ids) {
//        this.removeById(ids);
//        //清理当前菜品对应的口味数据
//        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DishFlavor::getDishId,ids);
//        dishFlavorService.remove(queryWrapper);
//    }
//批量删除和删除，删除菜品信息，同时删除口味信息
    @Override
    @Transactional
    public boolean deleteWithFlavors(List<Long> ids) {
        //先检查里边是否存在没有停售的商品，如果有之间返回false
        for (Long id1:ids){
            Dish dish = this.getById(id1);
            int x = dish.getStatus();
            if(x!=0){
                return false;
            }
        }
        for (Long id : ids) {
            Dish dish = this.getById(id);
            int x = dish.getStatus();
                this.removeById(id);
                LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(DishFlavor::getDishId, id);
                dishFlavorService.remove(queryWrapper);
        }
        return true;
    }
}