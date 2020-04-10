import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class getGameDetailsAndTournamentCount {

    public static WebDriver driver;
    public static String resultPlaceholder = "<!-- INSERT_RESULTS -->";
    List<WebElement> allGame;
    List<String> gameName = new ArrayList<String>();
    List<String> tournamentCount = new ArrayList<String>();
    List<String> gameUrl = new ArrayList<String>();
    List<Integer> pageStatusCode = new ArrayList<Integer>();

    @BeforeSuite
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/chromedriver");
        driver = new ChromeDriver();
    }

    @Test(priority = 1)
    public void openGameTvWebsite() {
        //Open gameTv website
        driver.get("https://www.game.tv/");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test(priority = 2)
    public void scrollForAvailableGames() {
        executeJavaScript("document.querySelector('[class=\"available-games\"]').scrollIntoView()");
    }

    @Test(priority = 3)
    public void getTotalGamesItem() {
        allGame = driver.findElements(By.className("games-item"));
    }

    @Test(priority = 4)
    public void openGameDetailsPage() throws Exception {
        Actions action = new Actions(driver);
        for (int count = 1; count <= allGame.size(); ) {
            try {
                action.keyDown(Keys.CONTROL).build().perform();
                driver.findElement(By.xpath("(//li[@class='games-item'])[" + count + "]")).click();
                //switch to new tab
                switchTab(1);
                pageStatusCode.add(getPageStatus(new URL(driver.getCurrentUrl())));
                gameName.add(driver.findElement(By.xpath("//h1[@class='heading']")).getAttribute("innerHTML"));
                tournamentCount.add(driver.findElement(By.className("count-tournaments")).getAttribute("innerHTML"));
                gameUrl.add(driver.getCurrentUrl());
                driver.close();
                switchTab(0);
                count++;
            } catch (Exception e) {
                executeJavaScript("window.scrollBy(0,200)");
            }
        }
        writeResults();
    }

    public void writeResults() throws IOException {
        String reportIn = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/src/test/java/ListReportTemplate.html")));
        for (int i = 0; i < gameName.size(); i++) {
            reportIn = reportIn.replaceFirst(resultPlaceholder, "<tr><td align='center'>" + (i + 1) + "</td><td align='center'>" + gameName.get(i) + "</td><td align='center'>" + gameUrl.get(i) + "</td><td align='center'>" + pageStatusCode.get(i) + "</td><td align='center'>" + tournamentCount.get(i) + "</td></tr>\n" + resultPlaceholder);
        }
        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String reportPath = System.getProperty("user.dir") + "/gameList_" + currentDate + ".html";
        Files.write(Paths.get(reportPath), reportIn.getBytes(), StandardOpenOption.CREATE);
    }

    @AfterSuite
    public void tearDown() {
        driver.quit();
    }

    /************************************** Common Utils Functions **********************************************/

    public static void executeJavaScript(String script) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(script);
    }

    public static int getPageStatus(URL url) throws Exception {
        int response;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        response = connection.getResponseCode();
        connection.disconnect();
        return response;
    }

    public static void switchTab(int tabNum)
    {
        ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabNum));
    }
}
