package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InspectionRepository extends JpaRepository<InspectionEntity, Integer>
{

}