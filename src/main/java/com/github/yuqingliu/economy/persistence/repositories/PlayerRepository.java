package com.github.yuqingliu.economy.persistence.repositories;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.github.yuqingliu.economy.persistence.entities.PurseEntity;
import com.github.yuqingliu.economy.modules.Hibernate;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerRepository {
    private final Hibernate hibernate;
    
    // Transactions
    public boolean addPlayer(UUID playerId) {
        Transaction transaction = null;
        try (Session session = hibernate.getSession()) {
            transaction = session.beginTransaction();
            PlayerEntity player = this.get(playerId);            
            if(player == null) {
                PlayerEntity playerEntity = new PlayerEntity();
                playerEntity.setPlayerId(playerId);
                PurseEntity playerPurse = new PurseEntity();
                playerPurse.setPlayerId(playerId);
                playerEntity.setPurse(playerPurse);
                session.persist(playerEntity);
                Set<CurrencyEntity> currencies = Set.copyOf(
                    session.createQuery(
                        "FROM CurrencyEntity c WHERE c.currencyName IN (" +
                        "SELECT DISTINCT c2.currencyName FROM CurrencyEntity c2)", 
                        CurrencyEntity.class
                    ).list()
                );
                currencies.forEach(uniqueCurrency -> {
                    CurrencyEntity currency = new CurrencyEntity();
                    currency.setCurrencyName(uniqueCurrency.getCurrencyName());
                    currency.setAmount(0);
                    currency.setIcon(uniqueCurrency.getIcon());
                    currency.setPurseId(playerPurse.getPlayerId());
                    currency.setAccountId(UUID.randomUUID());
                    currency.setPurse(playerPurse);
                    currency.setAccount(null);
                    session.persist(currency);
                });
                transaction.commit();
                return true;
            }
            transaction.rollback();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    // Queries
    public PlayerEntity get(UUID playerId) {
        try (Session session = hibernate.getSession()) {
            return session.get(PlayerEntity.class, playerId);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<PlayerEntity> findAll() {
        try (Session session = hibernate.getSession()) {
            return Set.copyOf(session.createQuery("from PlayerEntity", PlayerEntity.class).list());
        } catch (Exception e) {
            return Set.of();
        }
    }
}
