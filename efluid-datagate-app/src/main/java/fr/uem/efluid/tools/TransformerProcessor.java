package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.Commit;

import java.util.List;

public class TransformerProcessor {

    /**
     * <p>
     * Process payload transform if a compliant transformer is defined at import level
     * </p>
     *
     * @param commitToProcess
     */
    public void transformCommitToProcessOnImport(List<Commit> commitToProcess) {

        // Update payload on all concerned index entries
        if (commitToProcess.size() > 0) {
            commitToProcess.stream()
                    .flatMap(c -> c.getIndex().stream())
                    .filter(i -> this.transformer.isApplyOnDictionaryEntry(i.getDictionaryEntry()))
                    .forEach(i -> {
                        // Update payload with transformer
                        i.setPayload(this.transformer.transformPayload(i.getDictionaryEntry(), i.getPayload()));
                    });
        }
    }
}
