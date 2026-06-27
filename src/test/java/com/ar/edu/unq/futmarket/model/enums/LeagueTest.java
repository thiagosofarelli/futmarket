package com.ar.edu.unq.futmarket.model.enums;

import com.ar.edu.unq.futmarket.exception.LeagueNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeagueTest {

    @Test
    void getFullNameByCode_pl_returnsPremierLeague() {
        assertThat(League.getFullNameByCode("PL")).isEqualTo("Premier League");
    }

    @Test
    void getFullNameByCode_pd_returnsLaLiga() {
        assertThat(League.getFullNameByCode("PD")).isEqualTo("La Liga");
    }

    @Test
    void getFullNameByCode_fl1_returnsLigue1() {
        assertThat(League.getFullNameByCode("FL1")).isEqualTo("Ligue 1");
    }

    @Test
    void getFullNameByCode_bl1_returnsBundesliga() {
        assertThat(League.getFullNameByCode("BL1")).isEqualTo("Bundesliga");
    }

    @Test
    void getFullNameByCode_sa_returnsSerieA() {
        assertThat(League.getFullNameByCode("SA")).isEqualTo("Serie A");
    }

    @Test
    void getFullNameByCode_lowercase_isCaseInsensitive() {
        assertThat(League.getFullNameByCode("pl")).isEqualTo("Premier League");
    }

    @Test
    void getFullNameByCode_mixedCase_isCaseInsensitive() {
        assertThat(League.getFullNameByCode("Pl")).isEqualTo("Premier League");
    }

    @Test
    void getFullNameByCode_invalidCode_throwsLeagueNotFoundException() {
        assertThatThrownBy(() -> League.getFullNameByCode("INVALID"))
                .isInstanceOf(LeagueNotFoundException.class);
    }

    @Test
    void getFullNameByCode_null_throwsLeagueNotFoundException() {
        assertThatThrownBy(() -> League.getFullNameByCode(null))
                .isInstanceOf(LeagueNotFoundException.class);
    }

    @Test
    void getFullName_returnsCorrectValue() {
        assertThat(League.PL.getFullName()).isEqualTo("Premier League");
        assertThat(League.PD.getFullName()).isEqualTo("La Liga");
        assertThat(League.FL1.getFullName()).isEqualTo("Ligue 1");
        assertThat(League.BL1.getFullName()).isEqualTo("Bundesliga");
        assertThat(League.SA.getFullName()).isEqualTo("Serie A");
    }

    @Test
    void allLeagues_haveNonBlankFullName() {
        for (League league : League.values()) {
            assertThat(league.getFullName()).isNotBlank();
        }
    }
}
