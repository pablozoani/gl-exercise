package com.example.gl_exercise.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "phones", uniqueConstraints = @UniqueConstraint(columnNames = {"number", "cityCode", "countryCode"}))
@Getter
@ToString(exclude = "user")
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    @Setter
    @Column(nullable = false)
    private Long number;

    @Setter
    @Column(nullable = false)
    private Integer cityCode;

    @Setter
    @Column(nullable = false)
    private String countryCode;

    protected Phone() {
    }

    public Phone(User user, Long number, Integer cityCode, String countryCode) {
        this.user = user;
        this.number = number;
        this.cityCode = cityCode;
        this.countryCode = countryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Phone that = (Phone) o;
        return Objects.equals(this.number, that.number) &&
            Objects.equals(this.cityCode, that.cityCode) &&
            Objects.equals(this.countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.number, this.cityCode, this.countryCode);
    }
}
