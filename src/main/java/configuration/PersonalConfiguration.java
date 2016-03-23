package configuration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Josh on 23/03/2016.
 */
public class PersonalConfiguration {
    private final String work;
    private final String home;
    private final String name;
    private final String dob;

    private PersonalConfiguration(JsonNode personal) {
        work = personal.get("work").asText();
        home = personal.get("home").asText();
        name = personal.get("name").asText();
        dob = personal.get("dob").asText();
    }

    public static PersonalConfiguration from(JsonNode element) {
        return new PersonalConfiguration(element);
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
