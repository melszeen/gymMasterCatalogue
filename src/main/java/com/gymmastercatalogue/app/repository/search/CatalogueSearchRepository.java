package com.gymmastercatalogue.app.repository.search;

import com.gymmastercatalogue.app.domain.Catalogue;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Catalogue} entity.
 */
public interface CatalogueSearchRepository extends ElasticsearchRepository<Catalogue, Long> {
}
