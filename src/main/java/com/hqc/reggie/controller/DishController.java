package com.hqc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hqc.reggie.common.R;
import com.hqc.reggie.dto.DishDto;
import com.hqc.reggie.pojo.*;
import com.hqc.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*菜品管理*/
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    /*新增菜品*/
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        //当前端发送的数据 后端实体类无法接受，需要拓展Dto类型的实体类来接受，通用于操作多表时使用
        log.info(dishDto.toString());
        dishService.savaWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /*分页查询方法*/
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        dishLambdaQueryWrapper.orderByAsc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(dishPage, dishLambdaQueryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        //Page中的属性
//        records 用来存放查询出来的数据
//        total 返回记录的总数
//        size 每页显示条数，默认 10
//        current 当前页,默认1
//        orders 排序字段信息
//        optimizeCountSql 自动优化 COUNT SQL,默认true
//        isSearchCount 是否进行 count 查询,默认true
//        hitCount 是否命中count缓存,默认false
        List<Dish> records = dishPage.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id

            Category category = categoryService.getById(categoryId);//根据id查询分类对象
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /*根据id查询菜品信息，页面回显*/
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*修改菜品信息*/
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品信息成功");
    }

    /*修改菜品状态,可以单个操作，可以批量操作*/
    /*菜品停售，对应的包含该菜品的套餐也要停售*/
    @PostMapping("/status/{status}")
    @Transactional
    public R<String> status(@PathVariable int status, @RequestParam List<Long> ids) {
        for (Long id:ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishService.updateById(dish);
            /*相应的查出包含此菜品的套餐，修改其状态*/
            LambdaQueryWrapper<SetmealDish> queryWrapper= new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId,id);
            List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        for (SetmealDish setmealDish:setmealDishList) {
            LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Setmeal::getId, setmealDish.getSetmealId());
            Setmeal setmealServiceOne = setmealService.getOne(queryWrapper1);
            log.info("{}",setmealServiceOne);
            setmealServiceOne.setStatus(status);
            setmealService.updateById(setmealServiceOne);
            log.info("{}",setmealServiceOne);
        } }
        return R.success("菜品状态修改成功");
    }

    /*删除菜品信息同时删除对应口味*/
//    @DeleteMapping
//    public R<String> delete(Long ids){
//        dishService.deleteWithFlavor(ids);
//        return R.success("菜品删除成功");
//    }

    /*批量删除菜品信息同时删除对应口味*/
    @DeleteMapping
    //@RequestParam请求携带的是list时使用
    public R<String> deletes(@RequestParam List<Long> ids) {
        boolean x = dishService.deleteWithFlavors(ids);
        if (x) {
            return R.success("批量删除成功");
        } else return R.error("删除失败，请检查商品是否停售");
    }

    /*根据条件查询相应菜品*/
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){//可以只接受id参数，单通用性会变差
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //查询条件
//        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
//        //查询出的都是非停售商品
//        queryWrapper.eq(Dish::getStatus,1);
//        //排序条件
//        queryWrapper.orderByAsc(Dish::getCode).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(queryWrapper);
//        return R.success(dishList);
//    }

//？？？？那里的
    //套餐管理页面 当修改套餐信息时，点击添加菜品按钮，会调用此方法，页面传的分类id，根据分类id查对应的菜品，显示在页面上
    //用户端 页面传分类id 和 状态 根据分类id和status来查询当前分类下非停售的菜品
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){//可以只接受id参数，单通用性会变差
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        //查询出的都是非停售商品
        queryWrapper.eq(Dish::getStatus,1);
        //排序条件
        queryWrapper.orderByAsc(Dish::getCode).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = dishList.stream().map((item)->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
}