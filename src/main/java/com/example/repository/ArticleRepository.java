package com.example.repository;

import com.example.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("articleRepository")
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Article findByNombre(String name);
    Article findById(int id);
}
