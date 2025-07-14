package messages.warnings;

import messages.Message;
import messages.MessageType;
import utilities.PrettyPrint;

public class Warning extends Message {

    private MessageType warning;

    protected String warningHeader() {
        return PrettyPrint.PINK
                + "Warning "
                + warning.toString().substring(warning.toString().lastIndexOf("_")+1)
                + "\n"
                + PrettyPrint.RESET;
    }

    public String createMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(fileHeader());
        sb.append(warningHeader());

        if(location != null)
            sb.append(location.header());

        sb.append(buildWarning());
        return sb.toString();
    }

    public String printMessage() {
        if(interpretMode) {
            System.out.println(createMessage());
            return "";
        }
        System.out.println(createMessage());
        return "";
    }

    private String buildWarning() {
        String warningMsg = PrettyPrint.YELLOW + this.warning.getMessage() + PrettyPrint.RESET;
        if(this.args != null) {
            for(int i = 0; i < this.args.length; i++) {
                String arg = "<arg" + i + ">";
                warningMsg = warningMsg.replace(arg,this.args[i].toString());
            }
        }
        return warningMsg;
    }

    public void setWarningType(MessageType warning) { this.warning = warning; }
}
