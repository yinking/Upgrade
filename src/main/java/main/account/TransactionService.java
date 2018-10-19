package main.account;

import dao.Upgrade.dao.UpgradeAccountDAO;
import dao.Upgrade.dao.UpgradeTransactionDAO;
import dao.Upgrade.model.UpgradeAccount;
import dao.Upgrade.model.UpgradeTransaction;
import model.NoMoneyException;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class TransactionService {

    UpgradeAccountDAO accountDAO = UpgradeAccountDAO.getInstance();
    UpgradeTransactionDAO transactionDAO = UpgradeTransactionDAO.getInstance();




    public UpgradeAccount deposit(int accountId, Double amount) {
        UpgradeAccount account = accountDAO.selectById(accountId);
        account.setBalance(account.getBalance() + amount);
        accountDAO.update(account);
        UpgradeTransaction transaction = new UpgradeTransaction();
        transaction.setToAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setTime(new Date(System.currentTimeMillis()));
        transactionDAO.insert(transaction);
        return account;
    }

    public UpgradeAccount withdraw(int accountId, Double amount) throws NoMoneyException {
        UpgradeAccount account = accountDAO.selectById(accountId);
        if (account.getBalance() - amount < 0) {
            throw new NoMoneyException(accountId, amount);
        }
        account.setBalance(account.getBalance() - amount);
        accountDAO.update(account);
        UpgradeTransaction transaction = new UpgradeTransaction();
        transaction.setToAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setTime(new Date(System.currentTimeMillis()));
        transactionDAO.insert(transaction);
        return account;
    }

    public UpgradeTransaction tranfer(int fromAccountId, int toAccountId, Double amount) throws NoMoneyException {
        UpgradeAccount fromAccount = accountDAO.selectById(fromAccountId);
        if (fromAccount.getBalance() - amount < 0) {
            throw new NoMoneyException(fromAccountId, amount);
        }
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        accountDAO.update(fromAccount);
        UpgradeAccount toAccount = accountDAO.selectById(toAccountId);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountDAO.update(toAccount);
        UpgradeTransaction transaction = new UpgradeTransaction();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setTime(new Date(System.currentTimeMillis()));
        transactionDAO.insert(transaction);
        return transaction;
    }





}
