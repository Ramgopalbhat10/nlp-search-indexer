package com.eh.pulse.indexer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eh.pulse.indexer.model.Content;
import com.eh.pulse.indexer.service.HelloService;
import com.eh.pulse.indexer.service.IndexerService;
import com.eh.pulse.indexer.service.SearchService;

@RestController
@RequestMapping("/api/v1")
public class IndexerController {

  private final HelloService helloService;
  private final IndexerService indexerService;
  private final SearchService searchService;

  public IndexerController(HelloService helloService, IndexerService indexerService, SearchService searchService) {
    this.helloService = helloService;
    this.indexerService = indexerService;
    this.searchService = searchService;
  }

  @GetMapping("/hello")
  public String hello() {
    this.helloService.greet();
    return "Hello";
  }

  @GetMapping("/index")
  public String index() {
    indexerService.indexArticles();
    return "Index completed";
  }

  @GetMapping("/search")
  public List<Content> search(@RequestParam(name = "query") String query) {
    return this.searchService.search(query);
  }
}
