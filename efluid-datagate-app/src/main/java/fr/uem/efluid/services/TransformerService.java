package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.repositories.TransformerSetEntryRepository;
import fr.uem.efluid.model.repositories.TransformerSetRepository;
import fr.uem.efluid.model.repositories.TransformerSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Service
@Transactional
public class TransformerService {

    @Autowired
    private TransformerSourceRepository sources;

    @Autowired
    private TransformerSetRepository sets;

    @Autowired
    private TransformerSetEntryRepository setEntries;

    void prepareAssociatedTransformerSet(Commit commit) {

    }
}
