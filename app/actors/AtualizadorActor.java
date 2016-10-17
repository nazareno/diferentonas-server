package actors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

		Logger.debug("AtualizadorActor.onReceive()");
		if (msg instanceof AtualizadorActorProtocol.AtualizaIniciativasEScores) {
			jpaAPI.withTransaction(() -> daoAtualizacao.inicia());
			
			Atualizacao status = jpaAPI.withTransaction(() -> daoAtualizacao.find());
			List<String> datasDisponiveis = preparaDados(status.getUltima());
			
			if(datasDisponiveis == null){
				daoAtualizacao.finaliza(true);
				sender().tell(false, self());
				return;
			}
			
			
			for (String data: datasDisponiveis) {
				jpaAPI.withTransaction(() -> {
					try {
						
						boolean primeiraExecucao = status.getUltima().isEmpty();

						Date proximaData = formatoDataAtualizacao.parse(data);

						String scoresDataPath = Paths.get(daoAtualizacao.getFolder()).toAbsolutePath().toString() + "/diferentices-" + data + ".csv";
						Logger.debug("Usando arquivo de scores: " + scoresDataPath);
						atualizaScores(scoresDataPath, proximaData, primeiraExecucao);

						String iniciativasDataPath = Paths.get(daoAtualizacao.getFolder()).toAbsolutePath().toString() + "/iniciativas-" + data + ".csv";
						Logger.debug("Arquivo de inciativas: " + iniciativasDataPath);
						atualizaIniciativas(iniciativasDataPath, proximaData, primeiraExecucao);

					} catch (Exception e) {
						e.printStackTrace();
						daoAtualizacao.finaliza(true);
						sender().tell(false, self());
						return;
					}
				});
			}
			
			jpaAPI.withTransaction(() -> daoAtualizacao.finaliza(false));
			sender().tell(true, self());
		} else {
			Logger.warn("Mensagem de tipo desconhecido: " + msg.getClass().toString());
		}
	}
	
	private List<String> preparaDados(String ultimaDataAtualizada) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(configuration.getString("diferentonas.atualizacao", "false"));
		boolean ok = process.waitFor(2, TimeUnit.HOURS);
		if(!ok){
			return null;
		}

		return DadosUtil.listaAtualizacoes(configuration.getString("diferentonas.data", "dist/data"), ultimaDataAtualizada);
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
    			cidade.criaScore(score);
    		}else{
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
