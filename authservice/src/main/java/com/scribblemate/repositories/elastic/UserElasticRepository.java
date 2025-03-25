package com.scribblemate.repositories.elastic;

import com.scribblemate.entities.elastic.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserElasticRepository extends ElasticsearchRepository<UserDocument, String> {
}
