package actors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import models.Atualizacao;
import models.AtualizacaoDAO;
import models.Cidade;
import models.CidadeDAO;
import models.Iniciativa;
import models.IniciativaDAO;
import models.Score;

import org.h2.tools.Csv;

import play.Configuration;
import play.Logger;
import play.db.jpa.JPAApi;
import util.DadosUtil;
import actors.AtualizadorActorProtocol.AtualizaIniciativasEScores;
import akka.actor.UntypedActor;

public class AtualizadorActor extends UntypedActor {
	
	@Inject
	private JPAApi jpaAPI;
	
	@Inject
	private AtualizacaoDAO daoAtualizacao;
	
	@Inject
	private Configuration configuration;
	
	@Inject
	private CidadeDAO cidadeDAO;
	
	@Inject
	private IniciativaDAO iniciativaDAO;

	private SimpleDateFormat formatoDataAtualizacao = new SimpleDateFormat("yyyyMMdd");
	
	public void onReceive(Object msg) throws Exception {

		Logger.info("AtualizadorActor.onReceive()");
		if (msg instanceof AtualizadorActorProtocol.AtualizaIniciativasEScores) {
			Logger.info("mensagem");
			AtualizaIniciativasEScores mensagem = (AtualizadorActorProtocol.AtualizaIniciativasEScores)msg;
			
			String dataVotada = mensagem.getDataVotada();
			String servidorResponsavel = mensagem.getIdentificadorUnicoDoServidor();
			
			String[] dataVotadaSplit = dataVotada.split(" +")[0].split("/");
			String dataVotadaFormatada = dataVotadaSplit[2]+dataVotadaSplit[1]+dataVotadaSplit[0];
			
			
			Atualizacao maisRecente = jpaAPI.withTransaction(() -> {
				daoAtualizacao.inicia(dataVotada, servidorResponsavel);
				return daoAtualizacao.getUltimaRealizada();
			});
			
			if (maisRecente != null && maisRecente.equals(dataVotada)) {
				Logger.info("Dados já atualizados para data: " + dataVotada);
				jpaAPI.withTransaction(() -> {
					daoAtualizacao.finaliza(dataVotada, servidorResponsavel,
							false);
				});
				sender().tell(false, self());
				return;
			}
			
			String dataDisponivel = preparaDados(maisRecente == null?"":maisRecente.getDataDePublicacao(), dataVotadaFormatada);
			
			if(dataDisponivel == null){
				Logger.info("Não existem dados para atualização.");
				jpaAPI.withTransaction(() -> {
					daoAtualizacao.finaliza(dataVotada, servidorResponsavel, false);
				});
				sender().tell(false, self());
				return;
			}
			
			Logger.info("Tentando atualizar com dados de: " + dataVotada);
			
			boolean primeiraExecucao = maisRecente == null;

			jpaAPI.withTransaction(() -> {
				try {
					Date proximaData = formatoDataAtualizacao.parse(dataDisponivel);

					String scoresDataPath = Paths.get(daoAtualizacao.getFolder()).toAbsolutePath().toString() + "/diferentices-" + dataDisponivel + ".csv";
					Logger.debug("Arquivo de scores: " + scoresDataPath);
					atualizaScores(scoresDataPath, proximaData, primeiraExecucao);

					String iniciativasDataPath = Paths.get(daoAtualizacao.getFolder()).toAbsolutePath().toString() + "/iniciativas-" + dataDisponivel + ".csv";
					Logger.debug("Arquivo de inciativas: " + iniciativasDataPath);
					atualizaIniciativas(iniciativasDataPath, proximaData, primeiraExecucao);
					jpaAPI.withTransaction(() -> daoAtualizacao.finaliza(dataVotada, servidorResponsavel, false));
					sender().tell(true, self());
					Logger.info("Atualização concluída");
				} catch (Exception e) {
					Logger.error("Atualização com dados de " + dataVotada + " não terminou com sucesso.", e);
					jpaAPI.withTransaction(() -> daoAtualizacao.finaliza(dataVotada, servidorResponsavel, false));
					sender().tell(true, self());
				}
			});
		} else {
			Logger.warn("Mensagem de tipo desconhecido: " + msg.getClass().toString());
		}
	}
	
