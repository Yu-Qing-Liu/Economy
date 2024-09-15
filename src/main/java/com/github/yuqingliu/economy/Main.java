package com.github.yuqingliu.economy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.github.yuqingliu.economy.api.Economy;
import com.github.yuqingliu.economy.api.managers.EventManager;
import lombok.Getter;

@Getter
public class Main extends Economy {
    private AnnotationConfigApplicationContext applicationContext;
    
    @Autowired
    private EventManager eventManager;

    @Override
    public void onEnable() {
        SpringConfig.setPlugin(this);
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SpringConfig.class);
        applicationContext.refresh();
        printCustomBeans();
    }

    @Override
    public void onDisable() {
        if (applicationContext != null) {
            ((AnnotationConfigApplicationContext) applicationContext).close();
        }
    }

    private void printCustomBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : allBeanNames) {
            Object bean = applicationContext.getBean(beanName);
            if (!isDefaultSpringBean(beanName)) {
                System.out.println("Bean name: " + beanName + ", Bean class: " + bean.getClass().getName());
            }
        }
    }

    private boolean isDefaultSpringBean(String beanName) {
        return beanName.startsWith("org.springframework");
    }
}

