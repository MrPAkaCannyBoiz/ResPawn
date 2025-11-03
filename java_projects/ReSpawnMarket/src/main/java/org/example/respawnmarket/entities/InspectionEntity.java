package org.example.respawnmarket.entities;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "inspection")
public class InspectionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;


}
