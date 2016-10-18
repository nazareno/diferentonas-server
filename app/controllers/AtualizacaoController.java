package controllers;


import static akka.pattern.Patterns.ask;
import static play.libs.Json.toJson;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Atualizacao;
import models.AtualizacaoDAO;
import models.Cidadao;
import models.CidadaoDAO;
import play.Configuration;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import scala.concurrent.duration.Duration;
import actors.AtualizadorActorProtocol;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;

@Singleton
@Security.Authenticated(AcessoAdmin.class)
public class AtualizacaoController extends Controller {

	private ActorRef atualizador;
	private AtualizacaoDAO daoAtualizacao;
	private CidadaoDAO daoCidadao;
	private ActorSystem system;
	private Cancellable atualizacaoAgendada;
	private WSRequest atualizacaoURL;
	private Pattern padraoDaDataDePublicacao;
	private JPAApi jpaAPI;
	private boolean atualizacaoAtivada;

	@Inject
	public AtualizacaoController(AtualizacaoDAO daoAtualizacao, CidadaoDAO daoCidadao,
			@Named("atualizador-actor") ActorRef atualizador, ActorSystem system, Configuration configuration, WSClient client, JPAApi jpaAPI) {
		this.daoAtualizacao = daoAtualizacao;
		this.daoCidadao = daoCidadao;
		this.atualizador = atualizador;
		this.system = system;
		this.jpaAPI = jpaAPI;
		this.atualizacaoAtivada = configuration.getBoolean("diferentonas.atualizacao", false);
		if(atualizacaoAtivada){
			this.atualizacaoAgendada = system.scheduler().schedule(Duration.create(10, TimeUnit.SECONDS), 
					Duration.create(1, TimeUnit.DAYS), () -> {
						this.atualiza();
					}, 
					system.dispatcher());
			this.atualizacaoURL = client.url(configuration.getString("diferentonas.url", "http://portal.convenios.gov.br/download-de-dados"));
			this.padraoDaDataDePublicacao = Pattern.compile("\\d\\d\\/\\d\\d\\/\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d");
		}
		
	}

	@Transactional
	public Result getStatus() {
		
		Cidadao cidadao = daoCidadao
				.find(UUID.fromString(request().username()));
		if (!cidadao.isFuncionario()) {
			return unauthorized("Cidadão não autorizado");
		}
		
		return ok(toJson(daoAtualizacao.find()));
	}

	public void atualiza() {
		
		if(atualizacaoAtivada){

			if(jpaAPI.withTransaction(()->daoAtualizacao.find().estaAtualizando())){
				this.atualizacaoAgendada.cancel();
				this.atualizacaoAgendada = system.scheduler().schedule(Duration.create(new Random().nextInt(12), TimeUnit.HOURS), 
						Duration.create(1, TimeUnit.DAYS), () -> {
							this.atualiza();
						}, 
						system.dispatcher());
				return;
			}

		}
		atualizacaoURL.get().thenAccept(response -> {
			Matcher matcher = padraoDaDataDePublicacao.matcher(response.getBody());
			if(matcher.find()){
				Atualizacao status = jpaAPI.withTransaction((em) -> {
					Atualizacao result = daoAtualizacao.find();
					String data = matcher.group(1);
					result.atualiza(Arrays.asList(data));
					em.persist(result);
					em.flush();
					em.refresh(result);
					return result;
				});

				if (status.estaDesatualizado()) {
					ask(atualizador, new AtualizadorActorProtocol.AtualizaIniciativasEScores(), 1000L);
				}
			}
		});

	}
}
