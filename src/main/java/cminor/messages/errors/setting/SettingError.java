package cminor.messages.errors.setting;

import cminor.messages.errors.Error;

/**
 * An {@link Error} generated for any invalid compiler-related settings.
 * @author Daniel Levy
 */
public class SettingError extends Error {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String buildMessageHeader(String fileName) { return "Setting Error: "; }
}
