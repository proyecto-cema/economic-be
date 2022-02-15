package com.cema.economic.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "supply_operation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaSupplyOperation {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id")
    private UUID id;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "establishment_cuig")
    private String establishmentCuig;

    @ManyToOne(cascade = { CascadeType.REMOVE })
    @JoinColumn(name="supply_id")
    private CemaSupply cemaSupply;

    @Override
    public String toString() {
        return "CemaSupplyOperation{" +
                "id=" + id +
                ", operationType='" + operationType + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", establishmentCuig='" + establishmentCuig + '\'' +
                ", cemaSupply=" + cemaSupply.getName() +
                '}';
    }
}
