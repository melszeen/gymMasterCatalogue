package com.gymmastercatalogue.app.service;

import com.gymmastercatalogue.app.service.dto.CatalogueDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.gymmastercatalogue.app.domain.Catalogue}.
 */
public interface CatalogueService {

    /**
     * Save a catalogue.
     *
     * @param catalogueDTO the entity to save.
     * @return the persisted entity.
     */
    CatalogueDTO save(CatalogueDTO catalogueDTO);

    /**
     * Get all the catalogues.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CatalogueDTO> findAll(Pageable pageable);


    /**
     * Get the "id" catalogue.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CatalogueDTO> findOne(Long id);

    /**
     * Delete the "id" catalogue.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the catalogue corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CatalogueDTO> search(String query, Pageable pageable);
}
