package configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by Josh on 23/03/2016.
 */
public class PersonalConfiguration {
    private final String work;
    private final String home;
    private final String name;
    private final String dob;

    private PersonalConfiguration(JsonObject personal) {
        work = personal.get("work").getAsString();
        home = personal.get("home").getAsString();
        name = personal.get("name").getAsString();
        dob = personal.get("dob").getAsString();
    }

    public static PersonalConfiguration from(JsonElement element) {
        return new PersonalConfiguration(element.getAsJsonObject());
    }

    public String getDob() {
        return dob;
    }

    public String getName() {
        return name;
    }

    public String getHome() {
        return home;
    }

    public String getWork() {
        return work;
    }

}
