package modules;

import assistant.AwsFactory;
import assistant.GeneralFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jaunt.JNode;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;
import com.jaunt.component.Form;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Josh on 17/02/2016.
 */
public class WestpacModule extends AssistantModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestpacModule.class);
    private static final String BANK_BASE_URL = "https://banking.westpac.com.au";
    private static final String DASHBOARD_PATH = "/secure/banking/overview/dashboard";
    private static final String COMMUNICATIONS_PATH = "/eam/servlet/getEamInterfaceData";
    private static final String TRANSFERS_PATH = "/secure/banking/overview/payments/transfers";
    private static final String BUCKET = "networth.freshte.ch";

    private final UserAgent DRIVER = GeneralFactory.createDefaultBrowser();
    private final String LOG_DIR = "/tmp/assistant/logs/" + moduleName + "/";

    public WestpacModule() {
        super(WestpacModule.class);
    }

    @Override
    public void run() {
        try {
            if (!login()) return;
            List<Account> accounts = accounts();
            if (accounts == null) return;
            downloadFromS3(BUCKET, "networth.log", LOG_DIR);
            logNetworth(accounts);
            accounts.forEach(a -> transferAccount(a, accounts));
            LOGGER.info("Finished at " + DRIVER.getLocation());
            uploadToS3(BUCKET, LOG_DIR, "networth.log");
            uploadToS3(BUCKET, LOG_DIR, "networth.json");
            uploadToS3(BUCKET, getResourcePath(), "networth.html");
        } finally {
            LOGGER.info("Closing driver");
            try {
                DRIVER.cookies.empty();
                DRIVER.close();
            } catch (IOException e) {
                LOGGER.warn("Could not close driver properly: " + e.getMessage());
            }
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

    private String obfuscate(String text, Map<Character, Integer> conversionMap, String malgm) {
        String obfuscated = "";
        for (int i = 0; i < text.length(); i++) {
            int index = conversionMap.get(text.toUpperCase().charAt(i));
            obfuscated += malgm.charAt(index);
        }
        return obfuscated;
    }

    private boolean login() {
        LOGGER.info("Logging in");
        try {
            DRIVER.sendGET(BANK_BASE_URL + COMMUNICATIONS_PATH);
            String malgm = DRIVER.json.get("keymap").get("malgm").toString();
            String halgm = DRIVER.json.get("keymap").get("halgm").toString();
            Map<Character, Integer> conversionMap = new HashMap<>();
            for(JNode keymap : DRIVER.json.get("keymap").get("keys")) {
                JNode node = keymap.iterator().next();
                conversionMap.put(node.getName().charAt(0), node.toInt());
            }
            String obfuscated = obfuscate(getPassword(), conversionMap, malgm);
            String url = DRIVER.json.get("operations").get(1).get("submitToUri").toString().replaceAll("\\\\/", "/");
            DRIVER.sendPOST(BANK_BASE_URL + url, "username=" + getUsername() + "&brand=WPAC&password=" + obfuscated + "&halgm=" + URLEncoder.encode(halgm, "UTF-8"));
            if (!DRIVER.getLocation().startsWith(BANK_BASE_URL + DASHBOARD_PATH)) return false;
            LOGGER.info("Logged In Successfully");
            return true;
        } catch (Exception e) {
            LOGGER.error("Could not log in: " + e.getMessage());
            return false;
        }
    }

    private List<Account> accounts() {
        try {
            Document document = Jsoup.parse(DRIVER.getSource());
            List<Account> accounts = new ArrayList<>();
            for (Element e : document.select(".account-tile")) {
                e.select(".balance dd.CurrentBalance").first().children().remove();
                e.select(".account-info p").first().children().remove();

                Account account = new Account();
                account.name = e.select(".account-info h2").text().trim();
                account.type = e.parent().attr("data-analytics-productgroupname").trim();
                account.id = e.select(".account-info p").html().trim();
                account.humanBalance = String.valueOf(e.select(".balance dd.CurrentBalance").text().trim());
                account.balance = Double.valueOf(account.humanBalance.replaceAll("[$, ]", ""));

                String hashString = account.id.replaceAll("[ ]+", "-");
                int hashcode = 0;
                for (int i = 0; i < hashString.length(); i++) {
                    char c = hashString.charAt(i);
                    hashcode = c + (hashcode << 6) + (hashcode << 16) - hashcode;
                }
                account.hashcode = Math.abs(hashcode % 53);
                accounts.add(account);
                //LOGGER.info("Account " + new ObjectMapper().writeValueAsString(account));
            }
            return accounts;
        } catch (Exception e) {
            LOGGER.error("Error getting account information: " + e.getClass().getSimpleName());
            return null;
        }
    }

    private void transferAccount(Account toAccount, List<Account> accounts) {
        JsonNode settingsElement = configuration.get("account-" + toAccount.hashcode);
        if (settingsElement == null) return;
        ObjectNode settings = (ObjectNode) settingsElement;
        LOGGER.info("Checking account " + toAccount.name);
        if (toAccount.balance >= settings.get("min").asDouble()) return;

        Account findAccount = toAccount;
        for (Account account : accounts) {
            if (account.hashcode == settings.get("from").asInt()) {
                findAccount = account;
                break;
            }
        }
        final Account fromAccount = findAccount;
        if (fromAccount == toAccount) return;
        LOGGER.info("Initiating transfer: $" + settings.get("topup").asDouble() + " from " + fromAccount.name + " to " + toAccount.name);

        try {
            DRIVER.sendGET(BANK_BASE_URL + TRANSFERS_PATH);
        } catch (ResponseException e) {
            LOGGER.error("Could not access transfer URL: " + e.getMessage());
        }

        try {
            Document document = Jsoup.parse(DRIVER.getSource());
            Element fromAccountElement = document.select("#Form_FromAccountGuid option").stream().filter(e -> e.html().contains(fromAccount.id)).findFirst().get();
            Element toAccountElement = document.select("#Form_FromAccountGuid option").stream().filter(e -> e.html().contains(fromAccount.id)).findFirst().get();
            Form form = DRIVER.doc.getForm(0);
            form.setSelect("Form.FromAccountGuid", fromAccountElement.val());
            form.setSelect("Form.ToAccountGuid", toAccountElement.val());
            form.set("Form.FromDescription", "AutoTopup <$" + settings.get("topup").asText());
            form.setCheckBox("SameAsFromAccount", true);
            form.set("Form.Amount", settings.get("topup").asText());
            form.submit();
            if (DRIVER.json.get("isSuccessful").toBoolean()) {
                LOGGER.info("Transferred successfully");
            } else {
                LOGGER.error("Could not transfer: " + DRIVER.json);
            }
        } catch (NotFound e) {
            LOGGER.error("Could not find element: " + e.getMessage());
        } catch (ResponseException e) {
            LOGGER.error("Could not complete transfer: " + e.getMessage());
        }
    }

    private void logNetworth(List<Account> accounts) {
        List<Integer> networthList = new ArrayList<>();
        configuration.get("networth").forEach(n -> networthList.add(n.asInt()));
        double networth = accounts.stream().filter(a -> networthList.contains(a.hashcode)).mapToDouble(a -> a.balance).sum();
        Instant now = Instant.now();
        String networthStr = String.format("%d %s %.2f\n", now.toEpochMilli(), DateTimeFormatter.ISO_INSTANT.format(now), networth);

        try {
            new File(LOG_DIR).mkdirs();
            LOGGER.info("Outputting logs to " + new File(LOG_DIR).getAbsolutePath());

            new File(LOG_DIR + "networth.log").createNewFile();
            FileWriter logWriter = new FileWriter(LOG_DIR + "networth.log", true);
            logWriter.write(networthStr);
            logWriter.close();
            LOGGER.info("Networth: $" + String.format("%.2f", networth));
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
            new File(path).mkdirs();
            FileWriter writer = new FileWriter(path + filename);
            writer.write(content);
            writer.close();
            LOGGER.info("Successfully downloaded " + filename + " from S3 bucket " + bucket + " to " + path + filename);
        } catch (Exception e) {
            LOGGER.warn("Could not download " + bucket + ":" + filename + " from S3 to " + path + ": " + e.getMessage());
        }
    }

    private class Account {
        public String name;
        public String type;
        public String humanBalance;
        public double balance;
        public int hashcode;
        public String id;
    }
}
