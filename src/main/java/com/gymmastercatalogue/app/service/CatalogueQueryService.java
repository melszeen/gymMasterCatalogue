package com.gymmastercatalogue.app.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.gymmastercatalogue.app.domain.Catalogue;
import com.gymmastercatalogue.app.domain.*; // for static metamodels
import com.gymmastercatalogue.app.repository.CatalogueRepository;
import com.gymmastercatalogue.app.repository.search.CatalogueSearchRepository;
import com.gymmastercatalogue.app.service.dto.CatalogueCriteria;
import com.gymmastercatalogue.app.service.dto.CatalogueDTO;
import com.gymmastercatalogue.app.service.mapper.CatalogueMapper;

/**
 * Service for executing complex queries for {@link Catalogue} entities in the database.
 * The main input is a {@link CatalogueCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CatalogueDTO} or a {@link Page} of {@link CatalogueDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CatalogueQueryService extends QueryService<Catalogue> {

    private final Logger log = LoggerFactory.getLogger(CatalogueQueryService.class);

    private final CatalogueRepository catalogueRepository;

    private final CatalogueMapper catalogueMapper;

    private final CatalogueSearchRepository catalogueSearchRepository;

    public CatalogueQueryService(CatalogueRepository catalogueRepository, CatalogueMapper catalogueMapper, CatalogueSearchRepository catalogueSearchRepository) {
        this.catalogueRepository = catalogueRepository;
        this.catalogueMapper = catalogueMapper;
        this.catalogueSearchRepository = catalogueSearchRepository;
    }

    /**
     * Return a {@link List} of {@link CatalogueDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CatalogueDTO> findByCriteria(CatalogueCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Catalogue> specification = createSpecification(criteria);
        return catalogueMapper.toDto(catalogueRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CatalogueDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CatalogueDTO> findByCriteria(CatalogueCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Catalogue> specification = createSpecification(criteria);
        return catalogueRepository.findAll(specification, page)
            .map(catalogueMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CatalogueCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Catalogue> specification = createSpecification(criteria);
        return catalogueRepository.count(specification);
    }

    /**
     * Function to convert {@link CatalogueCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Catalogue> createSpecification(CatalogueCriteria criteria) {
        Specification<Catalogue> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Catalogue_.id));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), Catalogue_.description));
            }
            if (criteria.getPrice() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPrice(), Catalogue_.price));
            }
            if (criteria.getDuration() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDuration(), Catalogue_.duration));
            }
            if (criteria.getSessionDt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSessionDt(), Catalogue_.sessionDt));
            }
            if (criteria.getCategory() != null) {
                specification = specification.and(buildSpecification(criteria.getCategory(), Catalogue_.category));
            }
            if (criteria.getUsername() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUsername(), Catalogue_.username));
            }
        }
        return specification;
    }
}
