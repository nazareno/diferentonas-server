package models;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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


        if (cidades.isEmpty()) {
            Logger.info("Populando BD");
            try {
                populaCidades(jpaAPI);
                populaVizinhos(jpaAPI);
                populaScores(jpaAPI);
            } catch (SQLException e1) {
                Logger.error(e1.getLocalizedMessage());
                e1.printStackTrace();
            }

            Logger.info("Populou! ");


        } else {
            Logger.info("BD já populado");
        }
    }

    private void populaScores(JPAApi jpaAPI) throws SQLException {
        String dataPath = "dist/data/diferencas-cidades.csv";
        int count = 0;

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

            jpaAPI.withTransaction(() -> {
                EntityManager em = jpaAPI.em();
                Cidade o = em.find(Cidade.class, originID);
                if(o != null){
                    o.getScores().add(score);
                    em.persist(o);
                    em.flush();
                }
            });
            count++;
            if(count % 500 == 0){
                Logger.info("Inseri " + count + " scores nas cidades.");
            }
        }
    }

    private void populaVizinhos(JPAApi jpaAPI) throws SQLException {
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
                            e.printStackTrace();
                            System.err.println("Não recuperou cidade " + value + " similar a " + originID);
                            return 0;
                        }
                    });

            jpaAPI.withTransaction(() -> {
                EntityManager em = jpaAPI.em();

                Cidade o = em.find(Cidade.class, originID);
                List<Cidade> similares = similaresIDs
                        .mapToObj(
                                value -> em
                                        .find(Cidade.class, value))
                        .map(obj -> (Cidade) obj)
                        .collect(Collectors.toList());
                o.setSimilares(similares);
//			            Logger.info("Populando similares " + o);
                em.persist(o);
                em.flush();
            });
            count++;
            if(count % 500 == 0){
                Logger.info("Inseri vizinhos para " + count + " cidades...");
            }
        }
    }

    private void populaCidades(JPAApi jpaAPI) throws SQLException {
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
            jpaAPI.withTransaction(() -> {
                jpaAPI.em().persist(cidade);
            });
            count++;
            if(count % 500 == 0){
                Logger.info("Inseri " + count + " cidades.");
            }
        }
    }
}
