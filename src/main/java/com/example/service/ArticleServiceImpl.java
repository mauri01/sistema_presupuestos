package com.example.service;

import com.example.model.Article;
import com.example.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("articleService")
public class ArticleServiceImpl implements ArticleService{

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Article findbyName(String name){
        return articleRepository.findByNombre(name);
    }

    @Override
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public List<Article> findAllArticle(){
        return articleRepository.findAll();
    }

    @Override
    public List<Article> findAllArticleActive(){
        List<Article> articles = articleRepository.findAll();
        return articles.stream().filter(Article::isActive).collect(Collectors.toList());
    }

    @Override
    public Article findbyId(int id){
        return articleRepository.findById(id);
    }

    @Override
    public void descontarStock(String idArt, String cantidad){
        Article article = articleRepository.findById(Integer.parseInt(idArt));
        int stock = article.getStock();
        int stockNuevo = stock - (Integer.parseInt(cantidad));
        article.setStock(stockNuevo);
        articleRepository.save(article);
    }
}
