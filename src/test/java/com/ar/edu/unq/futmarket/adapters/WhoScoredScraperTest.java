package com.ar.edu.unq.futmarket.adapters;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.mockito.ArgumentCaptor.forClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhoScoredScraperTest {

    @Mock
    private PlayerRepository playerRepository;

    private TestWebDriver driver;

    @Mock
    private WebDriverWait wait;

    private WhoScoredScraper scraper;

    @BeforeEach
    void setUp() {
        scraper = spy(new WhoScoredScraper(playerRepository));
        ReflectionTestUtils.setField(scraper, "delayMillis", 0L);
        driver = new TestWebDriver();
    }

    // -----------------------------------------------------------------------
    // guard: disabled
    // -----------------------------------------------------------------------

    @Test
    void syncPlayerStats_disabled_neverCreatesDriver() {
        ReflectionTestUtils.setField(scraper, "enabled", false);
        scraper.syncPlayerStats(List.of(player("Messi")));
        verify(scraper, never()).createDriver();
        verifyNoInteractions(playerRepository);
    }

    // -----------------------------------------------------------------------
    // colección vacía
    // -----------------------------------------------------------------------

    @Test
    void syncPlayerStats_emptyCollection_quitsDriverCleanly() {
        enableWithMocks();
        stubCookieBannerDismissal();

        scraper.syncPlayerStats(List.of());

        assertThat(driver.wasQuitCalled()).isTrue();
        verify(playerRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // sin resultados de búsqueda para el jugador
    // -----------------------------------------------------------------------

    @Test
    void syncPlayerStats_noSearchResults_skipsPlayer() {
        enableWithMocks();
        stubCookieBannerDismissal();
        when(wait.until(any())).thenThrow(new TimeoutException("no results"));

        Player unknown = player("Unknown Player");
        scraper.syncPlayerStats(List.of(unknown));

        var captor = forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Unknown Player");
        assertThat(captor.getValue().getLastStatsSync()).isNotNull();
    }

    @Test
    void syncPlayerStats_emptyPlayerLinks_skipsPlayer() {
        enableWithMocks();
        stubCookieBannerDismissal();
        when(wait.until(any())).thenReturn(List.of());

        Player unknown = player("Unknown Player");
        scraper.syncPlayerStats(List.of(unknown));

        var captor = forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Unknown Player");
        assertThat(captor.getValue().getLastStatsSync()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // scrape exitoso: actualiza stats y guarda
    // -----------------------------------------------------------------------

    @Test
    void syncPlayerStats_successfulScrape_updatesDefensiveAndOffensiveStats() {
        enableWithMocks();

        stubCookieBannerDismissal();

        WebElement playerLink = mock(WebElement.class);
        doReturn("https://www.whoscored.com/players/1").when(playerLink).getAttribute("href");

        WebElement defTab = mock(WebElement.class);
        WebElement offTab = mock(WebElement.class);

        // order: player links, defensive tab click,
        //        defensive table visible, offensive tab click, offensive table visible
        when(wait.until(any()))
                .thenReturn(List.of(playerLink))
                .thenReturn(defTab)
                .thenReturn(mock(WebElement.class))
                .thenReturn(offTab)
                .thenReturn(mock(WebElement.class));

        // defensive: índice 3=tackles(1.5), índice 4=interceptions(2.0)
        WebElement defRow = statRow(0.0, 0.0, 0.0, 1.5, 2.0);
        // offensive: índice 3=goals(10), 4=assists(5), 5=shots(3),
        //            6=keyPasses(2), 7=dribbles(1), 8-11=0, 12=rating(7.5)
        WebElement offRow = statRow(
                0.0, 0.0, 0.0, 10.0, 5.0, 3.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 7.5);
        driver.setFindElementsHandler(by -> {
            String selector = by.toString();
            if (selector.contains("statistics-table-defensive")) {
                return List.of(defRow);
            }
            if (selector.contains("statistics-table-offensive")) {
                return List.of(offRow);
            }
            return List.of();
        });

        Player messi = player("Messi");
        scraper.syncPlayerStats(List.of(messi));

        verify(playerRepository).save(messi);
        assertThat(messi.getTackles()).isEqualTo(1.5);
        assertThat(messi.getInterceptions()).isEqualTo(2.0);
        assertThat(messi.getGoals()).isEqualTo(10.0);
        assertThat(messi.getAssists()).isEqualTo(5.0);
        assertThat(messi.getShots()).isEqualTo(3.0);
        assertThat(messi.getKeyPasses()).isEqualTo(2.0);
        assertThat(messi.getDribbles()).isEqualTo(1.0);
        assertThat(messi.getRating()).isEqualTo(7.5);
    }

    // -----------------------------------------------------------------------
    // excepción en un jugador: continúa con el siguiente
    // -----------------------------------------------------------------------

    @Test
    void syncPlayerStats_exceptionInOnePlayer_continuesWithNext() {
        enableWithMocks();
        stubCookieBannerDismissal();
        when(wait.until(any()))
                .thenThrow(new WebDriverException("crash"))
                .thenThrow(new TimeoutException("no results for second player"));

        Player p1 = player("Player1");
        Player p2 = player("Player2");
        scraper.syncPlayerStats(List.of(p1, p2));

        var captor = forClass(Player.class);
        verify(playerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Player2");
    }

    // -----------------------------------------------------------------------
    // InterruptedException en sleep: propaga la interrupción
    // -----------------------------------------------------------------------

    @Test
    void syncPlayerStats_interruptedDuringSleep_setsInterruptFlag() throws Exception {
        enableWithMocks();
        stubCookieBannerDismissal();
        WebElement playerLink = mock(WebElement.class);
        doReturn("https://www.whoscored.com/players/1").when(playerLink).getAttribute("href");

        when(wait.until(any()))
                .thenReturn(List.of(playerLink))
                .thenReturn(mock(WebElement.class))
                .thenReturn(mock(WebElement.class))
                .thenReturn(mock(WebElement.class))
                .thenReturn(mock(WebElement.class));

        driver.setFindElementsHandler(by -> List.of());
        doThrow(new InterruptedException()).when(scraper).sleepBetweenPlayers();

        scraper.syncPlayerStats(List.of(player("Messi"), player("Ronaldo")));

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        assertThat(Thread.interrupted()).isTrue(); // limpiar flag para no afectar otros tests
    }

    // -----------------------------------------------------------------------
    // parseDouble
    // -----------------------------------------------------------------------

    @Test
    void parseDouble_validDecimal_returnsDouble() {
        assertThat(scraper.parseDouble("7.5")).isEqualTo(7.5);
    }

    @Test
    void parseDouble_integer_returnsDouble() {
        assertThat(scraper.parseDouble("10")).isEqualTo(10.0);
    }

    @Test
    void parseDouble_textWithNoise_stripsNonNumericChars() {
        assertThat(scraper.parseDouble("3.14xyz")).isEqualTo(3.14);
    }

    @Test
    void parseDouble_blank_returnsZero() {
        assertThat(scraper.parseDouble("")).isEqualTo(0.0);
        assertThat(scraper.parseDouble("   ")).isEqualTo(0.0);
    }

    @Test
    void parseDouble_null_returnsZero() {
        assertThat(scraper.parseDouble(null)).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // encodeURL
    // -----------------------------------------------------------------------

    @Test
    void encodeURL_spacesAndAccentedChars_areEncoded() {
        assertThat(scraper.encodeURL("ángel di maría"))
                .isEqualTo("%C3%A1ngel%20di%20mar%C3%ADa");
    }

    @Test
    void encodeURL_null_returnsEmpty() {
        assertThat(scraper.encodeURL(null)).isEmpty();
    }

    @Test
    void encodeURL_noSpecialChars_returnsUnchanged() {
        assertThat(scraper.encodeURL("Messi")).isEqualTo("Messi");
    }

    @Test
    void encodeURL_enie_isEncoded() {
        assertThat(scraper.encodeURL("España")).contains("%C3%B1");
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private void enableWithMocks() {
        ReflectionTestUtils.setField(scraper, "enabled", true);
        doReturn(driver).when(scraper).createDriver();
        lenient().doReturn(wait).when(scraper).createWait(driver);
    }

    private void stubCookieBannerDismissal() {
        WebElement acceptButton = mock(WebElement.class);
        when(acceptButton.isDisplayed()).thenReturn(true);
        when(acceptButton.isEnabled()).thenReturn(true);
        driver.setFindElementHandler(by -> acceptButton);
    }

    private Player player(String name) {
        return Player.builder()
                .name(name)
                .team("Team")
                .league("League")
                .playerPosition(PlayerPosition.FORWARD)
                .build();
    }

    private WebElement statRow(double... values) {
        WebElement row = mock(WebElement.class);
        lenient().when(row.getText()).thenReturn("Total / Average");
        List<WebElement> tds = new ArrayList<>();
        for (double val : values) {
            WebElement td = mock(WebElement.class);
            lenient().when(td.getText()).thenReturn(String.valueOf(val));
            tds.add(td);
        }
        lenient().when(row.findElements(By.tagName("td"))).thenReturn(tds);
        return row;
    }

    @SuppressWarnings("null")
    private static final class TestWebDriver implements WebDriver, JavascriptExecutor {

        private Function<By, WebElement> findElementHandler = by -> {
            throw new NoSuchElementException(by.toString());
        };

        private Function<By, List<WebElement>> findElementsHandler = by -> List.of();
        private boolean quitCalled;

        void setFindElementHandler(Function<By, WebElement> findElementHandler) {
            this.findElementHandler = findElementHandler;
        }

        void setFindElementsHandler(Function<By, List<WebElement>> findElementsHandler) {
            this.findElementsHandler = findElementsHandler;
        }

        @Override
        public void get(String url) {
            // no-op for unit tests
        }

        @Override
        public String getCurrentUrl() {
            return "";
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public List<WebElement> findElements(By by) {
            return findElementsHandler.apply(by);
        }

        @Override
        public WebElement findElement(By by) {
            return findElementHandler.apply(by);
        }

        @Override
        public String getPageSource() {
            return "";
        }

        @Override
        public void close() {
            // no-op
        }

        @Override
        public void quit() {
            quitCalled = true;
        }

        boolean wasQuitCalled() {
            return quitCalled;
        }

        @Override
        public Set<String> getWindowHandles() {
            return Set.of();
        }

        @Override
        public String getWindowHandle() {
            return "window";
        }

        @Override
        public TargetLocator switchTo() {
            return null;
        }

        @Override
        public Navigation navigate() {
            return null;
        }

        @Override
        public Options manage() {
            return null;
        }

        @Override
        public Object executeScript(String script, Object... args) {
            return null;
        }

        @Override
        public Object executeAsyncScript(String script, Object... args) {
            return null;
        }
    }
}
