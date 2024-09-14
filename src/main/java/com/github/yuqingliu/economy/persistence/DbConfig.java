package com.github.yuqingliu.economy.persistence;

import java.io.File;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;
import com.github.yuqingliu.economy.persistence.repositories.implementations.*;
import com.github.yuqingliu.economy.persistence.services.*;

@Configuration
public class DbConfig {
    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin plugin) {
        DbConfig.plugin = plugin;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        File dataFolder = new File(plugin.getDataFolder(), "database.db");
        if (!dataFolder.exists()){
            dataFolder.getParentFile().mkdirs();
        }
        dataSource.setUrl("jdbc:sqlite:" + dataFolder);
        dataSource.setUsername("");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlayerRepository playerRepository() {
        return new PlayerRepositoryImpl(jdbcTemplate(dataSource()));
    }

    @Bean
    public PlayerService playerService() {
        return new PlayerService(playerRepository());
    }

}
