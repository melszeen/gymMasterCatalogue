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

import com.gymmastercatalogue.app.domain.enumeration.categoryEnum;
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

    private static final Double DEFAULT_PRICE = 0D;
    private static final Double UPDATED_PRICE = 1D;
    private static final Double SMALLER_PRICE = 0D - 1D;

    private static final Integer DEFAULT_DURATION = 0;
    private static final Integer UPDATED_DURATION = 1;
    private static final Integer SMALLER_DURATION = 0 - 1;

    private static final Instant DEFAULT_SESSION_DT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SESSION_DT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final categoryEnum DEFAULT_CATEGORY = categoryEnum.HIIT;
    private static final categoryEnum UPDATED_CATEGORY = categoryEnum.GYM;

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

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
            .price(DEFAULT_PRICE)
            .duration(DEFAULT_DURATION)
            .sessionDt(DEFAULT_SESSION_DT)
            .category(DEFAULT_CATEGORY)
            .username(DEFAULT_USERNAME);
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
            .price(UPDATED_PRICE)
            .duration(UPDATED_DURATION)
            .sessionDt(UPDATED_SESSION_DT)
            .category(UPDATED_CATEGORY)
            .username(UPDATED_USERNAME);
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
        assertThat(testCatalogue.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testCatalogue.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testCatalogue.getSessionDt()).isEqualTo(DEFAULT_SESSION_DT);
        assertThat(testCatalogue.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testCatalogue.getUsername()).isEqualTo(DEFAULT_USERNAME);

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
    public void checkCategoryIsRequired() throws Exception {
        int databaseSizeBeforeTest = catalogueRepository.findAll().size();
        // set the field null
        catalogue.setCategory(null);

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
    public void checkUsernameIsRequired() throws Exception {
        int databaseSizeBeforeTest = catalogueRepository.findAll().size();
        // set the field null
        catalogue.setUsername(null);

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
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].sessionDt").value(hasItem(DEFAULT_SESSION_DT.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)));
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
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.doubleValue()))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION))
            .andExpect(jsonPath("$.sessionDt").value(DEFAULT_SESSION_DT.toString()))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME));
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

    @Test
    @Transactional
    public void getAllCataloguesByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where category equals to DEFAULT_CATEGORY
        defaultCatalogueShouldBeFound("category.equals=" + DEFAULT_CATEGORY);

        // Get all the catalogueList where category equals to UPDATED_CATEGORY
        defaultCatalogueShouldNotBeFound("category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllCataloguesByCategoryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where category not equals to DEFAULT_CATEGORY
        defaultCatalogueShouldNotBeFound("category.notEquals=" + DEFAULT_CATEGORY);

        // Get all the catalogueList where category not equals to UPDATED_CATEGORY
        defaultCatalogueShouldBeFound("category.notEquals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllCataloguesByCategoryIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where category in DEFAULT_CATEGORY or UPDATED_CATEGORY
        defaultCatalogueShouldBeFound("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY);

        // Get all the catalogueList where category equals to UPDATED_CATEGORY
        defaultCatalogueShouldNotBeFound("category.in=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    public void getAllCataloguesByCategoryIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where category is not null
        defaultCatalogueShouldBeFound("category.specified=true");

        // Get all the catalogueList where category is null
        defaultCatalogueShouldNotBeFound("category.specified=false");
    }

    @Test
    @Transactional
    public void getAllCataloguesByUsernameIsEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where username equals to DEFAULT_USERNAME
        defaultCatalogueShouldBeFound("username.equals=" + DEFAULT_USERNAME);

        // Get all the catalogueList where username equals to UPDATED_USERNAME
        defaultCatalogueShouldNotBeFound("username.equals=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    public void getAllCataloguesByUsernameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where username not equals to DEFAULT_USERNAME
        defaultCatalogueShouldNotBeFound("username.notEquals=" + DEFAULT_USERNAME);

        // Get all the catalogueList where username not equals to UPDATED_USERNAME
        defaultCatalogueShouldBeFound("username.notEquals=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    public void getAllCataloguesByUsernameIsInShouldWork() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where username in DEFAULT_USERNAME or UPDATED_USERNAME
        defaultCatalogueShouldBeFound("username.in=" + DEFAULT_USERNAME + "," + UPDATED_USERNAME);

        // Get all the catalogueList where username equals to UPDATED_USERNAME
        defaultCatalogueShouldNotBeFound("username.in=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    public void getAllCataloguesByUsernameIsNullOrNotNull() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where username is not null
        defaultCatalogueShouldBeFound("username.specified=true");

        // Get all the catalogueList where username is null
        defaultCatalogueShouldNotBeFound("username.specified=false");
    }
                @Test
    @Transactional
    public void getAllCataloguesByUsernameContainsSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where username contains DEFAULT_USERNAME
        defaultCatalogueShouldBeFound("username.contains=" + DEFAULT_USERNAME);

        // Get all the catalogueList where username contains UPDATED_USERNAME
        defaultCatalogueShouldNotBeFound("username.contains=" + UPDATED_USERNAME);
    }

    @Test
    @Transactional
    public void getAllCataloguesByUsernameNotContainsSomething() throws Exception {
        // Initialize the database
        catalogueRepository.saveAndFlush(catalogue);

        // Get all the catalogueList where username does not contain DEFAULT_USERNAME
        defaultCatalogueShouldNotBeFound("username.doesNotContain=" + DEFAULT_USERNAME);

        // Get all the catalogueList where username does not contain UPDATED_USERNAME
        defaultCatalogueShouldBeFound("username.doesNotContain=" + UPDATED_USERNAME);
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
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].sessionDt").value(hasItem(DEFAULT_SESSION_DT.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)));

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
            .price(UPDATED_PRICE)
            .duration(UPDATED_DURATION)
            .sessionDt(UPDATED_SESSION_DT)
            .category(UPDATED_CATEGORY)
            .username(UPDATED_USERNAME);
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
        assertThat(testCatalogue.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testCatalogue.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testCatalogue.getSessionDt()).isEqualTo(UPDATED_SESSION_DT);
        assertThat(testCatalogue.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testCatalogue.getUsername()).isEqualTo(UPDATED_USERNAME);

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
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)))
            .andExpect(jsonPath("$.[*].sessionDt").value(hasItem(DEFAULT_SESSION_DT.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)));
    }
}
