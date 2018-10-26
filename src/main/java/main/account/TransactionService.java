package main.account;

import dao.Upgrade.dao.UpgradeAccountDAO;
import dao.Upgrade.dao.UpgradeTransactionDAO;
import dao.Upgrade.model.UpgradeAccount;
import dao.Upgrade.model.UpgradeTransaction;
import model.NoMoneyException;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    UpgradeAccountDAO accountDAO = UpgradeAccountDAO.getInstance();
    UpgradeTransactionDAO transactionDAO = UpgradeTransactionDAO.getInstance();

    static Map<Integer, Object> lockMap = new HashMap<Integer, Object>();

    public List<UpgradeTransaction> fetchTenTransaction(int accountId) {
        List<UpgradeTransaction> upgradeTransactions = transactionDAO.fetchTenTransaction("fromAccountId", "toAccountId", accountId + "");
        return upgradeTransactions;
    }


    synchronized static Object getLock(int aid) {
        if (!lockMap.containsKey(aid)) {
            lockMap.put(aid, new Object());
        }
        return lockMap.get(aid);
    }

    public UpgradeAccount deposit(int accountId, Double amount) {
        synchronized (getLock(accountId)) {
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

    }

    public UpgradeAccount withdraw(int accountId, Double amount) throws NoMoneyException {
        synchronized (getLock(accountId)) {
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
    }

    public UpgradeTransaction tranfer(int fromAccountId, int toAccountId, Double amount) throws NoMoneyException {


        Object smallLock = getLock(fromAccountId < toAccountId ? fromAccountId : toAccountId);
        Object bigLock = getLock(fromAccountId < toAccountId ? toAccountId : fromAccountId);
        synchronized (smallLock) {
            synchronized (bigLock) {
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

    }


}
