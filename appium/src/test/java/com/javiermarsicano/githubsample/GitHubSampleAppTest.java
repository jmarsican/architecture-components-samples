package com.javiermarsicano.githubsample;

import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.StartsActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.remote.Response;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.javiermarsicano.githubsample.TestResources.loadJsonResponse;
import static org.junit.Assert.*;

public class GitHubSampleAppTest extends BaseAndroidTest {

    private static final int TIMEOUT_SECONDS = 8;
    private static final String SELECTOR_BY_TEXT = "new UiScrollable(new UiSelector()).scrollIntoView(" +
            "new UiSelector().text(\"%s\"));";

    @Before
    public void setUp() {
        StartsActivity startsActivity = new StartsActivity() {
            @Override
            public Response execute(String driverCommand, Map<String, ?> parameters) {
                return driver.execute(driverCommand, parameters);
            }

            @Override
            public Response execute(String driverCommand) {
                return driver.execute(driverCommand);
            }
        };

        Activity activity = new Activity(APP_ID, ".MainActivity");
        startsActivity.startActivity(activity);
        driver.manage().timeouts().implicitlyWait(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        MockServer.INSTANCE.clearAll();
        driver.resetApp();
    }

    @Test
    public void firsScreenShowsSearchOption() {
        AndroidElement searchInput = driver.findElementById(APP_ID + ":id/input");

        assertTrue(searchInput.isDisplayed());
        assertEquals("search repositories", searchInput.getText());
    }

    @Test
    public void clickInSearchResults_ShowsCorrectRepositoryScreen() {
        //GIVEN
        MockServer.INSTANCE.mockSearchReposReturnsSuccessful(loadJsonResponse("repositories"));

        //WHEN
        AndroidElement searchInput = driver.findElementById(APP_ID + ":id/input");
        searchInput.click();
        searchInput.replaceValue("timber");
        driver.getKeyboard().sendKeys(Keys.ENTER);

        MobileElement secondElement = driver.findElementById(APP_ID + ":id/repo_list")
                .findElementsByClassName("android.widget.FrameLayout")
                .get(1)
                .findElementById(APP_ID + ":id/name");
        String secondElementText = secondElement.getText();
        secondElement.click();

        //THEN
        String repoName = driver.findElementById(APP_ID + ":id/name").getText();
        assertEquals(secondElementText, repoName);
    }

    @Test
    public void clickInSearchResults_ShowsErrorScreen() {
        MockServer.INSTANCE.mockSearchReposReturnsError();

        //WHEN
        AndroidElement searchInput = driver.findElementById(APP_ID + ":id/input");
        searchInput.click();
        searchInput.replaceValue("timber");
        driver.getKeyboard().sendKeys(Keys.ENTER);

        assertTrue(driver.findElement(By.id(APP_ID + ":id/retry"))
                .isDisplayed());
        AndroidElement errorTextView = driver.findElement(By.id(APP_ID + ":id/error_msg"));
        assertTrue(errorTextView.isDisplayed());
        assertFalse(errorTextView.getText().isEmpty());
    }


    @Test
    public void clickInContributor_ShowsAllHisRepos() {
        String contributorUserId = "JakeWharton";
        String contributorName = "Jake Wharton";
        MockServer.INSTANCE.mockSearchReposReturnsSuccessful(loadJsonResponse("repositories"));
        MockServer.INSTANCE.mockContributorsServiceReturnsSuccessful(loadJsonResponse("contributors"));
        MockServer.INSTANCE.mockUserReposServiceReturnsSuccessful(contributorUserId, loadJsonResponse("user-repos"));
        MockServer.INSTANCE.mockUsersServiceReturnsSuccessful(contributorUserId, loadJsonResponse("users"));

        AndroidElement searchInput = driver.findElementById(APP_ID + ":id/input");
        searchInput.click(); //open keyboard
        searchInput.replaceValue("timber"); //type text
        driver.getKeyboard().sendKeys(Keys.ENTER); //press search key

        driver.findElementById(APP_ID + ":id/repo_list")
                .findElementsByClassName("android.widget.FrameLayout")
                .get(1)
                .findElementById(APP_ID + ":id/name")
                .click();

        driver.findElementById(APP_ID + ":id/contributor_list")
                .findElement(MobileBy.AndroidUIAutomator(String.format(SELECTOR_BY_TEXT, contributorUserId)))
                .click();

        String userNameElementText = driver.findElementByAccessibilityId("user name").getText();
        assertEquals(contributorName, userNameElementText);
    }

    @Ignore
    @Test
    public void searchInListWithEspressoMatchers() {
        //TODO
    }

}
