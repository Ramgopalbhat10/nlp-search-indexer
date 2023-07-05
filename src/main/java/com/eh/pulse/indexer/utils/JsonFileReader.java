package com.eh.pulse.indexer.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.eh.pulse.indexer.model.Article;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@Component
public class JsonFileReader {
  
  public List<Article> readJson(String filename) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectReader objectReader = objectMapper.readerFor(Article.class);
    List<Article> articles = new ArrayList<>();

    try {
      Resource resource = new ClassPathResource(filename);
      InputStream inputStream = resource.getInputStream();
      JsonFactory factory = new JsonFactory();
      JsonParser parser = factory.createParser(inputStream);

      if (parser.nextToken() == JsonToken.START_ARRAY) {
        while (parser.nextToken() == JsonToken.START_OBJECT && articles.size() <= 100) {
          Article article = objectReader.readValue(parser);
          articles.add(article);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return articles;
  }
}
