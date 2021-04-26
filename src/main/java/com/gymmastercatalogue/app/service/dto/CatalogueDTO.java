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

    @DecimalMin(value = "0")
    private Double price;

    @Min(value = 0)
    private Integer duration;

    private Instant sessionDt;

    @NotNull
    private categoryEnum category;

    @NotNull
    private String username;

    
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
            ", price=" + getPrice() +
            ", duration=" + getDuration() +
            ", sessionDt='" + getSessionDt() + "'" +
            ", category='" + getCategory() + "'" +
            ", username='" + getUsername() + "'" +
            "}";
    }
}
