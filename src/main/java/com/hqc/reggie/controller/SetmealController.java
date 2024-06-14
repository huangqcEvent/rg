package com.hqc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hqc.reggie.common.R;
import com.hqc.reggie.dto.SetmealDto;
import com.hqc.reggie.pojo.Category;
import com.hqc.reggie.pojo.Setmeal;
import com.hqc.reggie.pojo.SetmealDish;
import com.hqc.reggie.service.CategoryService;
import com.hqc.reggie.service.SetmealDishService;
import com.hqc.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*套餐管理*/
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    //新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    //分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        setmealLambdaQueryWrapper.orderByAsc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, setmealLambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
        //自己设置records
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();//分类id
            Category category = categoryService.getById(categoryId);//根据分类id查询分类对象，从而得到分类名称
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);
    }

    /*删除，或批量删除套餐*/
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        boolean x = setmealService.removeWithDish(ids);
        if (x) {
            return R.success("删除套餐成功");
        }
        return R.error("套餐删除失败，请检查是否有套餐未停售");
    }

    /*批量停售，起售，单个停起售*/
    @PostMapping("/status/{status}")
    public R<String> status(@RequestParam List<Long> ids, @PathVariable Integer status) {
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改售卖状态成功");
    }

    /*修改套餐,页面回显*/
    //套餐以及对应的菜品信息，发送的是Dto
    @GetMapping("/{id}")
    @Transactional
    public R<SetmealDto> get(@PathVariable Long id) {
        //1.先查套餐信息
        Setmeal setmeal = setmealService.getById(id);
        //2.添加条件 根据套餐id查询对应的菜品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        //3.创建Dto对象，并赋值
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);//套餐信息
        setmealDto.setSetmealDishes(setmealDishList);//套餐中菜品信息
        return R.success(setmealDto);
    }

    //修改完毕并保存，操作两张表，套餐和套餐菜品表
    @PutMapping
    @Transactional
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        //1.修改套餐表中的信息
        setmealService.updateById(setmealDto);
        //2.删除对应的套餐菜品关系表中的数据
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper =  new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        //3.添加页面提交的数据到套餐菜品关系表中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
        return R.success("修改套餐信息成功");
        }

        //？？？？
    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        //先查对应的套餐信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId()).eq(Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> list = setmealService.list(queryWrapper);

        List<SetmealDto> setmealDtoList = list.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);

            Long setmealId = item.getId();
            LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(SetmealDish::getSetmealId,setmealId);
            List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper1);
            setmealDto.setSetmealDishes(setmealDishList);
            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(setmealDtoList);
    }
       //？？？？
    @GetMapping("/dish/{id}")
    public R<List<SetmealDish>> dish(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> dishList = setmealDishService.list(queryWrapper);
        return R.success(dishList);
    }
}
