package com.hqc.reggie.dto;

import com.hqc.reggie.pojo.Dish;
import com.hqc.reggie.pojo.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();//菜品口味信息

    private String categoryName;//菜品分类名称

    private Integer copies;//干啥的？？？？
}
