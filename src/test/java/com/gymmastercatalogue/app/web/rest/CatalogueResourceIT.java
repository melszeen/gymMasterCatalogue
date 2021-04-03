package com.gymmastercatalogue.app.web.rest;

import com.gymmastercatalogue.app.GymMasterCatalogueApp;
import com.gymmastercatalogue.app.domain.Catalogue;
import com.gymmastercatalogue.app.repository.CatalogueRepository;
import com.gymmastercatalogue.app.repository.search.CatalogueSearchRepository;
import com.gymmastercatalogue.app.service.CatalogueService;
import com.gymmastercatalogue.app.service.dto.CatalogueDTO;
import com.gymmastercatalogue.app.service.mapper.CatalogueMapper;
import com.gymmastercatalogue.app.service.dto.CatalogueCriteria;
import com.gymmastercatalogue.app.service.CatalogueQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CatalogueResource} REST controller.
 */
@SpringBootTest(classes = GymMasterCatalogueApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class CatalogueResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Integer DEFAULT_PARTNER_ID = 1;
    private static final Integer UPDATED_PARTNER_ID = 2;
    private static final Integer SMALLER_PARTNER_ID = 1 - 1;

    private static final Double DEFAULT_PRICE = 0D;
    private static final Double UPDATED_PRICE = 1D;
    private static final Double SMALLER_PRICE = 0D - 1D;

    private static final Integer DEFAULT_DURATION = 0;
    private static final Integer UPDATED_DURATION = 1;
    private static final Integer SMALLER_DURATION = 0 - 1;

    private static final Instant DEFAULT_SESSION_DT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SESSION_DT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private CatalogueRepository catalogueRepository;

    @Autowired
    private CatalogueMapper catalogueMapper;

    @Autowired
    private CatalogueService catalogueService;

    /**
     * This repository is mocked in the com.gymmastercatalogue.app.repository.search test package.
     *
     * @see com.gymmastercatalogue.app.repository.search.CatalogueSearchRepositoryMockConfiguration
     */
    @Autowired
    private CatalogueSearchRepository mockCatalogueSearchRepository;

    @Autowired
    private CatalogueQueryService catalogueQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCatalogueMockMvc;

    private Catalogue catalogue;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Catalogue createEntity(EntityManager em) {
        Catalogue catalogue = new Catalogue()
            .description(DEFAULT_DESCRIPTION)
            .partnerId(DEFAULT_PARTNER_ID)
            .price(DEFAULT_PRICE)
            .duration(DEFAULT_DURATION)
            .sessionDt(DEFAULT_SESSION_DT);
        return catalogue;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Catalogue createUpdatedEntity(EntityManager em) {
        Catalogue catalogue = new Catalogue()
            .description(UPDATED_DESCRIPTION)
            .partnerId(UPDATED_PARTNER_ID)
            .price(UPDATED_PRICE)
            .duration(UPDATED_DURATION)
            .sessionDt(UPDATED_SESSION_DT);
        return catalogue;
    }

    @BeforeEach
    public void initTest() {
        catalogue = createEntity(em);
    }

    @Test
    @Transactional
    public void createCatalogue() throws Exception {
        int databaseSizeBeforeCreate = catalogueRepository.findAll().size();
        // Create the Catalogue
        CatalogueDTO catalogueDTO = catalogueMapper.toDto(catalogue);
        restCatalogueMockMvc.perform(post("/api/catalogues")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogueDTO)))
            .andExpect(status().isCreated());

        // Validate the Catalogue in the database
        List<Catalogue> catalogueList = catalogueRepository.findAll();
        assertThat(catalogueList).hasSize(databaseSizeBeforeCreate + 1);
        Catalogue testCatalogue = catalogueList.get(catalogueList.size() - 1);
        assertThat(testCatalogue.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCatalogue.getPartnerId()).isEqualTo(DEFAULT_PARTNER_ID);
        assertThat(testCatalogue.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testCatalogue.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testCatalogue.getSessionDt()).isEqualTo(DEFAULT_SESSION_DT);

        // Validate the Catalogue in Elasticsearch
        verify(mockCatalogueSearchRepository, times(1)).save(testCatalogue);
    }

    @Test
    @Transactional
    public void createCatalogueWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = catalogueRepository.findAll().size();

        // Create the Catalogue with an existing ID
        catalogue.setId(1L);
        CatalogueDTO catalogueDTO = catalogueMapper.toDto(catalogue);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCatalogueMockMvc.perform(post("/api/catalogues")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogueDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Catalogue in the database
        List<Catalogue> catalogueList = catalogueRepository.findAll();
        assertThat(catalogueList).hasSize(databaseSizeBeforeCreate);

        // Validate the Catalogue in Elasticsearch
        verify(mockCatalogueSearchRepository, times(0)).save(catalogue);
    }


    @Test
    @Transactional
    public void checkPartnerIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = catalogueRepository.findAll().size();
        // set the field null
        catalogue.setPartnerId(null);

        // Create the Catalogue, which fails.
        CatalogueDTO catalogueDTO = catalogueMapper.toDto(catalogue);


        restCatalogueMockMvc.perform(post("/api/catalogues")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogueDTO)))
            .andExpect(status().isBadRequest());

        List<Catalogue> catalogueList = catalogueRepository.findAll();
        assertThat(catalogueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCatalogues() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList
        restCatalogueMockMvc.perform(get("/api/catalogues?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(catalogue.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].partnerId").value(hasItem(DEFAULT_PARTNER_ID)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].sessionDt").value(hasItem(DEFAULT_SESSION_DT.toString())));
    }
    
    @Test
    @Transactional
    public void getCatalogue() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get the catalogue
        restCatalogueMockMvc.perform(get("/api/catalogues/{id}", catalogue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(catalogue.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.partnerId").value(DEFAULT_PARTNER_ID))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.doubleValue()))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION))
            .andExpect(jsonPath("$.sessionDt").value(DEFAULT_SESSION_DT.toString()));
    }


    @Test
    @Transactional
    public void getCataloguesByIdFiltering() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        Long id = catalogue.getId();

        defaultCatalogueShouldBeFound("id.equals=" + id);
        defaultCatalogueShouldNotBeFound("id.notEquals=" + id);

        defaultCatalogueShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCatalogueShouldNotBeFound("id.greaterThan=" + id);

        defaultCatalogueShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCatalogueShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllCataloguesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where description equals to DEFAULT_DESCRIPTION
        defaultCatalogueShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the catalogueList where description equals to UPDATED_DESCRIPTION
        defaultCatalogueShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where description not equals to DEFAULT_DESCRIPTION
        defaultCatalogueShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the catalogueList where description not equals to UPDATED_DESCRIPTION
        defaultCatalogueShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultCatalogueShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the catalogueList where description equals to UPDATED_DESCRIPTION
        defaultCatalogueShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where description is not null
        defaultCatalogueShouldBeFound("description.specified=true");

        // Get all the catalogueList where description is null
        defaultCatalogueShouldNotBeFound("description.specified=false");
    }
                @Test
    @Transactional
    public void getAllCataloguesByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where description contains DEFAULT_DESCRIPTION
        defaultCatalogueShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the catalogueList where description contains UPDATED_DESCRIPTION
        defaultCatalogueShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where description does not contain DEFAULT_DESCRIPTION
        defaultCatalogueShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the catalogueList where description does not contain UPDATED_DESCRIPTION
        defaultCatalogueShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }


    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId equals to DEFAULT_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.equals=" + DEFAULT_PARTNER_ID);

        // Get all the catalogueList where partnerId equals to UPDATED_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.equals=" + UPDATED_PARTNER_ID);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId not equals to DEFAULT_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.notEquals=" + DEFAULT_PARTNER_ID);

        // Get all the catalogueList where partnerId not equals to UPDATED_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.notEquals=" + UPDATED_PARTNER_ID);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId in DEFAULT_PARTNER_ID or UPDATED_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.in=" + DEFAULT_PARTNER_ID + "," + UPDATED_PARTNER_ID);

        // Get all the catalogueList where partnerId equals to UPDATED_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.in=" + UPDATED_PARTNER_ID);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId is not null
        defaultCatalogueShouldBeFound("partnerId.specified=true");

        // Get all the catalogueList where partnerId is null
        defaultCatalogueShouldNotBeFound("partnerId.specified=false");
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId is greater than or equal to DEFAULT_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.greaterThanOrEqual=" + DEFAULT_PARTNER_ID);

        // Get all the catalogueList where partnerId is greater than or equal to UPDATED_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.greaterThanOrEqual=" + UPDATED_PARTNER_ID);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId is less than or equal to DEFAULT_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.lessThanOrEqual=" + DEFAULT_PARTNER_ID);

        // Get all the catalogueList where partnerId is less than or equal to SMALLER_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.lessThanOrEqual=" + SMALLER_PARTNER_ID);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsLessThanSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId is less than DEFAULT_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.lessThan=" + DEFAULT_PARTNER_ID);

        // Get all the catalogueList where partnerId is less than UPDATED_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.lessThan=" + UPDATED_PARTNER_ID);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPartnerIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where partnerId is greater than DEFAULT_PARTNER_ID
        defaultCatalogueShouldNotBeFound("partnerId.greaterThan=" + DEFAULT_PARTNER_ID);

        // Get all the catalogueList where partnerId is greater than SMALLER_PARTNER_ID
        defaultCatalogueShouldBeFound("partnerId.greaterThan=" + SMALLER_PARTNER_ID);
    }


    @Test
    @Transactional
    public void getAllCataloguesByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price equals to DEFAULT_PRICE
        defaultCatalogueShouldBeFound("price.equals=" + DEFAULT_PRICE);

        // Get all the catalogueList where price equals to UPDATED_PRICE
        defaultCatalogueShouldNotBeFound("price.equals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price not equals to DEFAULT_PRICE
        defaultCatalogueShouldNotBeFound("price.notEquals=" + DEFAULT_PRICE);

        // Get all the catalogueList where price not equals to UPDATED_PRICE
        defaultCatalogueShouldBeFound("price.notEquals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price in DEFAULT_PRICE or UPDATED_PRICE
        defaultCatalogueShouldBeFound("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE);

        // Get all the catalogueList where price equals to UPDATED_PRICE
        defaultCatalogueShouldNotBeFound("price.in=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price is not null
        defaultCatalogueShouldBeFound("price.specified=true");

        // Get all the catalogueList where price is null
        defaultCatalogueShouldNotBeFound("price.specified=false");
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price is greater than or equal to DEFAULT_PRICE
        defaultCatalogueShouldBeFound("price.greaterThanOrEqual=" + DEFAULT_PRICE);

        // Get all the catalogueList where price is greater than or equal to UPDATED_PRICE
        defaultCatalogueShouldNotBeFound("price.greaterThanOrEqual=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price is less than or equal to DEFAULT_PRICE
        defaultCatalogueShouldBeFound("price.lessThanOrEqual=" + DEFAULT_PRICE);

        // Get all the catalogueList where price is less than or equal to SMALLER_PRICE
        defaultCatalogueShouldNotBeFound("price.lessThanOrEqual=" + SMALLER_PRICE);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price is less than DEFAULT_PRICE
        defaultCatalogueShouldNotBeFound("price.lessThan=" + DEFAULT_PRICE);

        // Get all the catalogueList where price is less than UPDATED_PRICE
        defaultCatalogueShouldBeFound("price.lessThan=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCataloguesByPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where price is greater than DEFAULT_PRICE
        defaultCatalogueShouldNotBeFound("price.greaterThan=" + DEFAULT_PRICE);

        // Get all the catalogueList where price is greater than SMALLER_PRICE
        defaultCatalogueShouldBeFound("price.greaterThan=" + SMALLER_PRICE);
    }


    @Test
    @Transactional
    public void getAllCataloguesByDurationIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration equals to DEFAULT_DURATION
        defaultCatalogueShouldBeFound("duration.equals=" + DEFAULT_DURATION);

        // Get all the catalogueList where duration equals to UPDATED_DURATION
        defaultCatalogueShouldNotBeFound("duration.equals=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration not equals to DEFAULT_DURATION
        defaultCatalogueShouldNotBeFound("duration.notEquals=" + DEFAULT_DURATION);

        // Get all the catalogueList where duration not equals to UPDATED_DURATION
        defaultCatalogueShouldBeFound("duration.notEquals=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration in DEFAULT_DURATION or UPDATED_DURATION
        defaultCatalogueShouldBeFound("duration.in=" + DEFAULT_DURATION + "," + UPDATED_DURATION);

        // Get all the catalogueList where duration equals to UPDATED_DURATION
        defaultCatalogueShouldNotBeFound("duration.in=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration is not null
        defaultCatalogueShouldBeFound("duration.specified=true");

        // Get all the catalogueList where duration is null
        defaultCatalogueShouldNotBeFound("duration.specified=false");
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration is greater than or equal to DEFAULT_DURATION
        defaultCatalogueShouldBeFound("duration.greaterThanOrEqual=" + DEFAULT_DURATION);

        // Get all the catalogueList where duration is greater than or equal to UPDATED_DURATION
        defaultCatalogueShouldNotBeFound("duration.greaterThanOrEqual=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration is less than or equal to DEFAULT_DURATION
        defaultCatalogueShouldBeFound("duration.lessThanOrEqual=" + DEFAULT_DURATION);

        // Get all the catalogueList where duration is less than or equal to SMALLER_DURATION
        defaultCatalogueShouldNotBeFound("duration.lessThanOrEqual=" + SMALLER_DURATION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsLessThanSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration is less than DEFAULT_DURATION
        defaultCatalogueShouldNotBeFound("duration.lessThan=" + DEFAULT_DURATION);

        // Get all the catalogueList where duration is less than UPDATED_DURATION
        defaultCatalogueShouldBeFound("duration.lessThan=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllCataloguesByDurationIsGreaterThanSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where duration is greater than DEFAULT_DURATION
        defaultCatalogueShouldNotBeFound("duration.greaterThan=" + DEFAULT_DURATION);

        // Get all the catalogueList where duration is greater than SMALLER_DURATION
        defaultCatalogueShouldBeFound("duration.greaterThan=" + SMALLER_DURATION);
    }


    @Test
    @Transactional
    public void getAllCataloguesBySessionDtIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where sessionDt equals to DEFAULT_SESSION_DT
        defaultCatalogueShouldBeFound("sessionDt.equals=" + DEFAULT_SESSION_DT);

        // Get all the catalogueList where sessionDt equals to UPDATED_SESSION_DT
        defaultCatalogueShouldNotBeFound("sessionDt.equals=" + UPDATED_SESSION_DT);
    }

    @Test
    @Transactional
    public void getAllCataloguesBySessionDtIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where sessionDt not equals to DEFAULT_SESSION_DT
        defaultCatalogueShouldNotBeFound("sessionDt.notEquals=" + DEFAULT_SESSION_DT);

        // Get all the catalogueList where sessionDt not equals to UPDATED_SESSION_DT
        defaultCatalogueShouldBeFound("sessionDt.notEquals=" + UPDATED_SESSION_DT);
    }

    @Test
    @Transactional
    public void getAllCataloguesBySessionDtIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where sessionDt in DEFAULT_SESSION_DT or UPDATED_SESSION_DT
        defaultCatalogueShouldBeFound("sessionDt.in=" + DEFAULT_SESSION_DT + "," + UPDATED_SESSION_DT);

        // Get all the catalogueList where sessionDt equals to UPDATED_SESSION_DT
        defaultCatalogueShouldNotBeFound("sessionDt.in=" + UPDATED_SESSION_DT);
    }

    @Test
    @Transactional
    public void getAllCataloguesBySessionDtIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where sessionDt is not null
        defaultCatalogueShouldBeFound("sessionDt.specified=true");

        // Get all the catalogueList where sessionDt is null
        defaultCatalogueShouldNotBeFound("sessionDt.specified=false");
    }
    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCatalogueShouldBeFound(String filter) throws Exception {
        restCatalogueMockMvc.perform(get("/api/catalogues?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(catalogue.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].partnerId").value(hasItem(DEFAULT_PARTNER_ID)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].sessionDt").value(hasItem(DEFAULT_SESSION_DT.toString())));

        // Check, that the count call also returns 1
        restCatalogueMockMvc.perform(get("/api/catalogues/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCatalogueShouldNotBeFound(String filter) throws Exception {
        restCatalogueMockMvc.perform(get("/api/catalogues?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCatalogueMockMvc.perform(get("/api/catalogues/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingCatalogue() throws Exception {
        // Get the catalogue
        restCatalogueMockMvc.perform(get("/api/catalogues/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCatalogue() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        int databaseSizeBeforeUpdate = catalogueRepository.findAll().size();

        // Update the catalogue
        Catalogue updatedCatalogue = catalogueRepository.findById(catalogue.getId()).get();
        // Disconnect from session so that the updates on updatedCatalogue are not directly saved in db
        em.detach(updatedCatalogue);
        updatedCatalogue
            .description(UPDATED_DESCRIPTION)
            .partnerId(UPDATED_PARTNER_ID)
            .price(UPDATED_PRICE)
            .duration(UPDATED_DURATION)
            .sessionDt(UPDATED_SESSION_DT);
        CatalogueDTO catalogueDTO = catalogueMapper.toDto(updatedCatalogue);

        restCatalogueMockMvc.perform(put("/api/catalogues")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogueDTO)))
            .andExpect(status().isOk());

        // Validate the Catalogue in the database
        List<Catalogue> catalogueList = catalogueRepository.findAll();
        assertThat(catalogueList).hasSize(databaseSizeBeforeUpdate);
        Catalogue testCatalogue = catalogueList.get(catalogueList.size() - 1);
        assertThat(testCatalogue.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCatalogue.getPartnerId()).isEqualTo(UPDATED_PARTNER_ID);
        assertThat(testCatalogue.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testCatalogue.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testCatalogue.getSessionDt()).isEqualTo(UPDATED_SESSION_DT);

        // Validate the Catalogue in Elasticsearch
        verify(mockCatalogueSearchRepository, times(1)).save(testCatalogue);
    }

    @Test
    @Transactional
    public void updateNonExistingCatalogue() throws Exception {
        int databaseSizeBeforeUpdate = catalogueRepository.findAll().size();

        // Create the Catalogue
        CatalogueDTO catalogueDTO = catalogueMapper.toDto(catalogue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCatalogueMockMvc.perform(put("/api/catalogues")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(catalogueDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Catalogue in the database
        List<Catalogue> catalogueList = catalogueRepository.findAll();
        assertThat(catalogueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Catalogue in Elasticsearch
        verify(mockCatalogueSearchRepository, times(0)).save(catalogue);
    }

    @Test
    @Transactional
    public void deleteCatalogue() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        int databaseSizeBeforeDelete = catalogueRepository.findAll().size();

        // Delete the catalogue
        restCatalogueMockMvc.perform(delete("/api/catalogues/{id}", catalogue.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Catalogue> catalogueList = catalogueRepository.findAll();
        assertThat(catalogueList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Catalogue in Elasticsearch
        verify(mockCatalogueSearchRepository, times(1)).deleteById(catalogue.getId());
    }

    @Test
    @Transactional
    public void searchCatalogue() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);
        when(mockCatalogueSearchRepository.search(queryStringQuery("id:" + catalogue.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(catalogue), PageRequest.of(0, 1), 1));

        // Search the catalogue
        restCatalogueMockMvc.perform(get("/api/_search/catalogues?query=id:" + catalogue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(catalogue.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].partnerId").value(hasItem(DEFAULT_PARTNER_ID)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].sessionDt").value(hasItem(DEFAULT_SESSION_DT.toString())));
    }
}
