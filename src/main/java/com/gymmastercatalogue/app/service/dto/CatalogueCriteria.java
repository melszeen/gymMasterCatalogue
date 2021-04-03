package com.gymmastercatalogue.app.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.InstantFilter;

/**
 * Criteria class for the {@link com.gymmastercatalogue.app.domain.Catalogue} entity. This class is used
 * in {@link com.gymmastercatalogue.app.web.rest.CatalogueResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /catalogues?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CatalogueCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter description;

    private IntegerFilter partnerId;

    private DoubleFilter price;

    private IntegerFilter duration;

    private InstantFilter sessionDt;

    public CatalogueCriteria() {
    }

    public CatalogueCriteria(CatalogueCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.description = other.description == null ? null : other.description.copy();
        this.partnerId = other.partnerId == null ? null : other.partnerId.copy();
        this.price = other.price == null ? null : other.price.copy();
        this.duration = other.duration == null ? null : other.duration.copy();
        this.sessionDt = other.sessionDt == null ? null : other.sessionDt.copy();
    }

    @Override
    public CatalogueCriteria copy() {
        return new CatalogueCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getDescription() {
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public IntegerFilter getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(IntegerFilter partnerId) {
        this.partnerId = partnerId;
    }

    public DoubleFilter getPrice() {
        return price;
    }

    public void setPrice(DoubleFilter price) {
        this.price = price;
    }

    public IntegerFilter getDuration() {
        return duration;
    }

    public void setDuration(IntegerFilter duration) {
        this.duration = duration;
    }

    public InstantFilter getSessionDt() {
        return sessionDt;
    }

    public void setSessionDt(InstantFilter sessionDt) {
        this.sessionDt = sessionDt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CatalogueCriteria that = (CatalogueCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(description, that.description) &&
            Objects.equals(partnerId, that.partnerId) &&
            Objects.equals(price, that.price) &&
            Objects.equals(duration, that.duration) &&
            Objects.equals(sessionDt, that.sessionDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        description,
        partnerId,
        price,
        duration,
        sessionDt
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CatalogueCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (description != null ? "description=" + description + ", " : "") +
                (partnerId != null ? "partnerId=" + partnerId + ", " : "") +
                (price != null ? "price=" + price + ", " : "") +
                (duration != null ? "duration=" + duration + ", " : "") +
                (sessionDt != null ? "sessionDt=" + sessionDt + ", " : "") +
            "}";
    }

}
