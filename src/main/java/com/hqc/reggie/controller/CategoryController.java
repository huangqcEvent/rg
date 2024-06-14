package com.hqc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hqc.reggie.common.R;
import com.hqc.reggie.pojo.Category;
import com.hqc.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//分类管理页面
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    /*新增分类*/
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /*分页查询*/
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page={},pageSize={}",page,pageSize);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //添加排序条件,根据sort升序
        queryWrapper.orderByDesc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /*删除分类*/
    /*根据id删除分类*/
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除id为:{}的分类",ids);
//        categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("分类删除成功");
    }
    /*根据id修改分类信息*/
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息{}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

//根据条件查询分类数据
    //什么页面用
    //在菜品管理页面，当修改菜品信息时，用于页面回显使用
    //在套餐管理页面，修改套餐信息时，用于页面回显使用
    //在用户端，页面回显使用，显示所有分类信息
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getCreateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
