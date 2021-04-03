package com.gymmastercatalogue.app.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link CatalogueSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class CatalogueSearchRepositoryMockConfiguration {

    @MockBean
    private CatalogueSearchRepository mockCatalogueSearchRepository;

}
