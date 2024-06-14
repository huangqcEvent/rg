package com.hqc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hqc.reggie.dto.SetmealDto;
import com.hqc.reggie.mapper.SetmealMapper;
import com.hqc.reggie.pojo.Setmeal;
import com.hqc.reggie.pojo.SetmealDish;
import com.hqc.reggie.service.SetmealDishService;
import com.hqc.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    @Transactional
    //新增套餐
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，
        this.save(setmealDto);
        //保存套餐和菜品的关联信息，操作setmealdish表
        List<SetmealDish> dishList = setmealDto.getSetmealDishes();
        dishList.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishList);
    }

    @Override
    @Transactional
//删除，批量删除套餐
    //只能删除已经停售的套餐
    public boolean removeWithDish(List<Long> ids) {
        for (Long id:ids) {
            Setmeal setmeal = setmealService.getById(id);
            if (setmeal.getStatus() == 1) {
                return false;
            }
        }
        setmealService.removeWithDish(ids);
        for (Long id1:ids) {
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id1);
            setmealDishService.remove(setmealDishLambdaQueryWrapper);
           }
        return true;
        }
    }

