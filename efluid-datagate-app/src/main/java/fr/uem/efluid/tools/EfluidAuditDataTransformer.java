package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

import static fr.uem.efluid.tools.Transformer.TransformerRunner.transformedValue;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.*;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Transformer for Efluid Audit data update at merge
 * <p>
 * Example of simulated query
 * <pre>
 *     update TZONEIHM set ACTEURCREATION = 'evt 156444' , DATECREATION = 'current_date' , ACTEURMODIFICATION = 'current_date' ,
 *     DATEMODIFICATION = 'evt 154654' , ACTEURSUPPRESSION = 'evt 189445' , DATESUPPRESSION = 'current_date' where ID like 'TRA$%' and etatobjet = 0;
 * </pre>
 *
 * @author elecomte
 * @version 2
 * @since v1.2.0
 */
@Component
public class EfluidAuditDataTransformer extends Transformer<EfluidAuditDataTransformer.Config, EfluidAuditDataTransformer.Runner> {

    public static final String CURRENT_DATE_EXPR = "current_date";

    private final ManagedQueriesGenerator queriesGenerator;

    @Autowired
    public EfluidAuditDataTransformer(ManagedValueConverter converter, TransformerValueProvider provider, ManagedQueriesGenerator queriesGenerator) {
        super(converter, provider);
        this.queriesGenerator = queriesGenerator;
    }

    @Override
    public String getName() {
        return "EFLUID_AUDIT";
    }

    @Override
    protected Config newConfig() {
        return new Config();
    }

    @Override
    protected Runner runner(Config config, DictionaryEntry dict) {

        return new Runner(getValueProvider(), config, dict, this.queriesGenerator);
    }

    public static class Config extends Transformer.TransformerConfig {

        private List<String> appliedKeyPatterns;

        private Map<String, String> appliedValueFilterPatterns;

        private Map<String, String> dateUpdates;

        private Map<String, String> actorUpdates;

        @JsonIgnore
        private List<Pattern> appliedKeyMatchers;

        @JsonIgnore
        private List<Pattern> appliedValueColumnMatchers;

        @JsonIgnore
        private Map<Pattern, String> appliedUpdateColumnMatchers;

        @JsonIgnore
        private Map<String, List<Pattern>> appliedValueFilterMatchers;

        public Config() {
            super();
        }

        public List<String> getAppliedKeyPatterns() {
            return this.appliedKeyPatterns;
        }

        public void setAppliedKeyPatterns(List<String> appliedKeyPatterns) {
            this.appliedKeyPatterns = appliedKeyPatterns;
        }

        public Map<String, String> getAppliedValueFilterPatterns() {
            return this.appliedValueFilterPatterns;
        }

        public void setAppliedValueFilterPatterns(Map<String, String> appliedValueFilterPatterns) {
            this.appliedValueFilterPatterns = appliedValueFilterPatterns;
        }

        public Map<String, String> getDateUpdates() {
            return this.dateUpdates;
        }

        public void setDateUpdates(Map<String, String> dateUpdates) {
            this.dateUpdates = dateUpdates;
        }

        public Map<String, String> getActorUpdates() {
            return this.actorUpdates;
        }

        public void setActorUpdates(Map<String, String> actorUpdates) {
            this.actorUpdates = actorUpdates;
        }

        @Override
        void populateDefault() {
            super.populateDefault();
            this.appliedKeyPatterns = new ArrayList<>();
            this.appliedKeyPatterns.add("TRA$.*");
            this.appliedValueFilterPatterns = new HashMap<>();
            this.appliedValueFilterPatterns.put("ETATOBJET", "0");
            this.dateUpdates = new HashMap<>();
            this.dateUpdates.put("DATECREATION", "current_date");
            this.dateUpdates.put("DATEMODIFICATION", "current_date");
            this.dateUpdates.put("DATESUPPRESSION", "current_date");
            this.actorUpdates = new HashMap<>();
            this.actorUpdates.put("ACTEURCREATION", "evt 156444");
            this.actorUpdates.put("ACTEURMODIFICATION", "evt 154654");
            this.actorUpdates.put("ACTEURSUPPRESSION", "evt 189445");
        }

