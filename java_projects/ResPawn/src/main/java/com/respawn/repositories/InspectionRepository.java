package com.respawn.repositories;

import com.respawn.entities.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<InspectionEntity, Integer>
{

}