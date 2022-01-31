package com.cema.economic.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Supply {

    @ApiModelProperty(notes = "The units to measure this supply", example = "Kg")
    @NotEmpty(message = "Must specify the units for this supply")
    private String units;
    @ApiModelProperty(notes = "The name for this supply", example = "Heno")
    @NotEmpty(message = "Name is required")
    private String name;
    @ApiModelProperty(notes = "The price of every unit of this supply", example = "200")
    @Min(value = 0L, message = "The price must be positive")
    @NotNull(message = "Price is required")
    private Long price;
    @ApiModelProperty(notes = "The category of this supply", example = "Alimento")
    @NotEmpty(message = "Category name is required")
    private String categoryName;
    @ApiModelProperty(notes = "The cuig this operation is related to", example = "123")
    @NotEmpty(message = "establishmentCuig is required")
    private String establishmentCuig;
}
