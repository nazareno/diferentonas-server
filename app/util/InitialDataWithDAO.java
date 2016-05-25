package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import models.Cidade;
import models.CidadeDAO;
import models.Iniciativa;
import models.Score;

import org.h2.tools.Csv;

import play.Logger;
import play.api.Environment;
import play.db.jpa.JPAApi;

import com.google.inject.Inject;

/**
 * Loads data into database the first time the application is executed.
 * 
 * @author ricardo
 */
public class InitialDataWithDAO {

    /**
     * @param environment
     * @param jpaAPI
     */
    @Inject
    public InitialDataWithDAO(Environment environment, JPAApi jpaAPI, CidadeDAO dao) {
        Logger.info("Na inicialização da aplicação.");

        List<Cidade> cidades = jpaAPI.withTransaction(entityManager -> {
            return entityManager.createQuery("FROM Cidade", Cidade.class).setMaxResults(2).getResultList();
        });


        if (cidades.isEmpty()) {
            Logger.info("Populando BD");
            try {
                populaCidades(jpaAPI, dao);
                Logger.info("Populou cidades.");
                populaVizinhos(jpaAPI);
                Logger.info("Populou vizinhos");
                populaScores(jpaAPI);
                Logger.info("Populou scores");
            } catch (SQLException e1) {
                Logger.error(e1.getLocalizedMessage(), e1);
            }
        } else {
            Logger.info("BD já populado com cidades, vizinhos e scores");
        }

        // Depois de popular ou já populado
        cidades = jpaAPI.withTransaction(entityManager -> {
            return entityManager.createQuery("FROM Cidade", Cidade.class).setMaxResults(2).getResultList();
        });

        if(cidades.get(1).getIniciativas().isEmpty()) {
            try{
                populaConvenios(jpaAPI);
            } catch (SQLException e1) {
                Logger.error(e1.getLocalizedMessage());
                e1.printStackTrace();
            }
            Logger.info("Populou convenios");
        } else {
            Logger.info("BD já populado com convenios.");
        }
    }

    private void populaScores(JPAApi jpaAPI) throws SQLException {
        jpaAPI.withTransaction(() -> {
            try{
                String dataPath = "dist/data/diferencas-cidades-tudo.csv";
                int count = 0;
                EntityManager em = jpaAPI.em();

                final ResultSet scoreResultSet = new Csv().read(dataPath, null, "utf-8");
                scoreResultSet.next(); // header
                count = 0;
                while (scoreResultSet.next()) {
                    long originID = scoreResultSet.getLong(1);
                    Score score = new Score(
                            scoreResultSet.getString(2),
                            scoreResultSet.getFloat(3),
                            scoreResultSet.getFloat(4),
                            scoreResultSet.getFloat(5));
                    Cidade o = em.find(Cidade.class, originID);
                    if(o != null){
                        o.getScores().add(score);
                    }
                    count++;
                    if(count % 2000 == 0){
                        Logger.info("Inseri " + count + " scores nas cidades.");
                    }
                }
                em.flush();
            }catch (SQLException e){
                Logger.error(e.getMessage(), e);
            }
        });
    }

    private void populaVizinhos(JPAApi jpaAPI) throws SQLException {
        jpaAPI.withTransaction(() -> {
            try {
                EntityManager em = jpaAPI.em();
                String dataPath = "dist/data/vizinhos.euclidiano.csv";
                int count = 0;

                final ResultSet vizinhosResultSet = new Csv().read(dataPath, null, "utf-8");
                vizinhosResultSet.next(); // header
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

                    Cidade o = em.find(Cidade.class, originID);
                    List<Cidade> similares = similaresIDs
                            .mapToObj(
                                    value -> em
                                            .find(Cidade.class, value))
                            .map(obj -> (Cidade) obj)
                            .collect(Collectors.toList());
                    o.setSimilares(similares);
                    // em.persist(o);
                    count++;
                    if (count % 1000 == 0) {
                        Logger.info("Inseri vizinhos para " + count + " cidades...");
                    }
                }
                em.flush();
            } catch (SQLException e) {
                Logger.error(e.getMessage(), e);
            }
        });
    }

    private void populaCidades(JPAApi jpaAPI, CidadeDAO dao) throws SQLException {
        jpaAPI.withTransaction(() -> {
            try {
                String dataPath = "dist/data/dados2010.csv";

                ResultSet resultSet = new Csv().read(dataPath, null, "utf-8");
                resultSet.next(); // header
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
                            resultSet.getLong(9));

                    dao.create(cidade);

                    count++;
                    if (count % 500 == 0) {
                        Logger.info("Inseri " + count + " cidades.");
                    }
                }
            }catch (SQLException e){
                Logger.error(e.getMessage(), e);
            }
        });
    }

    private void populaConvenios(JPAApi jpaAPI) throws SQLException {
        jpaAPI.withTransaction(() -> {
            try {
                EntityManager em = jpaAPI.em();
                // TODO estou perdendo a primeira linha (?)
                String dataPath = "dist/data/iniciativas-detalhadas.csv";

                ResultSet resultSet = new Csv().read(dataPath, null, "utf-8");
                resultSet.next(); // header
                int count = 0;
                while (resultSet.next()) {
                	Long cidade = resultSet.getLong(63);
                	long idIniciativa = resultSet.getLong(3);
                    float verbaGovernoFederal = resultSet.getString(29).contains("NA") ? 0f : resultSet.getFloat(29); // repasse
                    float verbaMunicipio = resultSet.getString(30).contains("NA") ? 0f : resultSet.getFloat(30);	// contrapartida
                    DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
                    Iniciativa iniciativa = new Iniciativa(
                    		idIniciativa,				// id
                            resultSet.getInt(2), 		// ano
                            resultSet.getString(35),	// titulo
                            resultSet.getString(16), 	// programa
                            resultSet.getString(69),	// area
                            resultSet.getString(7),		// fonte
                            resultSet.getString(13),	// concedente
                            resultSet.getString(70),	// status
                            resultSet.getBoolean(50),	// temAditivo
                            verbaGovernoFederal,		// verba do governo federal
                            verbaMunicipio,				// verba do municipio
                            (Date) dateFormat.parse(resultSet.getString(18)),		// data de inicio
                            (Date) dateFormat.parse(resultSet.getString(19))	// data de conclusao municipio
                            );
                            

                    try {
                        Cidade o = em.find(Cidade.class, cidade);
                        if (o != null) {
                            o.getIniciativas().add(iniciativa);
                            //em.persist(o);
                        }
                        count++;
                        if (count % 1000 == 0) {
                            Logger.info("Inseri " + count + " convenios.");
                        }
                    } catch (EntityExistsException e){
                        Logger.warn("Convênio duplicado: " + idIniciativa, e);
                    }
                }
                em.flush();
            } catch (SQLException e){
                Logger.error(e.getMessage(), e);
            } catch (ParseException e){
            	Logger.error(e.getMessage(), e);
            }
        });
    }
}
