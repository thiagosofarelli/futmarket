package com.ar.edu.unq.futmarket.adapters;

import java.io.IOException;
import java.util.Collection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WhoScoredScraper {

    @Value("${futmarket.whoscored.enabled:false}")
    private boolean enabled;

    @Value("${futmarket.whoscored.delay-millis:1000}")
    private long delayMillis;

    private final PlayerRepository playerRepository;
    private static final String BASE_URL = "https://www.whoscored.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Scheduled(cron = "${futmarket.players.stats.sync.cron:0 0 2 * * *}")
    public void syncPlayerStats() {
        if (!enabled) {
            return;
        }

        syncPlayerStats(playerRepository.findAll());
    }

    public void syncPlayerStats(Collection<Player> players) {
        if (!enabled) {
            return;
        }

        for (Player player : players) {
            try {
                syncPlayerStats(player);
                sleep(delayMillis);
            } catch (IOException | InterruptedException ex) {
                // Continue with next player on error
            }
        }
    }

    @Async
    public void syncPlayerStatsAsync(Collection<Player> players) {
        syncPlayerStats(players);
    }

    private void syncPlayerStats(Player player) throws IOException, InterruptedException {
        String searchUrl = BASE_URL + "/Search/SearchPlayerStat?term=" + encodeURL(player.getName());
        
        Connection connection = Jsoup.connect(searchUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true);
        
        Document doc = connection.get();
        
        if (doc == null) {
            return;
        }

        // Try to find player in search results
        Elements searchResults = doc.select("div.search-results a[href*=/Players/]");
        if (searchResults.isEmpty()) {
            return;
        }

        // Get first result that matches player's team
        for (Element result : searchResults) {
            String href = result.attr("href");
            if (href.isEmpty()) {
                continue;
            }

            try {
                String playerPageUrl = BASE_URL + href;
                extractAndUpdateStats(player, playerPageUrl);
                return;
            } catch (IOException ex) {
                // Try next result
            }
        }
    }

    private void extractAndUpdateStats(Player player, String playerPageUrl) throws IOException {
        Connection connection = Jsoup.connect(playerPageUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true);

        Document doc = connection.get();
        if (doc == null) {
            return;
        }

        // Extract stats from WhoScored player page
        // Stats are typically in tables with specific structure
        double goals = extractStatValue(doc, "Goals");
        double assists = extractStatValue(doc, "Assists");
        double shots = extractStatValue(doc, "Shots");
        double keyPasses = extractStatValue(doc, "Key Passes");
        double dribbles = extractStatValue(doc, "Dribbles");
        double tackles = extractStatValue(doc, "Tackles");
        double interceptions = extractStatValue(doc, "Interceptions");
        double rating = extractRating(doc);

        player.setGoals(goals);
        player.setAssists(assists);
        player.setShots(shots);
        player.setKeyPasses(keyPasses);
        player.setDribbles(dribbles);
        player.setTackles(tackles);
        player.setInterceptions(interceptions);
        player.setRating(rating);

        playerRepository.save(player);
    }

    private double extractStatValue(Document doc, String statName) {
        // Look for stat rows in tables
        Elements rows = doc.select("table tbody tr");
        
        for (Element row : rows) {
            String rowText = row.text();
            if (rowText.contains(statName)) {
                Elements cells = row.select("td");
                if (cells.size() >= 2) {
                    try {
                        String value = cells.get(cells.size() - 1).text().trim();
                        return Double.parseDouble(value);
                    } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                        // Return 0 if parsing fails
                    }
                }
            }
        }

        // Alternative: look for stat divs
        Elements statElements = doc.select("div.playerStats span:contains(" + statName + ")");
        for (Element elem : statElements) {
            String nextText = elem.nextElementSibling() != null ? 
                    elem.nextElementSibling().text() : "";
            try {
                return Double.parseDouble(nextText.trim());
            } catch (NumberFormatException ex) {
                // Continue
            }
        }

        return 0.0;
    }

    private double extractRating(Document doc) {
        // Try to find player rating/score on page
        Elements ratingElements = doc.select("div.playerRating, span.rating");
        
        for (Element elem : ratingElements) {
            try {
                String ratingText = elem.text().replaceAll("[^0-9.]", "");
                return Double.parseDouble(ratingText);
            } catch (NumberFormatException ex) {
                // Continue
            }
        }

        return 0.0;
    }

    private String encodeURL(String text) {
        return text.replace(" ", "%20").replace("á", "%C3%A1").replace("é", "%C3%A9")
                .replace("í", "%C3%AD").replace("ó", "%C3%B3").replace("ú", "%C3%BA")
                .replace("ñ", "%C3%B1");
    }

    private void sleep(long millis) throws InterruptedException {
        if (millis > 0) {
            Thread.sleep(millis);
        }
    }
}
