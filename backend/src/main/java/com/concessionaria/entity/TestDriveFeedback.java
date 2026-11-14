package com.concessionaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_drive_feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDriveFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comments;

    @Column(nullable = false)
    @Builder.Default
    private Boolean interestedInBuying = false;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private TestDriveAppointment appointment;
}

