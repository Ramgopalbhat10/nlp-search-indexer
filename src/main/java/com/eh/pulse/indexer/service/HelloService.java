package com.eh.pulse.indexer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eh.pulse.indexer.model.Article;
import com.eh.pulse.indexer.utils.JsonFileReader;

@Service
public class HelloService {

  @Autowired
  private JsonFileReader jsonFileReader;

  public void greet() {
    List<Article> articles = this.jsonFileReader.readJson("medium_articles.json");
    System.out.printf("articles lenght -> %d", articles.size());
  }
}
