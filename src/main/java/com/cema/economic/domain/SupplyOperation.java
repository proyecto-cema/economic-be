package com.cema.economic.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplyOperation {
    @ApiModelProperty(notes = "The transaction Id of this operation, autogenerated.", example = "b000bba4-229e-4b59-8548-1c26508e459c")
    private UUID id;
    @ApiModelProperty(notes = "Any additional data for this operation", example = "Realizada en efectivo")
    private String description;
    @ApiModelProperty(notes = "The cuig this operation is related to", example = "123")
    @NotEmpty(message = "establishmentCuig is required")
    private String establishmentCuig;
    @ApiModelProperty(notes = "The amount of money this operation costs", example = "5432")
    @NotNull
    @Min(value = 1L, message = "Amount must be positive")
    private Long amount;
    @ApiModelProperty(notes = "The type of operation", example = "buy|use|loss")
    @NotEmpty(message = "type is required")
    @Pattern(regexp = "(?i)buy|use|loss")
    private String operationType;
    @ApiModelProperty(notes = "The the username of the operator who created this operation", example = "merlinds")
    @NotEmpty(message = "operator username is required")
    private String operatorUserName;
    @ApiModelProperty(notes = "The date when this operation took place", example = "2021-02-12")
    @NotNull
    private Date transactionDate;
    @ApiModelProperty(notes = "The name of the supply involved in this operation", example = "heno")
    @NotEmpty(message = "supply name is required")
    private String supplyName;
}
