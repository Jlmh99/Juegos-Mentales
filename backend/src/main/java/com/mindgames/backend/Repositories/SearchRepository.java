package com.mindgames.backend.Repositories;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.mindgames.backend.entities.SearchContent;

public interface SearchRepository extends ElasticsearchRepository<SearchContent, String> {

    List<SearchContent> findByTituloContainingOrDescripcionContaining(
        String titulo,
        String descripcion
    );
}
