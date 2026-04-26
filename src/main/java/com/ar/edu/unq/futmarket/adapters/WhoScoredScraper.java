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

    private static final String BASE_URL = "https://es.whoscored.com";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    @Scheduled(cron = "${futmarket.players.stats.sync.cron:0 0 2 * * *}")
    public void syncPlayerStats() {
        if (!enabled) return;
        syncPlayerStats(playerRepository.findAll());
    }

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

    @Async
    public void syncPlayerStatsAsync(Collection<Player> players) {
        syncPlayerStats(players);
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

        String playerPageUrl = BASE_URL + playerLinks.get(0).getAttribute("href");
        driver.get(playerPageUrl);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("top-player-stats-summary-grid")));
        } catch (TimeoutException e) {
            log.debug("Stats table not found for player: {}", player.getName());
            return;
        }

        WebElement firstRow;
        try {
            firstRow = driver.findElement(
                    By.cssSelector("#top-player-stats-summary-grid tbody tr:first-child"));
        } catch (NoSuchElementException e) {
            log.debug("No data rows in stats table for player: {}", player.getName());
            return;
        }

        player.setGoals(extractTdStat(firstRow, "goal"));
        player.setAssists(extractTdStat(firstRow, "assistTotal"));
        player.setShots(extractTdStat(firstRow, "shotsPerGame"));
        player.setKeyPasses(extractTdStat(firstRow, "passSuccess"));
        player.setRating(extractTdStat(firstRow, "rating"));

        playerRepository.save(player);
        log.info("Stats updated for player: {}", player.getName());
    }

    private double extractTdStat(WebElement row, String className) {
        try {
            WebElement td = row.findElement(By.cssSelector("td." + className));
            String text = td.getText().trim();
            return parseDouble(text);
        } catch (NoSuchElementException e) {
            return 0.0;
        }
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
        options.addArguments("user-agent=" + USER_AGENT);
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        return new ChromeDriver(options);
    }
}
