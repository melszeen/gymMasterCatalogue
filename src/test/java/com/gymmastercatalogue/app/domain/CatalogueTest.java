package com.gymmastercatalogue.app.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.gymmastercatalogue.app.web.rest.TestUtil;

public class CatalogueTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Catalogue.class);
        Catalogue catalogue1 = new Catalogue();
        catalogue1.setId(1L);
        Catalogue catalogue2 = new Catalogue();
        catalogue2.setId(catalogue1.getId());
        assertThat(catalogue1).isEqualTo(catalogue2);
        catalogue2.setId(2L);
        assertThat(catalogue1).isNotEqualTo(catalogue2);
        catalogue1.setId(null);
        assertThat(catalogue1).isNotEqualTo(catalogue2);
    }
}
