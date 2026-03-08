package com.respawn.repositories;

import com.respawn.entities.PawnshopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PawnshopRepository extends JpaRepository<PawnshopEntity, Integer>
{
}
