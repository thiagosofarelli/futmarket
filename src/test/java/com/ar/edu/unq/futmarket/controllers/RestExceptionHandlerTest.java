package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("bad arg");
    }

    @Test
    void handleMethodArgumentNotValid_returns400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        ResponseEntity<ApiError> response = handler.handleMethodArgumentNotValidException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlePositionNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handlePositionNotFound(new PositionNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleIllegalState_returns409() {
        ResponseEntity<ApiError> response = handler.handleIllegalState(new IllegalStateException("conflict"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("conflict");
    }

    @Test
    void handleUserNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleUserNotFound(new UserNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleInvalidBalance_returns400() {
        ResponseEntity<ApiError> response = handler.handleInvalidBalance(new InvalidBalanceException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleUsernameCannotBeBlank_returns400() {
        ResponseEntity<ApiError> response = handler.handleUsernameCannotBeBlank(new UsernameCannotBeBlankException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInvalidPurchasePrice_returns400() {
        ResponseEntity<ApiError> response = handler.handleInvalidPurchasePrice(new InvalidPurchasePriceException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlePlayerNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handlePlayerNotFound(new PlayerNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleInvalidTokenPrice_returns400() {
        ResponseEntity<ApiError> response = handler.handleInvalidTokenPrice(new InvalidTokenPriceException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInsufficientTokens_returns400() {
        ResponseEntity<ApiError> response = handler.handleInsufficientTokens(new InsufficientTokensException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlePortfolioNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handlePortfolioNotFound(new PortfolioNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleInvalidTokenQuantity_returns400() {
        ResponseEntity<ApiError> response = handler.handleInvalidTokenQuantity(new InvalidTokenQuantityException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleOrderNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleOrderNotFound(new OrderNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleGenericException_returns500() {
        ResponseEntity<ApiError> response = handler.handleGenericException(new RuntimeException("unexpected"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("unexpected");
    }

    @Test
    void handleInvalidCredentials_returns401() {
        ResponseEntity<ApiError> response = handler.handleInvalidCredentials(new InvalidCredentialsException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleSuperUserNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleSuperUserNotFound(new SuperUserNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleUsernameAlreadyExists_returns409() {
        ResponseEntity<ApiError> response = handler.handleUsernameAlreadyExists(new UsernameAlreadyExistsException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleSuperuserDoesntHaveThatPosition_returns409() {
        ResponseEntity<ApiError> response = handler.handleSuperuserDoesntHaveThatPosition(new SuperuserDoesntHaveThatPositionException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleMissingRequestBody_returns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ApiError> response = handler.handleMissingRequestBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleLeagueNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleLeagueNotFound(new LeagueNotFoundException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void apiError_hasTimestamp() {
        ResponseEntity<ApiError> response = handler.handleUserNotFound(new UserNotFoundException());
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void apiError_hasStatus() {
        ResponseEntity<ApiError> response = handler.handlePlayerNotFound(new PlayerNotFoundException());
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void apiError_hasError() {
        ResponseEntity<ApiError> response = handler.handleGenericException(new RuntimeException("err"));
        assertThat(response.getBody().getError()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
}
