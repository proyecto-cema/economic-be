package com.cema.economic.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableSupply {

    @ApiModelProperty(notes = "The name of the supply.", example = "Maiz")
    private String supplyName;
    @ApiModelProperty(notes = "The amount available to use.", example = "10")
    private long available;
    @ApiModelProperty(notes = "The units used to measure this supply.", example = "Kg")
    private String units;
}
