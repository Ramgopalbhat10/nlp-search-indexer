package com.eh.pulse.indexer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Dense_Vector;

import lombok.Data;

@Data
@Document(indexName = "nlp-pulse-search")
public class Content {
  
  @Id
  private String id;

  @Field(type = Text, name = "title")
  private String title;

  @Field(type = Text, name = "description")
  private String description;

  @Field(type = Text, name = "keywords")
  private String keywords;

  @Field(type = Text, name = "comments")
  private String comments;

  @Field(type = Dense_Vector, name = "vec-title")
  private List<Float> vecTitle;

  @Field(type = Dense_Vector, name = "vec-description")
  private List<Float> vecDescription;

  @Field(type = Dense_Vector, name = "vec-keywords")
  private List<Float> vecKeywords;

  @Field(type = Dense_Vector, name = "vec-comments")
  private List<Float> vecComments;
}
