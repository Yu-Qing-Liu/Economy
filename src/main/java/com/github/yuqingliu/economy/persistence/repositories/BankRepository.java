package com.github.yuqingliu.economy.persistence.repositories;

import com.github.yuqingliu.economy.api.logger.Logger;
import com.github.yuqingliu.economy.persistence.entities.AccountEntity;
import com.github.yuqingliu.economy.persistence.entities.BankEntity;
import com.github.yuqingliu.economy.persistence.entities.CurrencyEntity;
import com.github.yuqingliu.economy.persistence.entities.PlayerEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankRepository {
    private final SessionFactory sessionFactory;
    private final PlayerRepository playerRepository;
    private final Logger logger;
    
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

    public boolean depositAllInterestForAllBanks() {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Set<BankEntity> banks = this.findAll();
            Instant now = Instant.now();
            for (BankEntity bank : banks) {
                Instant lastInterestTimestamp = bank.getLastInterestTimestamp();
                Duration interestCooldown = bank.getInterestCooldown();
                Instant nextInterestTimestamp = lastInterestTimestamp.plus(interestCooldown);
                if (now.isAfter(nextInterestTimestamp)) {
                    Set<AccountEntity> bankAccounts = bank.getAccounts();
                    for (AccountEntity account : bankAccounts) {
                        Set<CurrencyEntity> currencies = account.getCurrencies();
                        for (CurrencyEntity currency : currencies) {
                            double initial = currency.getAmount();
                            double profit = initial * account.getInterestRate();
                            currency.setAmount(initial + profit);
                            session.merge(currency);
                        }
                    }
                    bank.setLastInterestTimestamp(now);
                    session.merge(bank);
                }
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        }
    }

    public boolean unlockAccount(AccountEntity account, Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            double unlockPrice = account.getUnlockCost();
            String currencyType = account.getUnlockCurrencyType();
            Query<CurrencyEntity> query = session.createQuery(
                "FROM CurrencyEntity c WHERE c.purseId = :purseId AND c.currencyName = :currencyName", 
                CurrencyEntity.class
            );
            query.setParameter("purseId", player.getUniqueId());
            query.setParameter("currencyName", currencyType);
            CurrencyEntity purseCurrency = query.uniqueResult();
            if(purseCurrency.getAmount() < unlockPrice) {
                logger.sendPlayerErrorMessage(player, "Not enouch currency to unlock this bank account.");
                throw new IllegalArgumentException();
            }
            purseCurrency.setAmount(purseCurrency.getAmount() - unlockPrice);
            session.merge(purseCurrency);
            account.setUnlocked(true);
            session.merge(account);
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
            return null;
        }
    }

    public Set<BankEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return Set.copyOf(session.createQuery("from BankEntity", BankEntity.class).list());
        } catch (Exception e) {
            return Set.of();
        }
    }
}
