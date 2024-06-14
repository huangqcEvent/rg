package com.hqc.reggie.dto;

import com.hqc.reggie.pojo.Setmeal;
import com.hqc.reggie.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
