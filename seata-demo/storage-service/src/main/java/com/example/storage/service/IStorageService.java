package com.example.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.storage.entity.Storage;

public interface IStorageService extends IService<Storage> {

    /**
     * 扣除存储数量
     */
    void deduct(String commodityCode, int count);
}