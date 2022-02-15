package com.cema.economic.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "bovine_operation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaBovineOperation {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id")
    private UUID id;

    @Column(name = "bovine_tag")
    private String bovineTag;

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "buyer_name")
    private String buyerName;

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
}
