package main.create_account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/account")
public class CreateAccountController {
    @Autowired
    private CreateAccountService service;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> createAccount(@PathVariable Long id) {
        service.create(id);
        return new ResponseEntity<String>("Account Created Successfully" + id, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> createAccount() {
        return new ResponseEntity<String>("lol", HttpStatus.CREATED);
    }
}
