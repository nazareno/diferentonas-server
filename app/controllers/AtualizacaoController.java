package controllers;

import static akka.pattern.Patterns.ask;
import static play.libs.Json.toJson;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Atualizacao;
import models.AtualizacaoDAO;
import models.CidadaoDAO;
import play.Configuration;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import actors.AtualizadorActorProtocol;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Singleton
public class AtualizacaoController extends Controller {

	private ActorRef atualizador;
	private AtualizacaoDAO daoAtualizacao;
	private WSRequest atualizacaoURL;
	private Pattern padraoDaDataDePublicacao;
	private JPAApi jpaAPI;
	private boolean atualizacaoAtivada;
	private String identificadorUnicoDoServidor;
	private String dataVotada;

	@Inject
	public AtualizacaoController(AtualizacaoDAO daoAtualizacao, CidadaoDAO daoCidadao,
			@Named("atualizador-actor") ActorRef atualizador, ActorSystem system, Configuration configuration, WSClient client, JPAApi jpaAPI) {
		this.daoAtualizacao = daoAtualizacao;
		this.atualizador = atualizador;
		this.jpaAPI = jpaAPI;
		this.atualizacaoAtivada = configuration.getBoolean("diferentonas.atualizacao.automatica", false);
		
		if(atualizacaoAtivada){
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime manha = LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 6, 0);
			LocalDateTime noite = LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 18, 0);
			
			long delay;
			if(now.isBefore(manha)){
				delay = manha.atZone(ZoneOffset.systemDefault()).toEpochSecond() - now.atZone(ZoneOffset.systemDefault()).toEpochSecond();
			}else if(now.isBefore(noite)){
				delay = noite.atZone(ZoneOffset.systemDefault()).toEpochSecond() - now.atZone(ZoneOffset.systemDefault()).toEpochSecond();
			}else{
				delay = manha.plusDays(1).atZone(ZoneOffset.systemDefault()).toEpochSecond() - now.atZone(ZoneOffset.systemDefault()).toEpochSecond();
			}
			
			system.scheduler().schedule(Duration.create(delay, TimeUnit.SECONDS), 
					Duration.create(12, TimeUnit.HOURS), () -> {
						this.votaEmLider();
					}, 
					system.dispatcher());
			system.scheduler().schedule(Duration.create(delay + 3600, TimeUnit.SECONDS), 
					Duration.create(12, TimeUnit.HOURS), () -> {
						this.elegeLiderEAtualiza();
					}, 
					system.dispatcher());
			this.atualizacaoURL = client.url(configuration.getString("diferentonas.url", "http://portal.convenios.gov.br/download-de-dados"));
			this.padraoDaDataDePublicacao = Pattern.compile("\\d\\d\\/\\d\\d\\/\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d");
			this.identificadorUnicoDoServidor = UUID.randomUUID().toString();
			Logger.info("ID do servidor: " + this.identificadorUnicoDoServidor);
			
			if(configuration.getBoolean("diferentonas.demo.forcaatualizacao", false)){
				Logger.info("Iniciando votação extraordinária!");
				this.votaEmLider();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Logger.error("Dormiu durante a eleição!", e);
				}
				Logger.info("Elegendo lider para atualização de urgência!");
				this.elegeLiderEAtualiza();
			}
		}
	}


	private void votaEmLider() {
		atualizacaoURL.get().thenApply(response -> {
			Logger.info("Conexão realizada.");
			String body = response.getBody();
			Matcher matcher = padraoDaDataDePublicacao.matcher(body);
			if(matcher.find()){
				String data = matcher.group(0);
				this.dataVotada = data;
				Logger.info("Votando para " + data + " em " + identificadorUnicoDoServidor);
				jpaAPI.withTransaction(()->daoAtualizacao.vota(data, identificadorUnicoDoServidor));
			}else{
				Logger.info("Problemas ao acessar página em: " + atualizacaoURL.getUrl());
			}
			return ok();
		});
	}

	private void elegeLiderEAtualiza() {
		Atualizacao lider = jpaAPI.withTransaction(()->daoAtualizacao.getLider(dataVotada));
		if(lider == null){
			Logger.warn("Impossível eleger lider! Não houveram votos para data: " + dataVotada);
			return;
		}
		
		if(this.identificadorUnicoDoServidor.equals(lider.getServidorResponsavel())){
			Logger.info("Iniciando atualização de dados às: " + new Date() + " com dados publicados em: " + dataVotada);
			ask(atualizador, new AtualizadorActorProtocol.AtualizaIniciativasEScores(dataVotada, identificadorUnicoDoServidor), 1000L);
		}
	}

	@Transactional
	public Result getStatus() {
		return ok(toJson(daoAtualizacao.getMaisRecentes()));
	}
}
