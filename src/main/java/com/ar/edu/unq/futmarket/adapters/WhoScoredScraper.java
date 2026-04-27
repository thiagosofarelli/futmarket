package com.ar.edu.unq.futmarket.adapters;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhoScoredScraper {

    @Value("${futmarket.whoscored.enabled:false}")
    private boolean enabled;

    @Value("${futmarket.whoscored.delay-millis:1500}")
    private long delayMillis;

    private final PlayerRepository playerRepository;

    private static final String BASE_URL = "https://www.whoscored.com";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    public void syncPlayerStats(Collection<Player> players) {
        if (!enabled) return;

        WebDriver driver = createDriver();
        try {
            dismissCookieBanner(driver);
            for (Player player : players) {
                try {
                    scrapeAndUpdate(driver, player);
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("Failed to sync stats for player {}: {}", player.getName(), e.getMessage());
                }
            }
        } finally {
            driver.quit();
        }
    }

    private void scrapeAndUpdate(WebDriver driver, Player player) {
        String searchUrl = BASE_URL + "/Search/?t=" + encodeURL(player.getName());
        driver.get(searchUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> playerLinks;
        try {
            playerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("table tbody tr td a[href*='/players/']")));
        } catch (TimeoutException e) {
            log.debug("No search results for player: {}", player.getName());
            return;
        }

        if (playerLinks.isEmpty()) return;

        String playerPageUrl = playerLinks.get(0).getAttribute("href");
        driver.get(playerPageUrl);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("top-player-stats-summary-grid")));
        } catch (TimeoutException e) {
            log.debug("Stats table not found for player: {}", player.getName());
            return;
        }

        WebElement totalRow;
        try {
            List<WebElement> rows = driver.findElements(
                    By.cssSelector("#top-player-stats-summary-grid tbody tr"));

            totalRow = rows.stream()
                    .filter(row -> row.getText().contains("Total / Average"))
                    .findFirst()
                    .orElse(null);

            if (totalRow == null) return;

        } catch (NoSuchElementException e) {
            log.debug("No data rows in stats table for player: {}", player.getName());
            return;
        }


        double goals = extractTdStatByIndex(totalRow, 3);      // Goles en columna 2
        double assists = extractTdStatByIndex(totalRow, 4);    // Asistencias en columna 3
        double rating = extractTdStatByIndex(totalRow, 11);    // Rating en columna 10

        player.setGoals(goals);
        player.setAssists(assists);
        player.setRating(rating);

        playerRepository.save(player);
        log.info("Stats updated for player: {}", player.getName());
    }

    private double extractTdStat(WebElement row, String className) {
        try {
            WebElement td = row.findElement(By.cssSelector("td[class*='" + className + "']"));
            return parseDouble(td.getText().trim());
        } catch (NoSuchElementException e) {
            return 0.0;
        }
    }

    private double extractTdStatByIndex(WebElement row, int index) {
        try {
            List<WebElement> tds = row.findElements(By.tagName("td"));
            if (index < tds.size()) {
                return parseDouble(tds.get(index).getText().trim());
            }
        } catch (Exception e) {
            // log or ignore
        }
        return 0.0;
    }

    private void dismissCookieBanner(WebDriver driver) {
        driver.get(BASE_URL);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button#accept-button, button[id*='accept'], button[class*='accept']")));
            acceptBtn.click();
        } catch (TimeoutException ignored) {
        }
    }

    private double parseDouble(String text) {
        if (text == null || text.isBlank()) return 0.0;
        try {
            return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String encodeURL(String text) {
        if (text == null) return "";
        return text.replace(" ", "%20")
                .replace("á", "%C3%A1").replace("é", "%C3%A9")
                .replace("í", "%C3%AD").replace("ó", "%C3%B3")
                .replace("ú", "%C3%FA").replace("ñ", "%C3%B1");
    }

    private WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("user-agent=" + USER_AGENT);
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        return new ChromeDriver(options);
    }
}
