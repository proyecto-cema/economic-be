package com.cema.economic.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

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

    @OneToMany(mappedBy = "cemaSupply", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<CemaSupplyOperation> cemaSupplyOperationList;

    @Override
    public String toString() {
        return "CemaSupply{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", units='" + units + '\'' +
                ", price=" + price +
                ", establishmentCuig='" + establishmentCuig + '\'' +
                ", category=" + category +
                '}';
    }
}
