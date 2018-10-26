package test.service;

import dao.Upgrade.model.UpgradeTransaction;
import main.ArticleService;
import main.account.TransactionService;
import model.Article;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import test.AbstractTest;

import java.util.List;

//@Transactional
public class ServiceTest extends AbstractTest {
    @Autowired
    private ArticleService articleService;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testFetchTenTransaction() {

        TransactionService transactionService = new TransactionService();

        List<UpgradeTransaction> upgradeTransactions = transactionService.fetchTenTransaction(1);

        Assert.assertNotNull(upgradeTransactions);


    }

}
