package assistant;

import com.amazonaws.services.lambda.runtime.Context;
import modules.AssistantModule;
import modules.WestpacModule;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class Assistant {

    public static void main(String[] args) throws FileNotFoundException {
        AssistantModule.moduleRunner(new WestpacModule());
    }

    public static void lambda(InputStream inputStream, OutputStream outputStream, Context context) {
        StringWriter stringWriter = new StringWriter();
        try {
            IOUtils.copy(inputStream, stringWriter, "UTF-8");
            context.getLogger().log("Assistant Lambda Initiated: " + stringWriter.toString());
            AssistantModule.moduleRunner(new WestpacModule());
            context.getLogger().log("Assistant Lambda Finished.");
            outputStream.write("Complete.".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
