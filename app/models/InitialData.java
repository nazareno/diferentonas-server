package models;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import org.h2.tools.Csv;

import play.Logger;
import play.api.Environment;
import play.db.jpa.JPAApi;

import com.google.inject.Inject;

public class InitialData {

    @Inject
    public InitialData(Environment environment, JPAApi jpaAPI) {

        List<Cidade> cidades = jpaAPI.withTransaction(entityManager -> {
            return entityManager.createQuery("FROM Cidade", Cidade.class).getResultList();
        });


        if (cidades.isEmpty() || cidades.size() < 5000) {
            Logger.info("Populando BD");
            try {
                populaCidades(jpaAPI);
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

        if(cidades.get(1).getConvenios().isEmpty()) {
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
                String dataPath = "dist/data/diferencas-cidades.csv";
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
                        //em.persist(o);
                    }
                    count++;
                    if(count % 500 == 0){
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
                    if (count % 500 == 0) {
                        Logger.info("Inseri vizinhos para " + count + " cidades...");
                    }
                }
                em.flush();
            } catch (SQLException e) {
                Logger.error(e.getMessage(), e);
            }
        });
    }

    private void populaCidades(JPAApi jpaAPI) throws SQLException {
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

                    jpaAPI.em().persist(cidade);

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
                String dataPath = "dist/data/convenios-municipio-detalhes-ccodigo.csv";

                ResultSet resultSet = new Csv().read(dataPath, null, "utf-8");
                resultSet.next(); // header
                int count = 0;
                while (resultSet.next()) {
                    Long cidade = resultSet.getLong(48);
                    float repasse = resultSet.getString(29).contains("NA") ? 0f : resultSet.getFloat(29); // repasse
                    long idConvenio = resultSet.getLong(2);
                    Convenio convenio = new Convenio(
                            idConvenio, // numero
                            resultSet.getInt(1), // ano
                            resultSet.getString(4), // situacao
                            resultSet.getString(7), // orgao superior
                            resultSet.getString(16), // TX programa
                            repasse,
                            resultSet.getString(35)); // objeto


                    try {
                        Cidade o = em.find(Cidade.class, cidade);
                        if (o != null) {
                            o.getConvenios().add(convenio);
                            //em.persist(o);
                        }
                        count++;
                        if (count % 500 == 0) {
                            Logger.info("Inseri " + count + " convenios.");
                        }
                    } catch (EntityExistsException e){
                        Logger.warn("Convênio duplicado: " + idConvenio, e);
                    }
                }
                em.flush();
            } catch (SQLException e){
                Logger.error(e.getMessage(), e);
            }
        });
    }
}
