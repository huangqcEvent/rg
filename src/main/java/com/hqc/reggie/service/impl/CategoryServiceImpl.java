package com.hqc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hqc.reggie.common.CustomException;
import com.hqc.reggie.mapper.CategoryMapper;
import com.hqc.reggie.pojo.Category;
import com.hqc.reggie.pojo.Dish;
import com.hqc.reggie.pojo.Setmeal;
import com.hqc.reggie.service.CategoryService;
import com.hqc.reggie.service.DishService;
import com.hqc.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
//根据id删除分类，删除之前判断分类是否关联菜品或套餐
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long ids) {
        //查询当前分类是否关联了菜品，如果关联，抛出业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id查询,得出关联数量
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count1=dishService.count(dishLambdaQueryWrapper);
        if (count1>0){
            //count不为0，已经关联，抛出业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        //查询当前分类是否关联了套餐，如果关联，抛出业务异常
        LambdaQueryWrapper<Setmeal> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int count2 = setmealService.count(categoryLambdaQueryWrapper);
        if (count2>0){
            //已经关联，抛出业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //没有关联，正常删除
        //super调用父类方法
        super.removeById(ids);
    }
}
