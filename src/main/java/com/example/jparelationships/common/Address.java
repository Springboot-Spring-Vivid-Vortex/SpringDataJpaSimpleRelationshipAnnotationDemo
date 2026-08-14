package com.example.jparelationships.common;

import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * A value object embedded directly inside owning entities (see
 * {@link com.example.jparelationships.onetomany.Employee#getHomeAddress()}).
 *
 * <p>Full write-up: {@code /doc/embeddable.md}
 *
 * <h2>The gist</h2>
 * {@code @Embeddable} marks a class that has <b>no identity of its own</b> and
 * <b>no separate database table</b>. Its fields are copied, column by column,
 * into whatever entity embeds it via {@code @Embedded}. It is the JPA
 * equivalent of "flattening" a nested Java object into the parent's row.
 *
 * <p>Compare this to {@code @OneToOne}: a one-to-one association points to a
 * <i>separate row</i> (with its own primary key) in a <i>separate table</i>.
 * An {@code @Embeddable} has no primary key and no table of its own - it is
 * just a convenient grouping of columns that live in whichever table embeds
 * it.
 */
@Embeddable
public class Address {

    // Column names default to the field name (e.g. "street", "city").
    // We could rename them with @AttributeOverride on the @Embedded field if
    // an entity needed to embed the same Address type more than once - see
    // the note in doc/embeddable.md.
    private String street;
    private String city;
    private String postalCode;
    private String country;

    /** JPA requires a no-arg constructor (may be protected/package-private). */
    protected Address() {
    }

    public Address(String street, String city, String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return street + ", " + city + " " + postalCode + ", " + country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(street, address.street)
                && Objects.equals(city, address.city)
                && Objects.equals(postalCode, address.postalCode)
                && Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, postalCode, country);
    }
}
