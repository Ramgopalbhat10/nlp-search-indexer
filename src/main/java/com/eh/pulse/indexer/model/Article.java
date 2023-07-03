package com.eh.pulse.indexer.model;

import java.util.List;

import lombok.Data;

@Data
public class Article {
  private String url;
  private String crawled_at;
  private String _id;
  private String title;
  private String author;
  private String published_at;
  private String author_url;
  private int reading_time;
  private String total_claps;
  private String raw_description;
  private String source;
  private String description;
  private String tags;
  private List<String> images;
  private String modified_at;
}
