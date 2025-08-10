package messages;

import ast.AST;
import messages.errors.Error;
import messages.errors.scope.ScopeError;
import utilities.PrettyPrint;

/**
 * An abstract representation of a message.
 * <p>
 *     This class contains most of the meta information and helper methods needed
 *     for the compiler to generate a specific message to the user. Currently, we
 *     have two types of messages: {@link messages.errors.Error} and {@link messages.warnings.Warning}.
 * </p>
 * @author Daniel Levy
 */
public abstract class Message {

    /**
     * The actual message that will be printed to the user.
     */
    protected String msg;

    /**
     * The specific {@link AST} node where the message was generated for.
     */
    protected AST location;

    /**
     * {@link MessageNumber} to indicate the main message we want to display to a user.
     */
    protected MessageNumber messageType;

    /**
     * List of arguments that could be used to personalize the message.
     */
    protected Object[] args;

    /**
     * {@link MessageNumber} to indicate a sub message we might attach to the main message.
     */
    protected MessageNumber suggest;

    /**
     * List of arguments that could be used to personalize a suggestion accompanying the message.
     */
    protected Object[] suggestionArgs;

    /**
     * Creates the {@link #msg} that will be displayed to the user.
     * @param fileName The file in which the error is generated for (if applicable).
     */
    public void createMessage(String fileName) {
        msg = buildMessageHeader(fileName) + buildMainMessage() + buildLocationInfo() + buildSupplementalMessage();
    }

    /**
     * Builds a string that provides meta information about the message to the user.
     * @param fileName The file name  we want to include in the header (if in compilation mode or using imported files).
     * @return String representing the start of a message.
     */
    protected String buildMessageHeader(String fileName) { return (fileName.isEmpty()) ? "" : "In " + fileName + ": "; }

    /**
     * Builds a string representing the location in the program that the message is generated for.
     * @return String representation of the {@link #location}.
     */
    protected String buildLocationInfo() { return (location == null) ? "" : PrettyPrint.RESET + location.header(); }

    /**
     * Builds the main message that the user needs to see.
     * @return String representing the main message that will be displayed to the user.
     */
    protected String buildMainMessage() {
        if(messageType == null)
            throw new RuntimeException("The message being created was not given a specified message number.");

        String msg = messageType.getMessage() + "\n";

        // Replace every "<argN>" with the actual argument (where N <= len(args))
        if(args != null) {
            for(int i = 0; i < args.length; i++)
                msg = msg.replace("<arg"+i+">", args[i].toString());
        }

        return msg;
    }

    /**
     * Builds an optional supplemental message to give the user more context about the main message.
     * @return String representing a suggestion from the compiler to the user.
     */
    private String buildSupplementalMessage() {
        if(suggest == null)
            return "";

        String suggestion = "\nSuggestion:\n" + PrettyPrint.RED + suggest.getMessage() + PrettyPrint.RESET;

        // Replace every "<argN>" with the actual argument (where N <= len(args))
        if(suggestionArgs != null) {
            for(int i = 0; i < suggestionArgs.length; i++)
                suggestion = suggestion.replace("<arg"+i+">", suggestionArgs[i].toString());
        }

        return suggestion;
    }

    /**
     * Generates a string that contains the specific message number.
     * @return String containing the message number.
     */
    protected String messageNumber() {
        if(messageType == null)
            return "";
        return messageType.toString().substring(messageType.toString().lastIndexOf("_")+1);
    }

    /**
     * Returns {@link #msg} which stores the actual message that is printed to the user.
     * @return String representing the current {@link Message}.
     */
    public String getMessage() { return msg; }

    /**
     * Returns the {@link #location} in which the message was generated for.
     * @return {@link AST} node representing where the message was created.
     */
    public AST getLocation() { return location; }

    /**
     * Setter for {@link #location}.
     * @param location {@link AST}
     */
    public void setLocation(AST location) { this.location = location; }

    /**
     * Setter for {@link #messageType}.
     * @param messageNumber {@link MessageNumber}
     */
    public void setMessageNumber(MessageNumber messageNumber) { messageType = messageNumber; }

    /**
     * Setter for {@link #args}.
     * @param args An array of objects.
     */
    public void setArgs(Object[] args) { this.args = args; }

    /**
     * Setter for {@link #suggest}.
     * @param suggest {@link MessageNumber}
     */
    public void setSuggestionNumber(MessageNumber suggest) { this.suggest = suggest; }

    /**
     * Setter for {@link #suggestionArgs}.
     * @param args An array of objects
     */
    public void setSuggestionArgs(Object[] args) { this.suggestionArgs = args; }

    /**
     * Checks if the current {@link Message} object is an {@link Error}.
     * @return {@code True} if the object is an {@link Error}, {@code False} otherwise.
     */
    public boolean isError() { return false; }

    /**
     * Explicitly casts the current instance of {@link Message} into an {@link Error}.
     * @return Current instance of {@link Message} as an {@link Error}.
     */
    public Error asError() {
        throw new RuntimeException("The current message does not represent an Error.");
    }
}
