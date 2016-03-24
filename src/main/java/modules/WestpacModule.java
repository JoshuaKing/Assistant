package modules;

import assistant.AwsFactory;
import assistant.GeneralFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by Josh on 17/02/2016.
 */
public class WestpacModule extends AssistantModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestpacModule.class);

    private final String LOG_DIR = "logs/" + moduleName + "/";
    private static final String DASHBOARD_URL = "https://banking.westpac.com.au/secure/banking/overview/dashboard";
    private static final String LOGIN_URL = "https://online.westpac.com.au/esis/Login/SrvPage";
    private static final String TRANSFER_URL = "https://banking.westpac.com.au/secure/banking/overview/payments/transfers";
    private final JBrowserDriver DRIVER = GeneralFactory.createDefaultBrowser();
    private static final String BUCKET = "networth.freshte.ch";

    public WestpacModule() {
        super(WestpacModule.class);
    }

    @Override
    public void run() {
        try {
            if (!login()) return;
            ArrayNode accounts = accounts();
            if (accounts == null) return;
            downloadFromS3(BUCKET, "networth.log", LOG_DIR);
            logNetworth(accounts);
            IntStream.range(0, accounts.size()).forEach(i -> transferAccount(i, accounts));
            LOGGER.info("Finished at " + DRIVER.getCurrentUrl());
            uploadToS3(BUCKET, LOG_DIR, "networth.log");
            uploadToS3(BUCKET, LOG_DIR, "networth.json");
            uploadToS3(BUCKET, getResourcePath(), "networth.html");
        } finally {
            LOGGER.info("Closing driver");
            DRIVER.quit();
        }
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
        DRIVER.get(LOGIN_URL);
        disableAsync();
        DRIVER.findElementById("username_temp").sendKeys(getUsername());
        for (int i = 0; i < getPassword().length(); i++) {
            char c = getPassword().toUpperCase().charAt(i);
            DRIVER.findElementById("keypad_0_kp" + c).click();
        }
        DRIVER.findElementById("btn-submit").click();
        while (DRIVER.getCurrentUrl().equals(LOGIN_URL) && !DRIVER.getTitle().contains("Sign in error")) {
            try {
                LOGGER.warn("Waiting...");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Just interruption
            }
        }
        if (DRIVER.getCurrentUrl().equals(DASHBOARD_URL)) {
            LOGGER.info("Logged In Successfully");
            return true;
        } else {
            LOGGER.error("Did not log in successfully: " + DRIVER.getCurrentUrl());
            return false;
        }
    }

    private void disableAsync() {
        DRIVER.executeScript("jQuery.ajaxSetup({async: false});");
    }

    private JsonNode executeJs(String filename, Object... args) {
        try {
            String js = getResource(filename);
            Object result = DRIVER.executeScript(js, args);
            if (result == null) return null;
            return new ObjectMapper().readTree(result.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return new ObjectMapper().createObjectNode();
        }
    }

    private ArrayNode accounts() {
        disableAsync();
        try {
            return (ArrayNode) executeJs("GetAccounts.js");
        } catch (Exception e) {
            LOGGER.error("Error getting account information: " + e.getClass().getSimpleName());
            return null;
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

        DRIVER.get(TRANSFER_URL);
        disableAsync();
        JsonNode result = executeJs("AccountTransfer.js", fromAccount.get("id").asInt(), toAccount.get("id").asInt(), settings.get("topup").asDouble(), settings.get("min").asDouble());
        LOGGER.info(result.toString());
    }

    private void logNetworth(ArrayNode accounts) {
        List<Double> balances = new ArrayList<>();
        for (JsonNode account : accounts) {
            configuration.get("networth").forEach(n -> {
                if (account.get("hashcode").asInt() == n.asInt()) {
                    LOGGER.info("Adding networth account " + account.get("name").asText());
                    balances.add(account.get("balance").asDouble());
                }
            });
        }

        double networth = balances.stream().mapToDouble(Double::new).sum();
        Instant now = Instant.now();
        String networthStr = String.format("%d %s %.2f\n", now.toEpochMilli(), DateTimeFormatter.ISO_INSTANT.format(now), networth);

        try {
            new File(LOG_DIR).mkdirs();
            LOGGER.info("Outputting logs to " + new File(LOG_DIR).getAbsolutePath());

            new File(LOG_DIR + "networth.log").createNewFile();
            FileWriter logWriter = new FileWriter(LOG_DIR + "networth.log", true);
            logWriter.write(networthStr);
            logWriter.close();
        } catch (IOException e) {
            LOGGER.error("Couldn't write to networth.log: " + e.getMessage());
        }

        LOGGER.info("Generating Google Chart of Networth");
        String data = "\n\t[ {'label': 'Time', 'type': 'datetime', 'id': 'time'}, {'label':'Networth','type':'number','id':'networth'} ]";


        try {
            List<String> logLines = IOUtils.readLines(new FileReader(LOG_DIR + "networth.log"));

            for (String line : logLines) {
                String[] sections = line.split(" ");
                if (sections.length < 3) break;
                data += ",\n\t[{v: new Date(" + sections[0] + "), f: '" + sections[1] + "'}, " + sections[2] + "]";
            }

            new File(LOG_DIR + "networth.json").createNewFile();
            FileWriter jsonWriter = new FileWriter(LOG_DIR + "networth.json");
            jsonWriter.write("var jsonData = [" + data + "\n];");
            jsonWriter.close();

            LOGGER.info("Generated.");
        } catch (IOException e) {
            LOGGER.error("Error in networth.log file operation: " + e.getMessage());
            return;
        }
    }

    private void uploadToS3(String bucket, String path, String filename) {
        try {
            AwsFactory awsFactory = GeneralFactory.getAwsFactory(WestpacModule.class);
            awsFactory.createS3Bucket(bucket);
            awsFactory.uploadToS3(bucket, filename, new File(path + filename));
            LOGGER.info("Successfully uploaded " + filename + " to S3 bucket " + bucket);
        } catch (Exception e) {
            LOGGER.error("Could not upload " + path + filename + " to S3 " + bucket + "@" + filename + ": " + e.getMessage());
        }
    }

    private void downloadFromS3(String bucket, String filename, String path) {
        try {
            AwsFactory awsFactory = GeneralFactory.getAwsFactory(WestpacModule.class);
            String content = awsFactory.downloadFromS3(bucket, filename);
            FileWriter writer = new FileWriter(path + filename);
            writer.write(content);
            writer.close();
            LOGGER.info("Successfully downloaded " + filename + " from S3 bucket " + bucket + " to " + path + filename);
        } catch (Exception e) {
            LOGGER.warn("Could not download " + bucket + ":" + filename + " from S3 to " + path + ": " + e.getMessage());
        }
    }
}
