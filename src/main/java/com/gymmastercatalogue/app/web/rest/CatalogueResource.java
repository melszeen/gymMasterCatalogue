package com.gymmastercatalogue.app.web.rest;

import com.gymmastercatalogue.app.service.CatalogueService;
import com.gymmastercatalogue.app.web.rest.errors.BadRequestAlertException;
import com.gymmastercatalogue.app.service.dto.CatalogueDTO;
import com.gymmastercatalogue.app.service.dto.CatalogueCriteria;
import com.gymmastercatalogue.app.service.CatalogueQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing {@link com.gymmastercatalogue.app.domain.Catalogue}.
 */
@RestController
@RequestMapping("/api")
public class CatalogueResource {

    private final Logger log = LoggerFactory.getLogger(CatalogueResource.class);

    private static final String ENTITY_NAME = "gymMasterCatalogueCatalogue";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CatalogueService catalogueService;

    private final CatalogueQueryService catalogueQueryService;

    public CatalogueResource(CatalogueService catalogueService, CatalogueQueryService catalogueQueryService) {
        this.catalogueService = catalogueService;
        this.catalogueQueryService = catalogueQueryService;
    }

    /**
     * {@code POST  /catalogues} : Create a new catalogue.
     *
     * @param catalogueDTO the catalogueDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new catalogueDTO, or with status {@code 400 (Bad Request)} if the catalogue has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/catalogues")
    public ResponseEntity<CatalogueDTO> createCatalogue(@Valid @RequestBody CatalogueDTO catalogueDTO) throws URISyntaxException {
        log.debug("REST request to save Catalogue : {}", catalogueDTO);
        if (catalogueDTO.getId() != null) {
            throw new BadRequestAlertException("A new catalogue cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CatalogueDTO result = catalogueService.save(catalogueDTO);
        return ResponseEntity.created(new URI("/api/catalogues/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /catalogues} : Updates an existing catalogue.
     *
     * @param catalogueDTO the catalogueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated catalogueDTO,
     * or with status {@code 400 (Bad Request)} if the catalogueDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the catalogueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/catalogues")
    public ResponseEntity<CatalogueDTO> updateCatalogue(@Valid @RequestBody CatalogueDTO catalogueDTO) throws URISyntaxException {
        log.debug("REST request to update Catalogue : {}", catalogueDTO);
        if (catalogueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CatalogueDTO result = catalogueService.save(catalogueDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, catalogueDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /catalogues} : get all the catalogues.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of catalogues in body.
     */
    @GetMapping("/catalogues")
    public ResponseEntity<List<CatalogueDTO>> getAllCatalogues(CatalogueCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Catalogues by criteria: {}", criteria);
        Page<CatalogueDTO> page = catalogueQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /catalogues/count} : count all the catalogues.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/catalogues/count")
    public ResponseEntity<Long> countCatalogues(CatalogueCriteria criteria) {
        log.debug("REST request to count Catalogues by criteria: {}", criteria);
        return ResponseEntity.ok().body(catalogueQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /catalogues/:id} : get the "id" catalogue.
     *
     * @param id the id of the catalogueDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the catalogueDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/catalogues/{id}")
    public ResponseEntity<CatalogueDTO> getCatalogue(@PathVariable Long id) {
        log.debug("REST request to get Catalogue : {}", id);
        Optional<CatalogueDTO> catalogueDTO = catalogueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(catalogueDTO);
    }

    /**
     * {@code DELETE  /catalogues/:id} : delete the "id" catalogue.
     *
     * @param id the id of the catalogueDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/catalogues/{id}")
    public ResponseEntity<Void> deleteCatalogue(@PathVariable Long id) {
        log.debug("REST request to delete Catalogue : {}", id);
        catalogueService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code SEARCH  /_search/catalogues?query=:query} : search for the catalogue corresponding
     * to the query.
     *
     * @param query the query of the catalogue search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/catalogues")
    public ResponseEntity<List<CatalogueDTO>> searchCatalogues(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Catalogues for query {}", query);
        Page<CatalogueDTO> page = catalogueService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
        }
}
