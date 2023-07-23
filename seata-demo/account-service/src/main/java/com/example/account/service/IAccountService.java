package com.example.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.account.entity.Account;

public interface IAccountService extends IService<Account> {
    /**
     * 从用户账户中扣款
     */
    void deduct(String userId, int money);
}