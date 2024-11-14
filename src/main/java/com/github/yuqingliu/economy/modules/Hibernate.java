package com.github.yuqingliu.economy.modules;

import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.github.yuqingliu.economy.persistence.entities.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Hibernate {
    private final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<>();
    private final SessionFactory sessionFactory;

    @Inject
    public Hibernate(JavaPlugin plugin) {
        this.sessionFactory = createSessionFactory(plugin.getDataFolder().getAbsolutePath());
    }

    // Factory method to create and provide the SessionFactory
    public SessionFactory createSessionFactory(String dataFolderPath) {
        Configuration configuration = new Configuration();
        
        // Set up SQLite connection
        String dbUrl = "jdbc:sqlite:" + dataFolderPath + "/database.db";
        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        configuration.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update"); // "update", "create", "create-drop"
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "false");

        // Use CHAR type for UUIDs
        configuration.setProperty("hibernate.type.preferred_uuid_jdbc_type", "CHAR");

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

        Metadata metadata = sources.getMetadataBuilder().build();
        return metadata.getSessionFactoryBuilder().build();
    }

    // Method to retrieve the current session for the current thread
    public Session getSession() {
        Session session = sessionThreadLocal.get();
        if (session == null) {
            session = sessionFactory.openSession();  // Open a new session if none is available
            sessionThreadLocal.set(session);         // Set the session to ThreadLocal
        }
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

