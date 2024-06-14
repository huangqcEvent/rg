package com.hqc.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hqc.reggie.common.R;
import com.hqc.reggie.pojo.AddressBook;
import com.hqc.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /*新增地址*/
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook, HttpSession session){
        Long id = (Long) session.getAttribute("user");
        addressBook.setUserId(id);
        addressBookService.save(addressBook);
        log.info(addressBook.toString());
        return R.success("新增地址成功");
    }
    /*查询全部地址*/
    @GetMapping("/list")
    public R<List<AddressBook>> list(HttpSession session){
        Long id = (Long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,id);
        List<AddressBook> addressList = addressBookService.list(queryWrapper);
        if (addressList==null){
            return R.error("请添加地址");
        }
        return R.success(addressList);
    }

    /*设置默认地址，因为默认地址只能有一个，所以在设置默认地址之前要把所有地址都设置为非默认的*/
    @PutMapping("/default")
    public R<String> defaul(@RequestBody AddressBook addressBook,HttpSession session){
        //获得当前用户的id，在地址破中查找对应的所有地址，把isdefault字段设置为0
        Long id = (Long) session.getAttribute("user");
        LambdaUpdateWrapper<AddressBook> wrapper =new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId,id);
        wrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(wrapper);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getId,addressBook.getId());
        AddressBook addressBook1 = addressBookService.getOne(queryWrapper);
        addressBook1.setIsDefault(1);
        addressBookService.updateById(addressBook1);
        return R.success("已经设置为默认地址");
    }

    /*根据地址id查询当个地址，页面回显*/
    @GetMapping("/{id}")
    public R<AddressBook> getone(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        return R.success(addressBook);
    }
    /*修改地址*/
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }
    /*删除地址*/
    @DeleteMapping
    public R<String> delete(Long ids){
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }

    /*获得默认地址*/
    //订单结算页面使用
    @GetMapping("/default")
    public R<AddressBook> defaultOne(HttpSession session){
        Long userId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,userId);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook byId = addressBookService.getOne(queryWrapper);
        return R.success(byId);
    }
}

