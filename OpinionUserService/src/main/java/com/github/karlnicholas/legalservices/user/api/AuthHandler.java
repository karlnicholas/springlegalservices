package com.github.karlnicholas.legalservices.user.api;

import com.github.karlnicholas.legalservices.user.dto.AuthResultDto;
import com.github.karlnicholas.legalservices.user.dto.UserLoginDto;
import com.github.karlnicholas.legalservices.user.security.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class AuthHandler {
    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

	public Mono<ServerResponse> handleLogin(ServerRequest serverRequest) {
		return ServerResponse.ok().body(authService.authenticate(serverRequest.bodyToMono(UserLoginDto.class)), AuthResultDto.class);
	}
}
