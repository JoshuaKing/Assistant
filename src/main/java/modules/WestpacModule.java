package modules;

import assistant.GeneralFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Created by Josh on 17/02/2016.
 */
public class WestpacModule extends AssistantModule {
    private final String DASHBOARD_URL = "https://banking.westpac.com.au/secure/banking/overview/dashboard";
    private final String LOGIN_URL = "https://online.westpac.com.au/esis/Login/SrvPage";
    private final String TRANSFER_URL = "https://banking.westpac.com.au/secure/banking/overview/payments/transfers";
    private final JBrowserDriver driver = GeneralFactory.createDefaultBrowser();
    private static final Logger LOGGER = LoggerFactory.getLogger(WestpacModule.class);

    public WestpacModule() {
        super(WestpacModule.class);
    }

    @Override
    public void run() {
        login();
        accounts();
        LOGGER.info("Ending at " + driver.getCurrentUrl());
        driver.quit();
    }

    private String getUsername() {
        try {
            String encrypted = configuration.get("username").asText();
            return GeneralFactory.getCipher().decrypt(encrypted);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return "";
        }
    }

    private String getPassword() {
        try {
            String encrypted = configuration.get("password").asText();
            return GeneralFactory.getCipher().decrypt(encrypted);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return "";
        }
    }

    private boolean login() {
        LOGGER.info("Logging in");
        driver.get(LOGIN_URL);
        disableAsync();
        driver.findElementById("username_temp").sendKeys(getUsername());
        for (int i = 0; i < getPassword().length(); i++) {
            char c = getPassword().toUpperCase().charAt(i);
            driver.findElementById("keypad_0_kp" + c).click();
        }
        driver.findElementById("btn-submit").click();
        LOGGER.info(driver.getCurrentUrl());
        return driver.getCurrentUrl().equals(DASHBOARD_URL);
    }

    private void disableAsync() {
        driver.executeScript("jQuery.ajaxSetup({async: false});");
    }

    private JsonNode executeJs(String filename, Object... args) {
        try {
            String js = getResource(filename);
            Object result = driver.executeScript(js, args);
            if (result == null) return null;
            return new ObjectMapper().readTree(result.toString());
        } catch (IOException e) {
           LOGGER.error(e.getMessage());
            return new ObjectMapper().createObjectNode();
        }
    }

    private void accounts() {
        disableAsync();
        try {
            ArrayNode accounts = (ArrayNode) executeJs("GetAccounts.js");
            IntStream.range(0, accounts.size()).forEach(i -> transferAccount(i, accounts));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void transferAccount(int i, ArrayNode accounts) {
        ObjectNode toAccount = (ObjectNode) accounts.get(i);
        JsonNode settingsElement = configuration.get("account-" + toAccount.get("hashcode").asInt());
        if (settingsElement == null) return;
        ObjectNode settings = (ObjectNode) settingsElement;
        LOGGER.info("Checking account " + toAccount.get("name").asText());
        if (toAccount.get("balance").asDouble() >= settings.get("min").asDouble()) return;

        ObjectNode fromAccount = toAccount;
        for (JsonNode account : accounts) {
            if (account.get("hashcode").asInt() == settings.get("from").asInt()) {
                fromAccount = (ObjectNode) account;
                break;
            }
        }
        LOGGER.info("Initiating transfer: $" + settings.get("topup").asDouble() + " from " + fromAccount.get("name").asText() + " to " + toAccount.get("name").asText());
        if (fromAccount == toAccount) return;

        driver.get(TRANSFER_URL);
        disableAsync();
        JsonNode result = executeJs("AccountTransfer.js", fromAccount.get("id").asInt(), toAccount.get("id").asInt(), settings.get("topup").asDouble(), settings.get("min").asDouble());
        LOGGER.info(result.toString());
    }
}
