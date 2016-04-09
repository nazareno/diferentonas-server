package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.h2.tools.Csv;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;

public class InitialData {

	@Inject
	public InitialData(JPAApi jpaAPI) {

		List<Cidade> cidades = jpaAPI.withTransaction(entityManager -> {
			return entityManager.createQuery("SELECT c FROM Cidade c",
					Cidade.class).getResultList();
		});

		if (cidades.isEmpty()) {
			try {
				ResultSet resultSet = new Csv().read("public/data/dados2010.csv", null, "utf-8");
				resultSet.next();
				while (resultSet.next()) {
					Cidade cidade = new Cidade(
							resultSet.getLong(2),
							resultSet.getString(3), 
							resultSet.getString(4),
							resultSet.getDouble(5), 
							resultSet.getDouble(6),
							resultSet.getDouble(7), 
							resultSet.getDouble(8), 
							resultSet.getLong(9));
					jpaAPI.withTransaction(() -> {
						jpaAPI.em().persist(cidade);
					});
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
