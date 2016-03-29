package assistant;

import com.amazonaws.services.lambda.runtime.Context;
import modules.AssistantModule;
import modules.WestpacModule;

import java.io.FileNotFoundException;

public class Assistant {

    public static void main(String[] args) throws FileNotFoundException {
        AssistantModule.moduleRunner(new WestpacModule());
    }

    public static void lambda(String input, Context context) {
        context.getLogger().log("Assistant Lambda Initiated v" + context.getFunctionVersion());
        AssistantModule.moduleRunner(new WestpacModule());
        context.getLogger().log("Assistant Lambda Finished.");
    }
}
