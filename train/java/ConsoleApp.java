

import java.io.*;

public interface ConsoleApp {

    /**
     * do something that has been entered into the console
     * @param comm String to be parsed
     * @return true if successfull else false
     * @throws Exception which is caught somewhere
     */
    public boolean command(String comm) throws Exception;

    public void setLog(PrintWriter writer);

    public String getName();

}
