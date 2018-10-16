package main;

import model.Article;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ArticleService {


    List<Article> list = new ArrayList<Article>(Arrays.asList(
            new Article("1", "ArticleA", "aa"),
            new Article("2", "ArticleB", "aa"),
            new Article("3", "ArticleC", "aa")
    ));


    public List<Article> getAllArticles() {
        return list;
    }
}
