package controllers;


import static play.libs.Json.toJson;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import models.Cidade;
import models.Score;

import org.h2.tools.Csv;

import play.Configuration;
import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import util.DadosUtil;

@Singleton
public class AtualizacaoController extends Controller {
	
	
	private JPAApi jpaAPI;
	private String folder;
	
	
	@Inject
	public AtualizacaoController(JPAApi jpaAPI, Configuration configuration) {
		this.jpaAPI = jpaAPI;
		this.folder = configuration.getString("diferentonas.data", "dist/data");
	}


	@Transactional(readOnly = true)
    public Result getAtualizacoes(){
    	
    	try{
    		return ok(toJson(DadosUtil.listaAtualizacoes(folder)));
    	}catch(IOException e){
    		return notFound(folder);
    	}
    }


	@Transactional
	public Result aplica(){
		
		try {
			List<String> atualizacoes = DadosUtil.listaAtualizacoes(folder);
			if(atualizacoes.isEmpty()){
				return notFound(folder);
			}
			
			String maisNova = atualizacoes.get(0);
			
			atualizaScores(maisNova);
			
			return ok();
		} catch (IOException | SQLException e) {
			return notFound(folder);
		}
	}
	
    private void atualizaScores(String atualizacao) throws SQLException {
    	String dataPath = "dist/data/diferentices-" + atualizacao + ".csv";
    	int count = 0;
    	EntityManager em = jpaAPI.em();

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
	}


}
