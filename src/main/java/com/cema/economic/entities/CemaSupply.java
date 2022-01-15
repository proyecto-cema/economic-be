package com.cema.economic.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "supply")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaSupply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "units")
    private String units;

    @Column(name = "price")
    private Long price;

    @Column(name = "establishment_cuig")
    private String establishmentCuig;

    @OneToOne()
    @JoinColumn(name = "category_name", referencedColumnName = "name")
    private CemaCategory category;
}
