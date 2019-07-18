package fr.uem.efluid.services;

/**
 * <p>
 * Behavior activation, specified as code, initialized as standard application properties,
 * stored in database and updatable dynamically using specific service
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public enum Feature {

    /**
     * <p>
     * For managed database updates : control the missing ids in update queries
     * </p>
     */
    CHECK_MISSING_IDS_AT_MANAGED_UPDATE("datagate-efluid.managed-updates.check-update-missing-ids"),

    /**
     * <p>
     * For managed database updates : control the missing ids in delete queries
     * </p>
     */
    CHECK_MISSING_IDS_AT_MANAGED_DELETE("datagate-efluid.managed-updates.check-delete-missing-ids"),

    /**
     * <p>
     * Check the dictionary version during import of a commit
     * </p>
     */
    VALIDATE_VERSION_FOR_IMPORT("datagate-efluid.imports.check-model-version"),

    /**
     * <p>If enabled, the database PK are pre-specified as dictionary entry keys</p>
     */
    SELECT_PK_AS_DEFAULT_DICT_ENTRY_KEY("datagate-efluid.dictionary.select-pk-as-default-keys");

    private final String propertyKey;

    /**
     * @param propertyKey
     */
    private Feature(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * @return the propertyKey
     */
    public String getPropertyKey() {
        return this.propertyKey;
    }
}