package com.github.yuqingliu.economy.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.cfg.Configuration;

import com.github.yuqingliu.economy.persistence.entities.*;
import com.google.inject.Inject;

public class Hibernate {
    private final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<>();
    private static SessionFactory sessionFactory;

    @Inject
    public Hibernate(JavaPlugin plugin) {
        if (sessionFactory == null) {
            sessionFactory = createSessionFactory(plugin.getDataFolder().getAbsolutePath());
        }
    }

    private static void defaultSettings(String dataFolderPath, Configuration configuration) {
        String dbUrl = "jdbc:sqlite:" + dataFolderPath + "/database.db";
        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        configuration.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update"); // "update", "create", "create-drop"
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "false");
        configuration.setProperty("hibernate.connection.autocommit", "false");
    }

    public static SessionFactory createSessionFactory(String dataFolderPath) {
        boolean readSuccess = false;
        Properties properties = new Properties();
        Configuration configuration = new Configuration();
        File dataFolder = new File(dataFolderPath);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try (FileInputStream input = new FileInputStream(dataFolderPath + "/hibernate.properties")) {
            properties.load(input);
            readSuccess = true;
        } catch (IOException e) {
            System.out.println("Error loading hibernate.properties, using default settings.");
        }

        if (!readSuccess) {
            defaultSettings(dataFolderPath, configuration);
        } else {
            for (String name : properties.stringPropertyNames()) {
                configuration.setProperty(name, properties.getProperty(name));
            }           
        }

        StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
        serviceRegistryBuilder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();

        MetadataSources sources = new MetadataSources(serviceRegistry);

        // Add annotated classes
        sources.addAnnotatedClass(AccountEntity.class);
        sources.addAnnotatedClass(BankEntity.class);
        sources.addAnnotatedClass(CurrencyEntity.class);
        sources.addAnnotatedClass(PlayerEntity.class);
        sources.addAnnotatedClass(PurseEntity.class);
        sources.addAnnotatedClass(ShopEntity.class);
        sources.addAnnotatedClass(ShopSectionEntity.class);
        sources.addAnnotatedClass(ShopItemEntity.class);
        sources.addAnnotatedClass(ShopOrderEntity.class);
        sources.addAnnotatedClass(VendorEntity.class);
        sources.addAnnotatedClass(VendorSectionEntity.class);
        sources.addAnnotatedClass(VendorItemEntity.class);
        sources.addAnnotatedClass(AuctionEntity.class);
        sources.addAnnotatedClass(BidEntity.class);

        Metadata metadata = sources.getMetadataBuilder().build();
        return metadata.getSessionFactoryBuilder().build();
    }

    // Method to retrieve the current session for the current thread
    public Session getSession() {
        Session session = sessionFactory.openSession();
        sessionThreadLocal.set(session);
        return session;
    }

    // Close session for the current thread
    public void closeSession() {
        Session session = sessionThreadLocal.get();
        if (session != null) {
            session.close();
            sessionThreadLocal.remove();
        }
    }
}