        @Override
        void checkContentIsValid(List<String> errors) {
            super.checkContentIsValid(errors);
            if (this.appliedKeyPatterns == null || this.appliedKeyPatterns.size() == 0) {
                errors.add("At least one key value pattern must be specified. Use \".*\" as default to match all");
            }
            if ((this.dateUpdates == null || this.dateUpdates.size() == 0) && (this.actorUpdates == null || this.actorUpdates.size() == 0)) {
                errors.add("At least one update on date or actor must be specified.");
            }
            if (this.dateUpdates != null) {
                if (this.dateUpdates.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("A date update column name cannot be empty. Use \".*\" as default to match all");
                }
                if (this.dateUpdates.values().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("A date update value cannot be empty. Use \"" + CURRENT_DATE_EXPR + "\" for current date or a fixed date using format \"" + FormatUtils.DATE_FORMAT + "\"");
                } else if (this.dateUpdates.values().stream().anyMatch(v -> !CURRENT_DATE_EXPR.equals(v) && !FormatUtils.canParseLd(v))) {
                    errors.add("A date update value must be \"current_date\" or a fixed date value using format \"" + FormatUtils.DATE_FORMAT + "\"");
                }
            }
            if (this.appliedValueFilterPatterns != null) {
                if (this.appliedValueFilterPatterns.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("Value filter column name cannot be empty. Use \".*\" as default to match all, or remove all filter patterns");
                }
            }
            if (this.actorUpdates != null) {
                if (this.actorUpdates.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update column name cannot be empty. Use \".*\" as default to match all");
                }
                if (this.actorUpdates.values().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update value cannot be empty. Specify a valid actor name value");
                }
            }
        }

        boolean isEntryMatches(PreparedIndexEntry entry) {

            // Preload patterns for keys (as this)
            if (this.appliedKeyMatchers == null) {
                this.appliedKeyMatchers = this.appliedKeyPatterns != null
                        ? this.appliedKeyPatterns.stream().map(Pattern::compile).collect(toList())
                        : Collections.emptyList();
            }

            // Continue only if key at least match
            if (this.appliedKeyMatchers.size() > 0 && this.appliedKeyMatchers.stream().noneMatch(c -> c.matcher(entry.getKeyValue()).matches())) {
                return false;
            }

            // Preload patterns for filter values (get columns and compile)
            if (this.appliedValueColumnMatchers == null) {
                this.appliedValueColumnMatchers = this.appliedValueFilterPatterns != null
                        ? generatePayloadMatchersFromColumnPatterns(this.appliedValueFilterPatterns.keySet().stream())
                        : Collections.emptyList();
            }

            // Continue only if value pattern may match (check that column at least exists)
            return this.appliedValueColumnMatchers.size() <= 0 || this.appliedValueColumnMatchers.stream().anyMatch(c -> c.matcher(entry.getPayload()).matches());
        }

        boolean isValueFilterMatches(List<Value> values) {
            if (this.appliedValueFilterMatchers == null) {
                this.appliedValueFilterMatchers =
                        this.appliedValueFilterPatterns != null
                                ? this.appliedValueFilterPatterns.entrySet().stream()
                                .collect(groupingBy(
                                        Map.Entry::getKey,
                                        mapping(e -> compile(e.getValue()), toList())))
                                : new HashMap<>();
            }

            // Match all if no filter
            if (this.appliedValueFilterMatchers.size() == 0) {
                return true;
            }

            return values.stream()
                    .filter(v -> this.appliedValueFilterMatchers.containsKey(v.getName()))
                    .anyMatch(v -> {
                        String val = v.getValueAsString();
                        return this.appliedValueFilterMatchers.get(v.getName()).stream().anyMatch(c -> c.matcher(val).matches());
                    });
        }

