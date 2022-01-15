package com.cema.economic.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class Category {

    @ApiModelProperty(notes = "The name of this category", example = "Alimento")
    @NotEmpty(message = "Name is required")
    private String name;
    @ApiModelProperty(notes = "A simple description of this category", example = "alimento para los animales")
    @NotEmpty(message = "Description is required")
    private String description;
}
