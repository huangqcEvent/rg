package com.hqc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hqc.reggie.common.R;
import com.hqc.reggie.pojo.ShoppingCart;
import com.hqc.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        //设置用户id
        Long userId = (Long) session.getAttribute("user");
        shoppingCart.setUserId(userId);
        //添加之前判断是否已经点过相同的菜品，如果已经点过只需要在原来基础上把数量加一即可
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);

        //如果one不为空，已经存在，数量加一即可
        if (one != null) {
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
        } else {
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return R.success(one);
    }

    /*购物车回显*/
    //订单结算页面回显
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /*减少,如果对应的菜品数量减为0，则删除数据库中购物车表的记录*/
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        Long userID = (Long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userID);
        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if(one.getNumber()==0){
            shoppingCartService.removeById(one.getId());
            return R.error("已经为o了");
        }
        one.setNumber(one.getNumber() - 1);
        shoppingCartService.updateById(one);
        return R.success("减少成功");
    }

    /*根据用户id清理购物车*/
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session){
        Long userId=(Long) session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
