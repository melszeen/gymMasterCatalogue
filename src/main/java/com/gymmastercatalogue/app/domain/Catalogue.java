package com.gymmastercatalogue.app.domain;


import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Catalogue.
 */
@Entity
@Table(name = "catalogue")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "catalogue")
public class Catalogue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "partner_id", nullable = false)
    private Integer partnerId;

    @DecimalMin(value = "0")
    @Column(name = "price")
    private Double price;

    @Min(value = 0)
    @Column(name = "duration")
    private Integer duration;

    @Column(name = "session_dt")
    private Instant sessionDt;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public Catalogue description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public Catalogue partnerId(Integer partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Double getPrice() {
        return price;
    }

    public Catalogue price(Double price) {
        this.price = price;
        return this;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getDuration() {
        return duration;
    }

    public Catalogue duration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Instant getSessionDt() {
        return sessionDt;
    }

    public Catalogue sessionDt(Instant sessionDt) {
        this.sessionDt = sessionDt;
        return this;
    }

    public void setSessionDt(Instant sessionDt) {
        this.sessionDt = sessionDt;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Catalogue)) {
            return false;
        }
        return id != null && id.equals(((Catalogue) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Catalogue{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", partnerId=" + getPartnerId() +
            ", price=" + getPrice() +
            ", duration=" + getDuration() +
            ", sessionDt='" + getSessionDt() + "'" +
            "}";
    }
}
