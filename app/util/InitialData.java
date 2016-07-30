package util;

import com.google.inject.Inject;
import models.*;
import org.h2.tools.Csv;
import play.Configuration;
import play.Logger;
import play.db.jpa.JPAApi;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Loads data into database the first time the application is executed.
 *
 * @author ricardo
 */
public class InitialData {
	
	private static final String DIST_DATA = "dist/data/";
	private final String dataDir;
	private String [] tipoOpiniao = {"coracao", "coracao_partido", "bomba"};
	private String [][] opinioes = {{
		"Ainda bem que iniciativas como essa estão sendo feitas aqui!",
		"Muito bom! Vai beneficiar muito a população daqui...",
		"Curti!!!!",
		"É disso que nossa cidade está precisando!",
		"Parabéns por mais essa iniciativa da prefeitura"
	},{
		"Tanta coisa melhor pra investir dinheiro né?",
		"A obra tá tão devagar... desse jeito não terminam nunca!",
		"Bem que essa grana poderia estar sendo usada pra melhorar nosso hospital...",
		"A prefeitura só tá fazendo isso pra ganhar popularidade",
		"Afff..."
	},{
		"Gente... a empresa responsável por essa iniciativa não existe não! Tudo faixada...",
		"Não tem nada lá... fui ver ontem!",
		"Essa obra nunca existiu",
		"Isso nunca vai acontecer aqui... o prefeito está embolsando o dinheiro todo!",
		"Passo em frente há um tempão e só tem a placa da iniciativa... nada mais!"
	}};
	
	private List<UUID> cidadaos = new LinkedList<UUID>();
	
    /**
     * @param jpaAPI
     */
    @Inject
    public InitialData(JPAApi jpaAPI, CidadaoDAO daoCidadao, AtualizacaoDAO daoAtualizacao, Configuration configuration) throws SQLException {
        Logger.info("Na inicialização da aplicação.");

		dataDir = Paths.get(configuration.getString("diferentonas.data", DIST_DATA)).toAbsolutePath().toString();

        jpaAPI.withTransaction(()->{
        	daoAtualizacao.create();
        });
        
        populaCidadaos(jpaAPI, daoCidadao, configuration.getString(Cidadao.ADMIN_EMAIL));

        populaCidades(jpaAPI);

        populaIniciativas(jpaAPI, daoCidadao);
    }

	private void populaCidadaos(JPAApi jpaAPI, CidadaoDAO dao, String adminEmail) {
		Logger.info("Populando cidadãos.");
		jpaAPI.withTransaction(() -> {
            Cidadao admin = dao.findByLogin(adminEmail);
            if (admin == null) {
                Cidadao cidadao = new Cidadao("Governo Federal", adminEmail);
                cidadao.setFuncionario(true);
                cidadao.setMinisterioDeAfiliacao("Governo Federal");
				admin = dao.saveAndUpdate(cidadao);
				
				// Usuários para demonstração
				int total = 1000;
                for(int i = 0; i < total; i++ ){
                	Cidadao cidadaoParaDemonstracao = new Cidadao("Anônimo", String.format("cidadao_%03d@mail.com", i));
                	cidadaoParaDemonstracao.setUrlDaFoto("http://www.gravatar.com/avatar/" + cidadaoParaDemonstracao.getLogin().hashCode()
							+ "?f=y&d=retro");
					cidadaos.add(dao.saveAndUpdate(cidadaoParaDemonstracao).getId());
                }
				Logger.info(total + " cidadãos mais o admin cadastrados.");
            } else {
				Logger.info("Admin já cadastrado, assumimos que já temos usuários");
			}
        });
	}

    private void populaCidades(JPAApi jpaAPI) {
	
		List<Cidade> cidades = jpaAPI.withTransaction(entityManager -> {
			return entityManager.createQuery("FROM Cidade", Cidade.class).setMaxResults(2).getResultList();
		});
	
		if (!cidades.isEmpty()) {
			Logger.info("BD já populado com cidades, vizinhos e diferentices");
			return;
		}
	
		Logger.info("Populando BD com cidades, vizinhos e diferentices");
		jpaAPI.withTransaction(() -> {
			try {
				String dataPath = dataDir + "/dados2010.csv";
				Logger.info("Cidades vêm de " + dataPath);
	
				ResultSet resultSet = new Csv().read(dataPath, null, "utf-8");
				int count = 0;
				while (resultSet.next()) {
					Cidade cidade = new Cidade(
							resultSet.getLong(2),
							resultSet.getString(3),
							resultSet.getString(4),
							resultSet.getFloat(5),
							resultSet.getFloat(6),
							resultSet.getFloat(7),
							resultSet.getFloat(8),
							resultSet.getLong(9),
							resultSet.getFloat(10),
							resultSet.getFloat(11),
							resultSet.getFloat(12));
	
					jpaAPI.em().persist(cidade);
	
					count++;
					if (count % 1000 == 0) {
						Logger.info("Inseri " + count + " cidades.");
					}
				}
				populaVizinhos(jpaAPI);
				populaScores(jpaAPI);
			} catch (SQLException | IOException e) {
				Logger.error(e.getMessage(), e);
			}
		});
		Logger.info("Populou BD com cidades, vizinhos e diferentices.");
	}

