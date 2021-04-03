package com.gymmastercatalogue.app.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CatalogueMapperTest {

    private CatalogueMapper catalogueMapper;

    @BeforeEach
    public void setUp() {
        catalogueMapper = new CatalogueMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(catalogueMapper.fromId(id).getId()).isEqualTo(id);
        assertThat(catalogueMapper.fromId(null)).isNull();
    }
}
