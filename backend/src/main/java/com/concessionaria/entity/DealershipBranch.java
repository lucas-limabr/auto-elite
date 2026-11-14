package com.concessionaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dealership_branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealershipBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxDailyTestDrives = 10;
}

