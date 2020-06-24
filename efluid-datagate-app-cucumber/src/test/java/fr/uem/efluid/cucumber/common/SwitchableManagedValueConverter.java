package fr.uem.efluid.cucumber.common;

import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * ManagedValueConverter where the "keepEmptyValue" parameter can be switched dynamically for testing
 */
public class SwitchableManagedValueConverter extends ManagedValueConverter {

    private boolean keepEmpty;

    public void setKeepEmpty(boolean keepEmpty) {
        this.keepEmpty = keepEmpty;
    }

    @Override
    protected boolean isProcessValue(Object value) {
        return this.keepEmpty || value != null;
    }
}
