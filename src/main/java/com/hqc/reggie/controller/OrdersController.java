package com.hqc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hqc.reggie.common.CustomException;
import com.hqc.reggie.common.R;
import com.hqc.reggie.dto.OrdersDto;
import com.hqc.reggie.pojo.*;
import com.hqc.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
/*用户下单*/
    @PostMapping("/submit")
    @Transactional
    public R<String> submit(@RequestBody Orders orders, HttpSession session){
        //获得当前用户id
        Long userID = (Long) session.getAttribute("user");
        //根据id去查询他的购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userID);
        List<ShoppingCart> cartList = shoppingCartService.list(queryWrapper);
        if (cartList==null || cartList.size()==0){
            throw new CustomException("购物车为空，不能下单");
        }
        //用户id来查询用户表
        User userInfo = userService.getById(userID);
        //查询用户地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBookInfo = addressBookService.getById(addressBookId);
        if (addressBookInfo==null){
            throw new CustomException("用户地址信息有误，不能下单");
        }
        //向订单表插入数据，一条数据,设置除了页面传来的三个数据的其余属性
        long orderId = IdWorker.getId();//订单号
        AtomicInteger amount = new AtomicInteger(0);
        //遍历购物车，得出总金额，以及订单明细表中数据
       List<OrderDetail> orderDetails = cartList.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userID);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(addressBookInfo.getConsignee());
        orders.setConsignee(addressBookInfo.getConsignee());
        orders.setPhone(addressBookInfo.getPhone());
        orders.setAddress(addressBookInfo.getDetail());
        ordersService.save(orders);
        //向订单明细表插入数据，多条
        orderDetailService.saveBatch(orderDetails);
        //清空他的购物车
        shoppingCartService.remove(queryWrapper);
        return R.success("下单成功，骑手在赶来的路上");
    }
    /*查看订单*/
    @GetMapping("/userPage")
    public R<Page> userPage(Integer page,Integer pageSize,HttpSession session){
        //获取用户id
        Long userId = (Long) session.getAttribute("user");
//        Page<Orders> ordersPage  = new Page<>(page,pageSize);
//        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Orders::getUserId,userId);
//        ordersService.page(ordersPage,queryWrapper);
//        return R.success(ordersPage);
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        //
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        ordersService.page(ordersPage, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> recordsDto =records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            //获得用户id
            Long userId1 = item.getUserId();
            //根据id查询对应的默认地址
            LambdaQueryWrapper<AddressBook> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(AddressBook::getIsDefault,1).eq(AddressBook::getUserId,userId1);
            AddressBook oneBook = addressBookService.getOne(queryWrapper1);
            ordersDto.setUserName(oneBook.getConsignee());
            ordersDto.setPhone(oneBook.getPhone());
            ordersDto.setAddress(oneBook.getDetail());
            ordersDto.setConsignee(oneBook.getConsignee());
            //根据订单id查询ordersDetail表中数据
            //获取订单id
             Long id = item.getId();
             LambdaQueryWrapper<OrderDetail> queryWrapper2 = new LambdaQueryWrapper<>();
             queryWrapper2.eq(OrderDetail::getOrderId,id);
             List<OrderDetail> list = orderDetailService.list(queryWrapper2);
             ordersDto.setOrderDetails(list);
             return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(recordsDto);

        //得到订单明细表，先得到订单id
//        LambdaQueryWrapper<Orders> queryWrapper1 = new LambdaQueryWrapper<>();
//        queryWrapper1.eq(Orders::getUserId,userId);
//        List<Orders> ordersList = ordersService.list(queryWrapper1);
//
//        List<OrdersDto> orderDetailList = ordersList.stream().map((item)->{
//            OrdersDto ordersDto = new OrdersDto();
//            BeanUtils.copyProperties(item,ordersDto);
//
//            LambdaQueryWrapper<OrderDetail> queryWrapper2 = new LambdaQueryWrapper<>();
//            queryWrapper2.eq(OrderDetail::getOrderId,item.getId());
//            List<OrderDetail> orderDetailList1 = orderDetailService.list(queryWrapper2);
//            BeanUtils.copyProperties(orderDetailList1,ordersDto);
//            return ordersDto;
//        }).collect(Collectors.toList());
        return R.success(ordersDtoPage);
    }

//管理端分页查询 页面回显
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize,Long number,HttpSession session){
        //获取用户id
//        Long userId = (Long) session.getAttribute("user");
        Page<Orders> ordersPage = new Page<Orders>(page,pageSize);
        LambdaQueryWrapper<Orders> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.like(number != null, Orders::getNumber, number);
        ordersService.page(ordersPage,dishLambdaQueryWrapper);
        return R.success(ordersPage);
    }
    /*后台修改订单状态*/
    @PutMapping
    public R<String> put(@RequestBody Orders orders){
        ordersService.updateById(orders);
        return R.success("订单状态修改完成");
    }
}
