package org.example.respawnmarket.repositories;

import org.example.respawnmarket.entities.PostalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostalRepository
        extends JpaRepository<PostalEntity, Integer>
{
}
