package com.github.yuqingliu.economy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.github.yuqingliu.economy.api.Economy;
import com.github.yuqingliu.economy.api.managers.EventManager;

import lombok.Getter;

@Getter
public class Main extends Economy {
    private ApplicationContext applicationContext;
    
    @Autowired
    private EventManager eventManager;

    @Override
    public void onEnable() {
        SpringConfig.setPlugin(this);
        applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
    }

    @Override
    public void onDisable() {
        if (applicationContext != null) {
            ((AnnotationConfigApplicationContext) applicationContext).close();
        }
    }
}

