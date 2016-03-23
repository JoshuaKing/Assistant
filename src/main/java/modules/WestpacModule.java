package modules;

import assistant.GeneralFactory;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;

/**
 * Created by Josh on 17/02/2016.
 */
public class WestpacModule extends AssistantModule {
    private final String DASHBOARD_URL = "https://banking.westpac.com.au/secure/banking/overview/dashboard";
    private final String LOGIN_URL = "https://online.westpac.com.au/esis/Login/SrvPage";
    private final JBrowserDriver driver = GeneralFactory.createDefaultBrowser();

    public WestpacModule() {
        super(WestpacModule.class);
    }

    @Override
    public void run() {
        login();
        accounts();
        logger.info("Ending at " + driver.getCurrentUrl());
        driver.quit();
    }

    private String getUsername() {
        try {
            String encrypted = configuration.getJsonConfiguration().get("username").getAsString();
            return GeneralFactory.getCipher().decrypt(encrypted);
        } catch (Exception e) {
            logger.error(e);
            return "";
        }
    }

    private String getPassword() {
        try {
            String encrypted = configuration.getJsonConfiguration().get("password").getAsString();
            return GeneralFactory.getCipher().decrypt(encrypted);
        } catch (Exception e) {
            logger.error(e);
            return "";
        }
    }

    private boolean login() {
        logger.debug("Logging in");
        driver.get(LOGIN_URL);
        disableAsync();
        driver.findElementById("username_temp").sendKeys(getUsername());
        for (int i = 0; i < getPassword().length(); i++) {
            char c = getPassword().toUpperCase().charAt(i);
            driver.findElementById("keypad_0_kp" + c).click();
        }
        driver.findElementById("btn-submit").click();
        logger.debug(driver.getCurrentUrl());
        return driver.getCurrentUrl().equals(DASHBOARD_URL);
    }

    private void disableAsync() {
        driver.executeScript("jQuery.ajaxSetup({async: false});");
    }

    private void accounts() {
        disableAsync();
        Object result = driver.executeScript("var accounts = [];\n" +
                "$('.account-tile').each(function(i, div) {\n" +
                    "div = $(div);\n" +
                    "var balanceHuman = div.find('.balance dd.CurrentBalance').clone().children().remove().end().text();\n" +
                    "var account = div.find('.account-info h2').text();\n" +
                    "var type = div.parent().attr('data-analytics-productgroupname');\n" +
                    "var id = div.find('.account-info p').clone().children().remove().end().text().trim();\n" +
                    "var hashcode = 0;\n" +
                    "var str = id.replace(/[ ]+/g, '-');\n" +
                    "for (i = 0; i < str.length; i++) {\n" +
                        "c = str.charCodeAt(i);\n" +
                        "hashcode = c + (hashcode << 6) + (hashcode << 16) - hashcode;\n" +
                    "}\n" +
                    "hashcode = Math.abs(hashcode % 53);\n" +
                    "var balance = Number(balanceHuman.replace(/[$, ]/g, ''));\n" +
                    "accounts.push({\n" +
                        "'name': account.trim(),\n" +
                        "'balanceHuman': balanceHuman.trim(),\n" +
                        "'balance': balance,\n" +
                        "'type': type.toLowerCase().trim(),\n" +
                        "'id': id.trim(),\n" +
                        "'hashcode': hashcode\n" +
                    "});\n" +
                "});\n" +
                "return JSON.stringify(accounts);\n");
        logger.debug(result != null ? result.toString() : "Null Result");
    }
}
