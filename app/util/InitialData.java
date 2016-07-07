package util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.persistence.EntityManager;

import models.AtualizacaoDAO;
import models.Cidadao;
import models.CidadaoDAO;
import models.Cidade;
import models.Iniciativa;
import models.Opiniao;
import models.Score;

import org.h2.tools.Csv;

import play.Logger;
import play.db.jpa.JPAApi;

import com.google.inject.Inject;

/**
 * Loads data into database the first time the application is executed.
 *
 * @author ricardo
 */
public class InitialData {
	
	private static final String DIST_DATA = "dist/data/";
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
    public InitialData(JPAApi jpaAPI, CidadaoDAO daoCidadao, AtualizacaoDAO daoAtualizacao) throws SQLException {
        Logger.info("Na inicialização da aplicação.");
        
        jpaAPI.withTransaction(()->{
        	daoAtualizacao.create();
        });
        
        populaCidadaos(jpaAPI, daoCidadao);

        populaCidades(jpaAPI);

        populaIniciativas(jpaAPI, daoCidadao);
    }

	private void populaCidadaos(JPAApi jpaAPI, CidadaoDAO dao) {
		jpaAPI.withTransaction(() -> {
            Cidadao admin = dao.findByLogin("admin");
            if (admin == null) {
                admin = dao.create(new Cidadao("admin"));
                for(int i = 0; i < 1000; i++ ){
                	cidadaos.add(dao.create(new Cidadao(String.format("cidadão_%03d", i))).getId());
                }
            }
        });
	}

    private void populaCidades(JPAApi jpaAPI) {
	
		List<Cidade> cidades = jpaAPI.withTransaction(entityManager -> {
			return entityManager.createQuery("FROM Cidade", Cidade.class).setMaxResults(2).getResultList();
		});
	
		if (!cidades.isEmpty()) {
			Logger.info("BD já populado com cidades, vizinhos e scores");
			return;
		}
	
		Logger.info("Populando BD com cidades, vizinhos e scores");
		jpaAPI.withTransaction(() -> {
			try {
				String dataPath = "dist/data/dados2010.csv";
	
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
					if (count % 500 == 0) {
						Logger.info("Inseri " + count + " cidades.");
					}
				}
				populaVizinhos(jpaAPI);
				populaScores(jpaAPI);
			} catch (SQLException | IOException e) {
				Logger.error(e.getMessage(), e);
			}
		});
		Logger.info("Populou BD com cidades, vizinhos e scores.");
	}

	private void populaVizinhos(JPAApi jpaAPI) throws SQLException {
		String dataPath = "dist/data/vizinhos.euclidiano.csv";
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
    	
    	List<String> listaAtualizacoes = DadosUtil.listaAtualizacoes(DIST_DATA);
    	
    	String dataPath = DIST_DATA + "diferentices-" + listaAtualizacoes.get(listaAtualizacoes.size()-1) + ".csv";
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
    		if (count % 2000 == 0) {
    			Logger.info("Inseri " + count + " scores nas cidades.");
    		}
    	}
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
                // TODO estou perdendo a primeira linha (?)
                
                List<String> listaAtualizacoes = DadosUtil.listaAtualizacoes(DIST_DATA);
            	
            	String dataPath = DIST_DATA + "iniciativas-" + listaAtualizacoes.get(listaAtualizacoes.size()-1) + ".csv";

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
                	
                	Iniciativa iniciativa = parseIniciativa(resultSet);
                	
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
            } catch (SQLException  e) {
                Logger.error("Parando prematuramente a inserção de Iniciativas!!!!!!");
                Logger.error(e.getMessage(), e);
            }
        });
    }

	private Iniciativa parseIniciativa(ResultSet resultSet) {
		try{
			float verbaGovernoFederal = resultSet.getString("VL_REPASSE_CONV").contains("NA") ? 0f : resultSet.getFloat("VL_REPASSE_CONV"); // repasse
			float verbaMunicipio = resultSet.getString("VL_CONTRAPARTIDA_CONV").contains("NA") ? 0f : resultSet.getFloat("VL_CONTRAPARTIDA_CONV");    // contrapartida

			DateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
			Date dataConclusao = formatter.parse(resultSet.getString("DIA_FIM_VIGENC_CONV"));

			// Adicionando 2 meses para o prazo de prestação de contas
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(dataConclusao);
			cal.add(GregorianCalendar.MONTH, 2);
			Date dataConclusaoGovernoFederal = cal.getTime();


			return new Iniciativa(
					resultSet.getLong("NR_CONVENIO"),        // id
					resultSet.getInt("ANO"),        // ano
					resultSet.getString("OBJETO_PROPOSTA"),    // titulo
					resultSet.getString("Nome Programa"),    // programa
					resultSet.getString("Nome Sub Funcao"),    // area
					resultSet.getString("DESC_ORGAO_SUP"),        // fonte
					resultSet.getString("DESC_ORGAO"),    // concedente
					resultSet.getString("TX_STATUS"),    // status
					false,//resultSet.getBoolean(50),    // temAditivo
					verbaGovernoFederal,        // verba do governo federal
					verbaMunicipio,                // verba do municipio
					formatter.parse(resultSet.getString("DIA_INIC_VIGENC_CONV")),        // data de inicio
					dataConclusao,    // data de conclusao municipio
					dataConclusaoGovernoFederal);	
		}catch(SQLException | ParseException e){
			Logger.error("Erro no parsing da iniciativa em: " + resultSet.toString());
		}
		return null;
	}
}
