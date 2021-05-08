package com.github.karlnicholas.legalservices.opinionrestca;

import java.sql.SQLException;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.github.karlnicholas.legalservices.caselist.model.CaseListEntry;
import com.github.karlnicholas.legalservices.opinion.model.OpinionBase;
import com.github.karlnicholas.legalservices.opinion.model.OpinionKey;
import reactor.core.publisher.Mono;

@Component
public class OpinionsServiceHandler {
	private ParameterizedTypeReference<List<OpinionBase>> opinionBaseType;
	private ParameterizedTypeReference<List<OpinionKey>> opinionKeysType;
	private ParameterizedTypeReference<List<CaseListEntry>> caseListEntriesType;
	private final OpinionBaseDao opinionBaseDao;

	public OpinionsServiceHandler(OpinionBaseDao opinionBaseDao) {
		this.opinionBaseDao = opinionBaseDao;
		this.opinionKeysType = new ParameterizedTypeReference<List<OpinionKey>>() {};
		this.opinionBaseType = new ParameterizedTypeReference<List<OpinionBase>>() {};
	}

	public Mono<ServerResponse> getOpinionsWithStatuteCitations(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
				.body(request.bodyToMono(opinionKeysType).map(opinionKeys -> {
					try {
						return opinionBaseDao.getOpinionsWithStatuteCitations(opinionKeys);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}), opinionBaseType);
	}

	public Mono<ServerResponse> getSlipOpinionUpdateNeeded(ServerRequest request) {
		try {
			return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).bodyValue(opinionBaseDao.callSlipOpinionUpdateNeeded());
		} catch (SQLException e) {
			return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getLocalizedMessage());
		}
	}

	public Mono<ServerResponse> caseListEntries(ServerRequest request) {
		try {
			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(opinionBaseDao.caseListEntries());
		} catch (SQLException e) {
			return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getLocalizedMessage());
		}
	}

	public Mono<ServerResponse> caseListEntryUpdates(ServerRequest request) {
		return request.bodyToMono(caseListEntriesType).flatMap(caseListEntries -> {
			try {
				opinionBaseDao.caseListEntryUpdates(caseListEntries);
				return ServerResponse.ok().build();
			} catch (SQLException e) {
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getLocalizedMessage());
			}
		});
	}

	public Mono<ServerResponse> caseListEntryUpdate(ServerRequest request) {
		return request.bodyToMono(CaseListEntry.class).flatMap(caseListEntry -> {
			try {
				opinionBaseDao.caseListEntryUpdate(caseListEntry);
				return ServerResponse.ok().build();
			} catch (SQLException e) {
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(e.getLocalizedMessage());
			}
		});
	}

}
