package main.account;

import dao.Upgrade.model.UpgradeAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value = "/account")
public class AccountController {
    @Autowired
    private AccountService service;

    @RequestMapping(value = "/create/{ownerId}", method = RequestMethod.GET)
    public ResponseEntity<String> createAccount(@PathVariable Integer ownerId) {
        service.create(ownerId);
        return new ResponseEntity<String>("Account Created Successfully" + ownerId, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/list/{ownerId}", method = RequestMethod.GET)
    public ResponseEntity<List<UpgradeAccount>> getAccounts(@PathVariable Integer ownerId) {
        return new ResponseEntity<List<UpgradeAccount>>(service.getAccountsById(ownerId), HttpStatus.OK);
    }


}
