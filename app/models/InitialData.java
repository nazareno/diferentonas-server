package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

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
				try {
					System.out.println(new File(".").getCanonicalPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ResultSet resultSet = new Csv().read("public/data/dados2010.csv", null, "utf-8");
				resultSet.next();
				while (resultSet.next()) {
					Cidade cidade = new Cidade(
							resultSet.getLong(2),
							resultSet.getString(1), resultSet.getDouble(3),
							resultSet.getDouble(4), resultSet.getDouble(5),
							resultSet.getDouble(6), resultSet.getLong(7));
					jpaAPI.withTransaction(() -> {
						jpaAPI.em().persist(cidade);
					});
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// try (Scanner input = new Scanner(new File(
			// "public/data/dados2010.csv"));) {
			// input.nextLine();
			// while (input.hasNextLine()) {
			// String[] fields = input.nextLine().split(" +");
			// Cidade cidade = new Cidade(Long.valueOf(fields[1]),
			// fields[0], Double.valueOf(fields[2]),
			// Double.valueOf(fields[3]),
			// Double.valueOf(fields[4]),
			// Double.valueOf(fields[5]), Long.valueOf(fields[6]));
			// jpaAPI.withTransaction(() -> {
			// jpaAPI.em().persist(cidade);
			// });
			// }
			// } catch (FileNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}
}
