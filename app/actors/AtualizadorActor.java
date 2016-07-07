package actors;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import models.AtualizacaoDAO;
import models.Cidade;
import models.Score;

import org.h2.tools.Csv;

import play.Logger;
import play.db.jpa.JPAApi;
import actors.AtualizadorActorProtocol.AtualizaIniciativas;
import actors.AtualizadorActorProtocol.AtualizaScores;
import akka.actor.UntypedActor;

public class AtualizadorActor extends UntypedActor {
	
	@Inject
	private JPAApi jpaAPI;
	
	@Inject
	private AtualizacaoDAO daoAtualizacao;
	
	public void onReceive(Object msg) throws Exception {
		Logger.debug("AtualizadorActor.onReceive()");
		if (msg instanceof AtualizaIniciativas) {
			// sender().tell("Hello, " + ((AtualizaIniciativas) msg).name,
			// self());
		} else if (msg instanceof AtualizaScores) {
			play.Logger.debug("****************** Before");
			
			jpaAPI.withTransaction(() -> daoAtualizacao.inicia());
			
			jpaAPI.withTransaction(() -> {
				try {
					Thread.sleep(20000);
					atualizaScores();
					daoAtualizacao.finaliza(false);
					sender().tell(true, self());
				} catch (Exception e) {
					e.printStackTrace();
					daoAtualizacao.finaliza(true);
					sender().tell(false, self());
				}
			});
			play.Logger.debug("****************** After");
		}
	}
	
    private void atualizaScores() throws SQLException {
    	
    	String proxima = daoAtualizacao.find().getProxima();
    	
    	//TODO BAIXAR DADOS E FILTRAR NO R AQUI!
    	
    	String dataPath = daoAtualizacao.getFolder() + "/diferentices-" + proxima + ".csv";
    	EntityManager em = jpaAPI.em();

    	atualizaScores(dataPath, em);
	}

	private void atualizaScores(String dataPath, EntityManager em) throws SQLException{
		int count = 0;

    	final ResultSet scoreResultSet = new Csv().read(dataPath, null, "utf-8");
    	count = 0;
    	while (scoreResultSet.next()) {
    		long originID = scoreResultSet.getLong(1);
    		Cidade cidade = em.find(Cidade.class, originID);
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
    		
    		cidade.atualizaScore(score);
    		
    		em.persist(cidade);
    		
    		count++;
    		if (count % 2000 == 0) {
    			Logger.info("Inseri " + count + " scores nas cidades.");
    		}
    	}
    	scoreResultSet.close();
    	
    	new File(dataPath).delete();
	}

}
