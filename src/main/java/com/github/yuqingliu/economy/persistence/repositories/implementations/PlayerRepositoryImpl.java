package com.github.yuqingliu.economy.persistence.repositories.implementations;

import com.github.yuqingliu.economy.persistence.models.Player;
import com.github.yuqingliu.economy.persistence.repositories.PlayerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Player> PLAYER_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Player(UUID.fromString(rs.getString("uuid")));
        }
    };

    @Override
    public void save(Player player) {
        String sql = "INSERT INTO player (uuid) VALUES (?) ON CONFLICT (uuid) DO UPDATE SET uuid = excluded.uuid";
        jdbcTemplate.update(sql, player.getId().toString());
    }

    @Override
    public Player findById(UUID uuid) {
        String sql = "SELECT * FROM player WHERE uuid = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{uuid.toString()}, PLAYER_ROW_MAPPER);
    }

    @Override
    public void deleteById(UUID uuid) {
        String sql = "DELETE FROM player WHERE uuid = ?";
        jdbcTemplate.update(sql, uuid.toString());
    }
}
