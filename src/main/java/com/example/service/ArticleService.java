package com.example.service;

import com.example.model.Article;

import java.util.List;

public interface ArticleService {
    public Article findbyName(String name);
    public Article saveArticle(Article article);
    public List<Article> findAllArticle();
    public Article findbyId(int id);
    public List<Article> findAllArticleActive();
    void backStock(int idArt, int cantidad);

    void descontarStock(String idArt, String cantidad);
}
