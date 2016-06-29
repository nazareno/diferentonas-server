package controllers;


import static play.libs.Json.toJson;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import models.Cidade;
import models.Score;

import org.h2.tools.Csv;

import play.Logger;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

public class AtualizacaoController extends Controller {
	
	@Inject
	private JPAApi jpaAPI;

    private static final String folder = "dist/data/";

	@Transactional(readOnly = true)
    public Result getAtualizacoes(){
    	
    	try{
    		return ok(toJson(listaAtualizacoes()));
    	}catch(IOException e){
    		return notFound(folder);
    	}
    }

	private List<String> listaAtualizacoes() throws IOException {

		List<String> paths = new ArrayList<>();

		Path dir = Paths.get(folder);

		Logger.debug(dir.toAbsolutePath().toString());

		try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
				"iniciativas-[0-9]*.csv");) {

			for (Path path : ds) {
				String name = path.getFileName().toString();

				paths.add(name.substring(name.lastIndexOf("-") + 1,
						name.indexOf(".csv")));
			}

			Collections.reverse(paths);
			return paths;
		}
	}

	@Transactional
	public Result aplica(){
		
		try {
			List<String> atualizacoes = listaAtualizacoes();
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