        /**
         * From the columns specified in a DictionaryEntry, keep only the ones which would have been matched by current config, to add them.
         * Values are specified as TransformedValue to allow erasure (can be a "null" value, which is valid)
         *
         * @param possibleColumnsFromDictEntry columns of current DictEntry. Keep order if any
         * @param provider                     date provider for transformation
         * @return exactly transformed columns mapped to their optional values.
         */
        Map<String, NullableValue> getColumnTransformationsFromDefinitions(
                Collection<String> possibleColumnsFromDictEntry,
                TransformerValueProvider provider) {

            String currentTime = provider.getFormatedCurrentTime();

            // 1st step : get "actor" values mapped to pattern
            Map<Pattern, NullableValue> replacementPatterns = getActorUpdates() != null
                    ? new HashMap<>(
                    getActorUpdates().entrySet().stream()
                            .collect(toMap(
                                    e -> compile(e.getKey()),
                                    e -> isEmpty(e.getValue())
                                            ? new NullableValue(ColumnType.STRING, null)
                                            : new NullableValue(ColumnType.STRING, e.getValue()))
                            ))
                    : new HashMap<>();

            // 2nd step : get "date" values mapped to pattern, added to actor ones
            if (getDateUpdates() != null) {
                getDateUpdates().forEach((k, v) -> {
                    if (isEmpty(v)) {
                        replacementPatterns.put(compile(k), new NullableValue(ColumnType.TEMPORAL, null));
                    } else if (CURRENT_DATE_EXPR.equals(v)) {
                        replacementPatterns.put(compile(k), new NullableValue(ColumnType.TEMPORAL, currentTime));
                    } else {
                        replacementPatterns.put(compile(k), new NullableValue(ColumnType.TEMPORAL, FormatUtils.format(FormatUtils.parseLd(v).atStartOfDay())));
                    }
                });
            }

            // Will keep the columns natural order (which should be the selectClause order)
            Map<String, NullableValue> columnsWithReplacements = new LinkedHashMap<>();

            // Final step : for columns getting a corresponding match in patterns, map the found value
            possibleColumnsFromDictEntry.forEach(c ->
                    replacementPatterns.entrySet().stream()
                            .filter(e -> e.getKey().matcher(c).matches())
                            .findFirst() /* Use 1st found only if duplicated in actor / dates ! */
                            .ifPresent(e -> columnsWithReplacements.put(c, e.getValue()))

            );

            return columnsWithReplacements;
        }
    }

    /**
     * Transformation model for Efluid audit data - use the config to identify the transformation to process, and apply
     * them to the payload
     */
    public static class Runner extends Transformer.TransformerRunner<EfluidAuditDataTransformer.Config> {

        private final Map<String, NullableValue> mappedReplacedValues;

        private Runner(TransformerValueProvider provider, Config config, DictionaryEntry dict, ManagedQueriesGenerator queriesGenerator) {
            super(provider, config, dict);
            // Init content for a replacement process on specified columns
            this.mappedReplacedValues = this.config.getColumnTransformationsFromDefinitions(
                    queriesGenerator.splitSelectClause(this.dict.getSelectClause(), null, null),
                    this.provider);
        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return this.config.isEntryMatches(preparedIndexEntry);
        }

        /**
         * <p>The process here get the prepared "mappedReplacedValues" which are the new values (as NullableValue) mapped to the current dict entry column name.
         * This needs to be able to process various cases :
         * <ul>
         *     <li>If a value in the payload is not specified as transformed, don't touch it. For example value "A" stay "A"</li>
         *     <li>If a value in the payload is specified as transformed, replace its content with the transformed one. Even if the new content is "null". For example value "A" became "B" or null</li>
         *     <li>If a value which should be transformed is NOT in the payload, init it with the transformed content. For example value null (missing value) became "B"</li>
         * </ul>
         * </p>
         * <p>For the processed payload (as a list of <tt>Value</tt>), 2 steps :
         * <ul>
         *     <li>1st : For each payload value, apply the identified replacement if any. If the replacement is a null value, then the processed value is erased</li>
         *     <li>2nd : For each  prepared "mappedReplacedValues" column not processed (ie : columns not present in the current payload but with a configured transformation), create a new "transformed" value</li>
         * </ul>
         * </p>
         *
         * @param values the payload to process. The list can be modified (update in value content or added values)
         */
        @Override
        public void accept(List<Value> values) {
            if (this.config.isValueFilterMatches(values)) {

                List<String> toTransform = new ArrayList<>(this.mappedReplacedValues.keySet());

                // Process on indexed list for replacement support
                for (int i = 0; i < values.size(); i++) {
                    Value val = values.get(i);

                    NullableValue replacement = this.mappedReplacedValues.get(val.getName());

                    // If no optional => Not transformed
                    if (replacement != null) {
                        if (replacement.hasValue()) {
                            values.set(i, transformedValue(val, replacement.getValue()));
                        } else {
                            values.set(i, transformedValue(val, ""));
                        }
                        toTransform.remove(val.getName());
                    }
                }

                // Add new values for column missing in payload
                toTransform.forEach(c -> {
                    NullableValue replacement = this.mappedReplacedValues.get(c);
                    if (replacement.hasValue()) {
                        values.add(replacement.toTransformedValueOnMissingTarget(c));
                    }
                });
            }
        }
    }

    /**
     * Holder of a transformed value : can be a new value, or null (for erasure). Contains an "expected" type
     * to allow basic init even on missing columns in payload
     */
    private static class NullableValue {

        private final ColumnType type;
        private final String value;

        private NullableValue(ColumnType type, String value) {
            this.type = type;
            this.value = value;
        }

        public ColumnType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        boolean hasValue() {
            return this.value != null;
        }

        /**
         * Init a TransformedValue from a missing target (when a null or missing value of payload is transformed to something)
         *
         * @param colName
         * @return
         */
        Value toTransformedValueOnMissingTarget(final String colName) {
            return transformedValue(new Value() {
                @Override
                public String getName() {
                    return colName;
                }

                @Override
                public byte[] getValue() {
                    return null;
                }

                @Override
                public ColumnType getType() {
                    return NullableValue.this.type;
                }
            }, getValue());
        }
    }
}
