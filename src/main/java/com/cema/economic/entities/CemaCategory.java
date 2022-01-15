package com.cema.economic.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CemaCategory {
    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