	private void populaVizinhos(JPAApi jpaAPI) throws SQLException {
		String dataPath = dataDir + "/vizinhos.euclidiano.csv";
		Logger.info("Cidades semelhantes vêm de " + dataPath);
		int count = 0;

		final ResultSet vizinhosResultSet = new Csv().read(dataPath, null, "utf-8");
		count = 0;
		while (vizinhosResultSet.next()) {
			long originID = vizinhosResultSet.getLong(12);

			LongStream similaresIDs = LongStream
					.range(13, 23)
					.map(value -> {
						try {
							return vizinhosResultSet.getLong((int) value);
						} catch (Exception e) {
							Logger.error("Não recuperou cidade " + value + " similar a " + originID, e);
							return 0;
						}
					});

			EntityManager em = jpaAPI.em();
			Cidade o = em.find(Cidade.class, originID);
			List<Cidade> similares = similaresIDs
					.mapToObj(
							value -> em
							.find(Cidade.class, value))
							.map(obj -> (Cidade) obj)
							.collect(Collectors.toList());
			o.setSimilares(similares);
			em.persist(o);
			count++;
			if (count % 1000 == 0) {
				Logger.info("Inseri vizinhos para " + count + " cidades...");
			}
		}
    }

    private void populaScores(JPAApi jpaAPI) throws SQLException, IOException {
    	
    	List<String> listaAtualizacoes = DadosUtil.listaAtualizacoes(dataDir);
    	String dataPath = dataDir + "/diferentices-" + listaAtualizacoes.get(listaAtualizacoes.size()-1) + ".csv";
		Logger.info("Diferentices vêm de " + dataPath);

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
    		
    		score.setCidade(cidade);
    		
    		em.persist(score);
    		
    		count++;
    		if (count % 20000 == 0) {
    			Logger.info("Inseri " + count + " diferentices nas cidades.");
    		}
    	}
		new File(dataPath).delete();

	}

	private void populaIniciativas(JPAApi jpaAPI, CidadaoDAO daoCidadao) {
		Random r = new Random();
    	
        List<Iniciativa> iniciativas = jpaAPI.withTransaction(entityManager -> {
            return entityManager.createQuery("FROM Iniciativa", Iniciativa.class).setMaxResults(2).getResultList();
        });

        if (!iniciativas.isEmpty()) {
        	Logger.info("BD já populado com iniciativas.");
        	return;
        }
        
        jpaAPI.withTransaction(() -> {
            try {
                EntityManager em = jpaAPI.em();

                List<String> listaAtualizacoes = DadosUtil.listaAtualizacoes(dataDir);
            	
            	String dataPath = dataDir + "/iniciativas-" + listaAtualizacoes.get(listaAtualizacoes.size()-1) + ".csv";
				Logger.info("Iniciativas vêm de " + dataPath);

                ResultSet resultSet = new Csv().read(dataPath, null, "utf-8");
                int count = 0;
                while (resultSet.next()) {

                	long idIniciativa = resultSet.getLong("NR_CONVENIO");
                	if (em.find(Iniciativa.class, idIniciativa) != null) {
                    	Logger.warn("Convênio duplicado: " + idIniciativa);
                		continue;
                	}
                	
                	Cidade cidadeDaIniciativa = em.find(Cidade.class, resultSet.getLong("cod7"));
                	if (cidadeDaIniciativa == null) {
                		Logger.error("Cidade " + resultSet.getLong("cod7") + " não encontrada para iniciativa " + idIniciativa);
                		continue;
                	}
                	
                	Iniciativa iniciativa = DadosUtil.parseIniciativa(resultSet);
                	
                	if(iniciativa == null){
                		continue;
                	}
                    
                    iniciativa.setCidade(cidadeDaIniciativa);
                    
                    em.persist(iniciativa);
                    
//                    int numeroDeOpinioes = 2; // LOCAL ONLY
                    int numeroDeOpinioes = 5 + r.nextInt(8); // POPULATE HEROKU DB
					for (int i = 0; i < numeroDeOpinioes; i++) {
                    	Cidadao cidadao = daoCidadao.find(cidadaos.get(r.nextInt(1000)));
                    	Opiniao opiniao = new Opiniao();
                    	int tipo = r.nextInt(3);
						opiniao.setTipo(tipoOpiniao[tipo]);
                    	opiniao.setConteudo(opinioes[tipo][r.nextInt(5)]);
                    	opiniao.setAutor(cidadao);
                    	iniciativa.addOpiniao(opiniao);
					}
                    em.persist(iniciativa);

                    count++;
                    if (count % 2000 == 0) {
                    	Logger.info("Inseri " + count + " iniciativas.");
                    	em.flush();
                    }
                }
        		new File(dataPath).delete();

            } catch (SQLException  e) {
                Logger.error("Parando prematuramente a inserção de Iniciativas!!!!!!");
                Logger.error(e.getMessage(), e);
            }
        });
    }

}
