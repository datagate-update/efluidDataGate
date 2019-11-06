package fr.uem.efluid.model.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.TransformerSet;

/**
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public interface TransformerSetRepository extends JpaRepository<TransformerSet, UUID> {

	// Default
}
