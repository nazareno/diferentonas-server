package util;

import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.persistence.EntityManager;

import models.AtualizacaoDAO;
import models.Cidadao;
import models.CidadaoDAO;
import models.Cidade;

import org.h2.tools.Csv;

import play.Configuration;
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
	private final String dataDir;
	
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
			} catch (SQLException e) {
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

}
