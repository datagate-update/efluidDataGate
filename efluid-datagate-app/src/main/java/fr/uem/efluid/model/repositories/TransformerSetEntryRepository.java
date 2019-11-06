package fr.uem.efluid.model.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.uem.efluid.model.entities.TransformerSetEntry;

/**
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public interface TransformerSetEntryRepository extends JpaRepository<TransformerSetEntry, UUID> {

	// Default
}
