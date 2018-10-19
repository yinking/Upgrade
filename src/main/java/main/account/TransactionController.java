package main.account;

import dao.Upgrade.model.UpgradeAccount;
import dao.Upgrade.model.UpgradeTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value = "/transaction")
public class TransactionController {
    @Autowired
    private TransactionService service;

    @RequestMapping(value = "/deposit/{account}/{amount}", method = RequestMethod.GET)
    public ResponseEntity<UpgradeAccount> deposit(@PathVariable Integer account, @PathVariable Double amount) {
        UpgradeAccount upgradeAccount = service.deposit(account, amount);
        return new ResponseEntity<UpgradeAccount>(upgradeAccount, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/withdraw/{account}/{amount}", method = RequestMethod.GET)
    public ResponseEntity<UpgradeAccount> withdraw(@PathVariable Integer account, @PathVariable Double amount) {
        UpgradeAccount upgradeAccount = service.withdraw(account, amount);
        return new ResponseEntity<UpgradeAccount>(upgradeAccount, HttpStatus.CREATED);
    }


    @RequestMapping(value = "/transfer/{fromAccount}/{toAccount}/{amount}", method = RequestMethod.GET)
    public ResponseEntity<UpgradeTransaction> transfer(@PathVariable Integer fromAccount, @PathVariable Integer toAccount, @PathVariable Double amount) {

        UpgradeTransaction transaction = service.tranfer(fromAccount, toAccount, amount);
        return new ResponseEntity<UpgradeTransaction>(transaction, HttpStatus.CREATED);
    }

}
