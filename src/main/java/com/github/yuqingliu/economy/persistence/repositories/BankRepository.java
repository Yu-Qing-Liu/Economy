package com.github.yuqingliu.economy.persistence.repositories;

import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankRepository {
    private final SessionFactory sessionFactory;
    private final PlayerRepository playerRepository;
    
    // Transactions
    public boolean save(BankEntity bank) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(bank);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean delete(String bankName) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            BankEntity bank = session.get(BankEntity.class, bankName);
            if (bank != null) {
                session.remove(bank);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean addBankAccountToAll(String accountName, String bankName, ItemStack icon, double interestRate, String unlockCurrencyName, double unlockCost) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            BankEntity bank = this.get(bankName);
            Set<PlayerEntity> players = playerRepository.findAll();
            for (PlayerEntity player : players) {
                AccountEntity newAccount = new AccountEntity();
                newAccount.setBank(bank);
                newAccount.setIcon(icon);
                newAccount.setPlayer(player);
                newAccount.setAccountId(player.getPlayerId());
                newAccount.setAccountName(accountName);
                newAccount.setInterestRate(interestRate);
                newAccount.setUnlockCurrencyType(unlockCurrencyName);
                newAccount.setUnlockCost(unlockCost);

                Set<CurrencyEntity> currencies = player.getPurse().getCurrencies();
                for (CurrencyEntity entity : currencies) {
                    CurrencyEntity currency = new CurrencyEntity();
                    currency.setCurrencyName(entity.getCurrencyName());
                    currency.setAmount(0);
                    currency.setIcon(entity.getIcon());
                    currency.setPurseId(UUID.randomUUID());
                    currency.setAccountId(player.getPlayerId());
                    currency.setPurse(null);
                    currency.setAccount(newAccount);
                    newAccount.getCurrencies().add(currency);
                }
                session.persist(newAccount);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    // Queries
    public BankEntity get(String bankName) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(BankEntity.class, bankName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<BankEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from BankEntity", BankEntity.class).list());
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }
}
