package messages.errors.setting;

import messages.MessageHandler;
import messages.errors.ErrorBuilder;

/**
 * An {@link ErrorBuilder} that will create a {@link SettingError}.
 * @author Daniel Levy
 */
public class SettingErrorBuilder extends ErrorBuilder {

    /**
     * Default constructor for {@link SettingErrorBuilder}.
     * @param handler The {@link MessageHandler} that will store the current {@link Error} object.
     */
    public SettingErrorBuilder(MessageHandler handler) { super(new SettingError(),handler); }
}
