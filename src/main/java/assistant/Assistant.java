package assistant;

import modules.AssistantModule;
import modules.WestpacModule;

import java.io.FileNotFoundException;

public class Assistant {

    public static void main(String[] args) throws FileNotFoundException {
        AssistantModule.moduleRunner(new WestpacModule());
    }
}
