package com.github.yuqingliu.economy;

import java.io.File;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class SpringConfig {
    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin plugin) {
        SpringConfig.plugin = plugin;
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
}
