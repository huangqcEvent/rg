package com.hqc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hqc.reggie.pojo.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long ids);
}
