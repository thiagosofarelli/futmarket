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
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
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

        // --- DEFENSIVE STATS ---
        try {
            clickTab(driver, wait, "Defensive");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statistics-table-defensive")));
            WebElement defRow = getAverageRow(driver, "#statistics-table-defensive");

            if (defRow != null) {

                double tackles = extractTdStatByIndex(defRow, 3);
                double interceptions = extractTdStatByIndex(defRow, 4);

                player.setTackles(tackles);
                player.setInterceptions(interceptions);
            }
        } catch (Exception e) {
            log.debug("Defensive stats not found for player: {}", player.getName());
        }

        // --- OFFENSIVE STATS ---
        try {
            clickTab(driver, wait, "Offensive");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statistics-table-offensive")));
            WebElement offRow = getAverageRow(driver, "#statistics-table-offensive");

            if (offRow != null) {

                double goals = extractTdStatByIndex(offRow, 3);
                double assists = extractTdStatByIndex(offRow, 4);
                double shots = extractTdStatByIndex(offRow, 5);
                double keyPasses = extractTdStatByIndex(offRow, 6);
                double dribblings = extractTdStatByIndex(offRow, 7);
                double rating = extractTdStatByIndex(offRow, 12);

                player.setGoals(goals);
                player.setAssists(assists);
                player.setShots(shots);
                player.setKeyPasses(keyPasses);
                player.setDribbles(dribblings);
                player.setRating(rating);

            }
        } catch (Exception e) {
            log.debug("Offensive stats not found for player: {}", player.getName());
        }

        player.setLastStatsSync(LocalDateTime.now());
        playerRepository.save(player);
        log.info("Stats updated for player: {}", player.getName());
    }

    private void clickTab(WebDriver driver, WebDriverWait wait, String tabName) {
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), '" + tabName + "')]")));
        try {
            tab.click();
        } catch (ElementClickInterceptedException e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", tab);
        }
    }

    private WebElement getAverageRow(WebDriver driver, String containerSelector) {
        try {
            List<WebElement> rows = driver.findElements(
                    By.cssSelector(containerSelector + " #top-player-stats-summary-grid tbody tr"));

            return rows.stream()
                    .filter(row -> row.getText().contains("Total / Average"))
                    .findFirst()
                    .orElse(null);
        } catch (NoSuchElementException e) {
            return null;
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
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement acceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Aceptar todo']")
            ));

            try {
                acceptBtn.click();
                log.info("Banner de cookies cerrado (Clic normal).");
            } catch (ElementClickInterceptedException e) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", acceptBtn);
                log.info("Banner de cookies cerrado (Javascript).");
            }

            Thread.sleep(1000);

        } catch (TimeoutException e) {
            log.debug("No apareció el banner de cookies o ya estaba aceptado.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        // options.addArguments("--headless=new");
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