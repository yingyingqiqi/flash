package com.yingqi.flash.service;

import com.yingqi.flash.dao.UserDao;
import com.yingqi.flash.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired(required = false)
    UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }
    @Transactional
    public void insertUser(User user) {
        userDao.insert(user);
    }
}
