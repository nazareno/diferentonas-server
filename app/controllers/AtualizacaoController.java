package controllers;


import static akka.pattern.Patterns.ask;
import static play.libs.Json.toJson;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Atualizacao;
import models.AtualizacaoDAO;
import models.Cidadao;
import models.CidadaoDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import actors.AtualizadorActorProtocol;
import akka.actor.ActorRef;

@Singleton
@Security.Authenticated(AcessoAdmin.class)
public class AtualizacaoController extends Controller {

	private ActorRef atualizador;
	private AtualizacaoDAO daoAtualizacao;
	private CidadaoDAO daoCidadao;

	@Inject
	public AtualizacaoController(AtualizacaoDAO daoAtualizacao, CidadaoDAO daoCidadao,
			@Named("atualizador-actor") ActorRef atualizador) {
		this.daoAtualizacao = daoAtualizacao;
		this.daoCidadao = daoCidadao;
		this.atualizador = atualizador;
	}

	@Transactional
	public Result getAtualizacoes() {
		
		Cidadao cidadao = daoCidadao
				.find(UUID.fromString(request().username()));
		if (!cidadao.isFuncionario()) {
			return unauthorized("Cidadão não autorizado");
		}

		return ok(toJson(daoAtualizacao.verifica()));
	}

	@Transactional
	public Result aplica() {
		
		Cidadao cidadao = daoCidadao
				.find(UUID.fromString(request().username()));
		if (!cidadao.isFuncionario()) {
			return unauthorized("Cidadão não autorizado");
		}

		Atualizacao statusDaAtualizacao = daoAtualizacao.verifica();

		if (!statusDaAtualizacao.estaDesatualizado()) {
			return ok(toJson(statusDaAtualizacao));
		}

		ask(atualizador, new AtualizadorActorProtocol.AtualizaIniciativasEScores(), 1000L);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		return getAtualizacoes();
	}
}
