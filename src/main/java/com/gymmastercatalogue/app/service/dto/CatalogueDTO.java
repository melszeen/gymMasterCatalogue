package com.gymmastercatalogue.app.service.dto;

import java.time.Instant;
import javax.validation.constraints.*;
import java.io.Serializable;
import com.gymmastercatalogue.app.domain.enumeration.categoryEnum;

/**
 * A DTO for the {@link com.gymmastercatalogue.app.domain.Catalogue} entity.
 */
public class CatalogueDTO implements Serializable {
    
    private Long id;

    private String description;

    @NotNull
    private Integer partnerId;

    @DecimalMin(value = "0")
    private Double price;

    @Min(value = 0)
    private Integer duration;

    private Instant sessionDt;

    @NotNull
    private categoryEnum category;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Instant getSessionDt() {
        return sessionDt;
    }

    public void setSessionDt(Instant sessionDt) {
        this.sessionDt = sessionDt;
    }

    public categoryEnum getCategory() {
        return category;
    }

    public void setCategory(categoryEnum category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CatalogueDTO)) {
            return false;
        }

        return id != null && id.equals(((CatalogueDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CatalogueDTO{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", partnerId=" + getPartnerId() +
            ", price=" + getPrice() +
            ", duration=" + getDuration() +
            ", sessionDt='" + getSessionDt() + "'" +
            ", category='" + getCategory() + "'" +
            "}";
    }
}
