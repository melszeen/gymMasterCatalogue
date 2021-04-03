package com.gymmastercatalogue.app.service.impl;

import com.gymmastercatalogue.app.service.CatalogueService;
import com.gymmastercatalogue.app.domain.Catalogue;
import com.gymmastercatalogue.app.repository.CatalogueRepository;
import com.gymmastercatalogue.app.repository.search.CatalogueSearchRepository;
import com.gymmastercatalogue.app.service.dto.CatalogueDTO;
import com.gymmastercatalogue.app.service.mapper.CatalogueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing {@link Catalogue}.
 */
@Service
@Transactional
public class CatalogueServiceImpl implements CatalogueService {

    private final Logger log = LoggerFactory.getLogger(CatalogueServiceImpl.class);

    private final CatalogueRepository catalogueRepository;

    private final CatalogueMapper catalogueMapper;

    private final CatalogueSearchRepository catalogueSearchRepository;

    public CatalogueServiceImpl(CatalogueRepository catalogueRepository, CatalogueMapper catalogueMapper, CatalogueSearchRepository catalogueSearchRepository) {
        this.catalogueRepository = catalogueRepository;
        this.catalogueMapper = catalogueMapper;
        this.catalogueSearchRepository = catalogueSearchRepository;
    }

    @Override
    public CatalogueDTO save(CatalogueDTO catalogueDTO) {
        log.debug("Request to save Catalogue : {}", catalogueDTO);
        Catalogue catalogue = catalogueMapper.toEntity(catalogueDTO);
        catalogue = catalogueRepository.save(catalogue);
        CatalogueDTO result = catalogueMapper.toDto(catalogue);
        catalogueSearchRepository.save(catalogue);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogueDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Catalogues");
        return catalogueRepository.findAll(pageable)
            .map(catalogueMapper::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogueDTO> findOne(Long id) {
        log.debug("Request to get Catalogue : {}", id);
        return catalogueRepository.findById(id)
            .map(catalogueMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Catalogue : {}", id);
        catalogueRepository.deleteById(id);
        catalogueSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogueDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Catalogues for query {}", query);
        return catalogueSearchRepository.search(queryStringQuery(query), pageable)
            .map(catalogueMapper::toDto);
    }
}