	private String preparaDados(String ultimaDataAtualizada, String data){
		String comando = configuration.getString("diferentonas.atualizacao.comando");
		String dados = null;
		try{
			if(!comando.isEmpty()){
				Logger.info("Preparando dados para atualização com: " + comando);
				ProcessBuilder builder = new ProcessBuilder(comando.split(" +"));
				File saidaDeErro = new File("/tmp/diferentonas_" + data + ".err");
				Logger.info("Acompanhe a execução em: /tmp/diferentonas_" + data + ".out e " + "/tmp/diferentonas_" + data + ".err");
				saidaDeErro.createNewFile();
				builder.redirectError(saidaDeErro);
				File saidaPadrao = new File("/tmp/diferentonas_" + data + ".out");
				saidaPadrao.createNewFile();
				builder.redirectOutput(saidaPadrao);
				Process process = builder.start();
				if(process.waitFor(2, TimeUnit.HOURS)){
					dados = DadosUtil.listaAtualizacoes(daoAtualizacao.getFolder(), data);
				}
				if(process.exitValue() != 0){
					Logger.error("Erro durante a execução do comando de atualização. Finalizado com: " + process.exitValue());
					Logger.error("Verifique o que aconteceu em /tmp/diferentonas_" + data + ".err");
				}
			}else{
				Logger.info("Não foi definido um comando para atualização. Verifique a propriedade diferentonas.atualizacao.comando");
			}
		}catch(IOException | InterruptedException e){
			Logger.error("Erro durante a execução do comando de atualização: " + comando, e);
		}
		return dados;
	}

	private void atualizaScores(String dataPath, Date dataDaAtualizacao, boolean primeiraExecucao) throws SQLException {
    	
    	int count = 0;

    	final ResultSet scoreResultSet = new Csv().read(dataPath, null, "utf-8");
    	count = 0;
    	while (scoreResultSet.next()) {
    		long originID = scoreResultSet.getLong(1);
    		Cidade cidade = cidadeDAO.find(originID);
    		if (cidade == null) {
        		Logger.error("Cidade " + originID + " não encontrada");
        		continue;
    		}

    		Score score = new Score(
    				scoreResultSet.getString(2),
    				scoreResultSet.getFloat(3),
    				scoreResultSet.getFloat(4),
    				scoreResultSet.getFloat(5),
    				scoreResultSet.getFloat(6));
    		
    		if(primeiraExecucao){
    			if(cidade.getId() == 4209102L){
    				Logger.info("Novo score em Joinville: " + score.getArea());
    			}
    			cidade.criaScore(score);
    		}else{
    			if(cidade.getId() == 4209102L){
    				Logger.info("Atualizando score em Joinville: " + score.getArea());
    			}
    			cidade.atualizaScore(score, dataDaAtualizacao);
    		}
    		
    		cidadeDAO.save(cidade);
    		
    		count++;
    		if (count % 2000 == 0) {
    			Logger.info("Atualizou " + count + " scores nas cidades.");
    		}
    	}
    	scoreResultSet.close();
    	
    	new File(dataPath).delete();
	}
	
    	


	
	private void atualizaIniciativas(String dataPath, Date dataDaAtualizacao, boolean primeiraExecucao) throws SQLException {

		ResultSet resultSet = new Csv().read(dataPath, null, "utf-8");
		int count = 0;
		while (resultSet.next()) {

			long idIniciativa = resultSet.getLong("NR_CONVENIO");

			Cidade cidadeDaIniciativa = cidadeDAO.find(resultSet.getLong("cod7"));
			if (cidadeDaIniciativa == null) {
				Logger.error("Cidade " + resultSet.getLong("cod7") + " não encontrada para iniciativa " + idIniciativa);
				continue;
			}

			Iniciativa iniciativaAtualizada = util.DadosUtil.parseIniciativa(resultSet);
			if(iniciativaAtualizada == null){
				continue;
			}
			
			Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
			if (iniciativa == null) {
				cidadeDaIniciativa.addIniciativa(iniciativaAtualizada, dataDaAtualizacao, !primeiraExecucao);
				cidadeDAO.save(cidadeDaIniciativa);
			}else{
				if(!primeiraExecucao){
					iniciativa.atualiza(iniciativaAtualizada, dataDaAtualizacao);
					iniciativaDAO.save(iniciativa);
				}
			}

			count++;
			if (count % 2000 == 0) {
				Logger.info("Atualizou " + count + " iniciativas.");
			}
		}
		
		new File(dataPath).delete();
    }

}
