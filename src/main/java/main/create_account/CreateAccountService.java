package main.create_account;

import dao.Upgrade.dao.UpgradeAccountDAO;
import model.Article;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CreateAccountService {


    List<Article> list = new ArrayList<Article>(Arrays.asList(
            new Article("1", "ArticleA", "aa"),
            new Article("2", "ArticleB", "aa"),
            new Article("3", "ArticleC", "aa")
    ));

    public void create(long userId) {
        UpgradeAccountDAO dao = UpgradeAccountDAO.getInstance();


    }

    public List<Article> getAllArticles() {
        return list;
    }
}
