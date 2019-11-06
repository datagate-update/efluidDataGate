package fr.uem.efluid.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.TransformerSource;
import fr.uem.efluid.model.repositories.TransformerSetEntryRepository;
import fr.uem.efluid.model.repositories.TransformerSetRepository;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Service
@Transactional
public class TransformerService {

	@Autowired
	private TransformerSource source;
	
	@Autowired
	private TransformerSetRepository sets;
	
	@Autowired
	private TransformerSetEntryRepository setEntries;
	
	void prepareAssociatedTransformerSet(Commit commit) {
		
	}
}
