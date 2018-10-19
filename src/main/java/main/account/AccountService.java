package main.account;

import dao.Upgrade.dao.UpgradeAccountDAO;
import dao.Upgrade.model.UpgradeAccount;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    UpgradeAccountDAO dao = UpgradeAccountDAO.getInstance();


    public void create(Integer userId) {

        UpgradeAccount account = new UpgradeAccount(userId, 0.0);
        dao.insert(account);
    }

    public List<UpgradeAccount> getAccountsById(Integer ownerId) {
        return dao.selectByIntegerField("ownerId", ownerId);
    }


}
