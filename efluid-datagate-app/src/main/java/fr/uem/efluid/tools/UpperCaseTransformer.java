package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import org.springframework.stereotype.Component;

/**
 * Transformer example
 */
@Component
public class UpperCaseTransformer extends ColumnTransformer<UpperCaseTransformer.Config, UpperCaseTransformer.Runner> {

    @Override
    public String getName() {
        return "UPPERCASE_TRANSFORMER";
    }

    @Override
    protected UpperCaseTransformer.Config newConfig() {
        return new UpperCaseTransformer.Config();
    }

    @Override
    protected Runner runner(UpperCaseTransformer.Config config, DictionaryEntry dict) {
        return new Runner(config, dict);
    }

    public static class Config extends ColumnTransformer.Config {

        public Config(){
            super();
        }
    }

    public static class Runner extends ColumnTransformer.Runner<UpperCaseTransformer.Config> {

        public Runner(UpperCaseTransformer.Config config, DictionaryEntry dict) {
            super(config, dict);
        }

        @Override
        protected String transformValue(String value, ColumnType type) {
            return value.toUpperCase();
        }
    }
}