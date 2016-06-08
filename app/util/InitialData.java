package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import models.*;

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

    /**
     * @param jpaAPI
     */
    @Inject
    public InitialData(JPAApi jpaAPI, CidadaoDAO dao, MensagemDAO daoMensagem, IniciativaDAO daoIniciativa) throws SQLException {
        Logger.info("Na inicialização da aplicação.");

        List<Cidade> cidades = jpaAPI.withTransaction(entityManager -> {
            return entityManager.createQuery("FROM Cidade", Cidade.class).setMaxResults(2).getResultList();
        });

        if (cidades.isEmpty()) {
            Logger.info("Populando BD");
            jpaAPI.withTransaction(() -> {
                try {
                    populaCidades(jpaAPI);
                    populaVizinhos(jpaAPI);
                    populaScores(jpaAPI);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            Logger.info("Populou cidades.");
        } else {
            Logger.info("BD já populado com cidades, vizinhos e scores");
        }

        // Depois de popular ou já populado
        cidades = jpaAPI.withTransaction(entityManager -> {
            return entityManager.createQuery("FROM Cidade", Cidade.class).setMaxResults(2).getResultList();
        });

        if (cidades.get(1).getIniciativas().isEmpty()) {
            try {
                populaConvenios(jpaAPI);
            } catch (SQLException e1) {
                Logger.error(e1.getLocalizedMessage());
                e1.printStackTrace();
            }
            Logger.info("Populou iniciativas.");
        } else {
            Logger.info("BD já populado com convenios.");
        }
        
        
        jpaAPI.withTransaction(() -> {
            Cidadao admin = dao.findByLogin("admin");
            if (admin == null) {
                admin = dao.create(new Cidadao("admin"));
                daoMensagem.create(new Mensagem("Fiquem atentos!", "Importante!", "Ministério da Justiça"));
                daoMensagem.create(new Mensagem("Muitos convênios estão sendo celebrados esse mês... fique de olho na sua cidade", "Atenção!", "Ministério do Planejamento"));
                daoMensagem.create(new Mensagem("Achou alguma estranha nas iniciativas da sua cidade? Denuncie!", "Denuncie também!", "Ministério da Justiça"));
                
                
                Cidadao iara = dao.create(new Cidadao("Iara"));
                Cidadao luiz = dao.create(new Cidadao("Luiz"));
                Cidadao mariana = dao.create(new Cidadao("Mariana"));
                Cidadao nazareno = dao.create(new Cidadao("Nazareno"));
                Cidadao ricardo = dao.create(new Cidadao("Ricardo"));
                
                Iniciativa i = daoIniciativa.find(816240L);
                admin.inscreverEm(i);
				i.addOpiniao(new Opiniao(i, "não gostei...", "coracao-partido",iara));
				i.addOpiniao(new Opiniao(i, "tá estranho... passei ontem lá e não tinha nada.", "coracao-partido",luiz));
				i.addOpiniao(new Opiniao(i, "Me disseram que a empresa é fantasma", "bomba",mariana));
				i.addOpiniao(new Opiniao(i, "Eu conheço o dono da empresa, é verdadeira sim! Mas até agora não iniciaram a obra", "coracao",luiz));
				i.addOpiniao(new Opiniao(i, "quero mais é ver isso pronto", "coracao-partido",iara));
				
                i = daoIniciativa.find(791092L);
                admin.inscreverEm(i);
				i.addOpiniao(new Opiniao(i, "Prefeitura de parabéns, pedalo lá com minha digníssima todo dia!", "coracao",nazareno));
				i.addOpiniao(new Opiniao(i, "achei os equipamentos de má qualidade", "coracao-partido",iara));
				i.addOpiniao(new Opiniao(i, "o local não tem segurança nenhuma, podiam ter feito uma guarita!", "coracao-partido",mariana));
				i.addOpiniao(new Opiniao(i, "Também pedalo lá...", "coracao",ricardo));
				
				
				
            }
        });

    }

    private void populaScores(JPAApi jpaAPI) throws SQLException {
        try {
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
                if (o != null) {
                    o.getScores().add(score);
                }
                count++;
                if (count % 2000 == 0) {
                    Logger.info("Inseri " + count + " scores nas cidades.");
                }
            }
        } catch (SQLException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private void populaVizinhos(JPAApi jpaAPI) throws SQLException {
        try {
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
        } catch (SQLException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private void populaCidades(JPAApi jpaAPI) throws SQLException {
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
        } catch (SQLException e) {
            Logger.error(e.getMessage(), e);
        }
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
                    Long idCidade = resultSet.getLong(63);
                    long idIniciativa = resultSet.getLong(3);

                    float verbaGovernoFederal = resultSet.getString(29).contains("NA") ? 0f : resultSet.getFloat(29); // repasse
                    float verbaMunicipio = resultSet.getString(30).contains("NA") ? 0f : resultSet.getFloat(30);    // contrapartida

                    DateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
                    Date dataConclusao = formatter.parse(resultSet.getString(19));

                    // Adicionando 2 meses para o prazo de prestação de contas
                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(dataConclusao);
                    cal.add(GregorianCalendar.MONTH, 2);
                    Date dataConclusaoGovernoFederal = cal.getTime();
                    Iniciativa iniciativa = new Iniciativa(
                            idIniciativa,                // id
                            resultSet.getInt(2),        // ano
                            resultSet.getString(35),    // titulo
                            resultSet.getString(16),    // programa
                            resultSet.getString(69),    // area
                            resultSet.getString(7),        // fonte
                            resultSet.getString(13),    // concedente
                            resultSet.getString(70),    // status
                            resultSet.getBoolean(50),    // temAditivo
                            verbaGovernoFederal,        // verba do governo federal
                            verbaMunicipio,                // verba do municipio
                            formatter.parse(resultSet.getString(18)),        // data de inicio
                            dataConclusao,    // data de conclusao municipio
                            dataConclusaoGovernoFederal);


                    try {
                        Cidade cidadeDaIniciativa = em.find(Cidade.class, idCidade);
                        if (cidadeDaIniciativa != null) {
                            iniciativa.setCidade(cidadeDaIniciativa);
                            cidadeDaIniciativa.getIniciativas().add(iniciativa);
                        } else {
                            Logger.error("Cidade " + idCidade + " não encontrada para iniciativa " + idIniciativa);
                        }
                        count++;
                        if (count % 2000 == 0) {
                            Logger.info("Inseri " + count + " iniciativas.");
                        }
                    } catch (EntityExistsException e) {
                        Logger.warn("Convênio duplicado: " + idIniciativa, e);
                    }
                }
            } catch (SQLException e) {
                Logger.error(e.getMessage(), e);
            } catch (ParseException e) {
                Logger.error(e.getMessage(), e);
            }
        });
    }
}
