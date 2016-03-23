package modules;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import log.Logger;

/**
 * Created by Josh on 17/02/2016.
 */
public class WestpacModule implements AssistantModule {

    private final String DASHBOARD_URL = "https://banking.westpac.com.au/secure/banking/overview/dashboard";
    private final String LOGIN_URL = "https://online.westpac.com.au/esis/Login/SrvPage";
    private final JBrowserDriver driver = new JBrowserDriver();

    @Override
    public void run(ModuleConfig moduleConfig) {
        login(driver);
        accounts(driver);
        Logger.alert(driver.getCurrentUrl());

        driver.quit();
    }

    private boolean login(JBrowserDriver driver) {
        driver.get(LOGIN_URL);
        driver.findElementById("username_temp").sendKeys("92481774");
        driver.findElementById("keypad_0_kpM").click();
        driver.findElementById("keypad_0_kp3").click();
        driver.findElementById("keypad_0_kpR").click();
        driver.findElementById("keypad_0_kpC").click();
        driver.findElementById("keypad_0_kpU").click();
        driver.findElementById("keypad_0_kpR").click();
        Object obfuscatedPassword = driver.executeScript("return $('#password_temp').val();");
        Logger.debug(obfuscatedPassword.toString());
        driver.findElementById("btn-submit").click();
        return driver.getCurrentUrl().equals(DASHBOARD_URL);
    }

    private void accounts(JBrowserDriver driver) {

    }
}
