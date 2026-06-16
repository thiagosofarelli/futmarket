package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.controllers.response.ApiGeneralResponse;
import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.repositories.OrderRepository;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.QuoteRepository;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.impl.BootstrapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class BootstrapServiceImplTest {

    @Autowired
    private BootstrapServiceImpl bootstrapService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDbBeforeEach() {
        bootstrapService.removeAllData();
    }

    @Test
    void initializeDemoData_emptyDatabase_createsAllDemoEntities() {
        BootstrapResponse response = bootstrapService.initializeDemoData();

        assertThat(response).isNotNull();
        assertThat(response.superuserCreated()).isTrue();
        assertThat(response.usersCreated()).isEqualTo(3); // alice, bob, carla
        assertThat(response.playersCreated()).isEqualTo(4); // Lamine, Lautaro, Virgil, Thibaut
        assertThat(response.quotesCreated()).isEqualTo(4); // 4 quotes recalculated
        assertThat(response.ordersCreated()).isEqualTo(3); // 3 buy orders

        // Verify counts
        assertThat(userRepository.count()).isEqualTo(4); // 1 superuser + 3 demo users
        assertThat(playerRepository.count()).isEqualTo(4);
        assertThat(quoteRepository.count()).isEqualTo(4);
        assertThat(orderRepository.count()).isEqualTo(3);

        // Verify Superuser has positions for all players
        User superuser = userRepository.findBySuperuserTrue().orElse(null);
        assertThat(superuser).isNotNull();
        assertThat(superuser.getPortfolio().getPositions()).hasSize(4);
    }

    @Test
    void initializeDemoData_alreadyPopulatedDatabase_doesNotDuplicateData() {
        // Initial bootstrap
        bootstrapService.initializeDemoData();

        // Second bootstrap run
        BootstrapResponse secondResponse = bootstrapService.initializeDemoData();

        assertThat(secondResponse).isNotNull();
        assertThat(secondResponse.superuserCreated()).isFalse();
        assertThat(secondResponse.usersCreated()).isEqualTo(0);
        assertThat(secondResponse.playersCreated()).isEqualTo(0);
        assertThat(secondResponse.quotesCreated()).isEqualTo(0);
        assertThat(secondResponse.ordersCreated()).isEqualTo(0);

        // Database counts should remain the same
        assertThat(userRepository.count()).isEqualTo(4);
        assertThat(playerRepository.count()).isEqualTo(4);
        assertThat(quoteRepository.count()).isEqualTo(4);
        assertThat(orderRepository.count()).isEqualTo(3);
    }

    @Test
    void removeAllData_clearsRepositories() {
        // First populate
        bootstrapService.initializeDemoData();
        assertThat(userRepository.count()).isGreaterThan(0);
        assertThat(playerRepository.count()).isGreaterThan(0);

        // Now remove
        ApiGeneralResponse removeResponse = bootstrapService.removeAllData();
        assertThat(removeResponse).isNotNull();
        assertThat(removeResponse.message()).isEqualTo("All data removed");

        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(playerRepository.count()).isEqualTo(0);
        assertThat(quoteRepository.count()).isEqualTo(0);
        assertThat(orderRepository.count()).isEqualTo(0);
    }
}
