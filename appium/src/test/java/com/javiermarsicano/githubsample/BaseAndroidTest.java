package com.javiermarsicano.githubsample;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

import static com.javiermarsicano.githubsample.TestResources.apiDemosApk;

public abstract class BaseAndroidTest {
    private static AppiumDriverLocalService service;
    protected static AppiumDriver<AndroidElement> driver;

    public static String APP_ID = "com.android.example.github";

    /**
     * initialization.
     */
    @BeforeClass
    public static void beforeClass() throws MalformedURLException {
//        service = AppiumDriverLocalService.buildDefaultService();
//        service.start();
//
//        if (service == null || !service.isRunning()) {
//            throw new AppiumServerHasNotBeenStartedLocallyException(
//                    "An appium server node is not started!");
//        }

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
        capabilities.setCapability("uiautomator2ServerInstallTimeout", 60000);

        capabilities.setCapability(MobileCapabilityType.APP, apiDemosApk().toAbsolutePath().toString());
        capabilities.setCapability("allowTestPackages", true);
        driver = new AppiumDriver<>(new URL("http://localhost:4723/wd/hub"), capabilities);
    }

    /**
     * finishing.
     */
    @AfterClass
    public static void afterClass() {
        if (driver != null) {
            driver.quit();
        }
        if (service != null) {
            service.stop();
        }
    }
}

