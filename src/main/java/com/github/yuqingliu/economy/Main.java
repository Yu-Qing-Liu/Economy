package com.github.yuqingliu.economy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.github.yuqingliu.economy.api.Economy;
import com.github.yuqingliu.economy.api.managers.EventManager;
import com.github.yuqingliu.economy.persistence.DbConfig;

import lombok.Getter;

@Getter
public class Main extends Economy {
    private ApplicationContext applicationContext;
    
    @Autowired
    private EventManager eventManager;

    @Override
    public void onEnable() {
        SpringConfig.setPlugin(this);
        DbConfig.setPlugin(this);
        applicationContext = new AnnotationConfigApplicationContext(DbConfig.class, SpringConfig.class);
    }

    @Override
    public void onDisable() {
        if (applicationContext != null) {
            ((AnnotationConfigApplicationContext) applicationContext).close();
        }
    }
}

