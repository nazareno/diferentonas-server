package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IniciativaDAO {

	private static final int LIMITE_DE_SIMILARES = 50;
	private JPAApi jpaAPI;

	@Inject
	public IniciativaDAO(JPAApi jpaAPI) {
		this.jpaAPI = jpaAPI;
	}

	public Iniciativa find(Long id) {
		return jpaAPI.em().find(Iniciativa.class, id);
	}

	public void save(Iniciativa iniciativa) {
		jpaAPI.em().persist(iniciativa);
	}

	public void remove(Iniciativa iniciativa){
		jpaAPI.em().remove(iniciativa);
	}
	
	public void flush() {
		jpaAPI.em().flush();
	}

	/**
	 * Retorna as {@link Iniciativa}s mais similares ordenadas por proximidade,
	 * dado o id de uma iniciativa.
	 * 
	 * @param id de uma {@link Iniciativa}
	 * @param quantidade de iniciativas pra retornar.
	 * @param cidadao
	 * @return Uma lista de {@link Iniciativa}s.
	 */
	public List<Iniciativa> findSimilares(Long id, Long quantidade, Cidadao cidadao) {
		return jpaAPI.withTransaction(()->{
			Iniciativa outro = find(id);
			Query query = jpaAPI
					.em()
					.createNativeQuery(
							"with similares AS ("
									+ "SELECT i.* FROM iniciativa AS i, "
									+ "TO_TSVECTOR('portuguese', i.titulo ) AS titulo_v, "
									+ "TO_TSQUERY('portuguese', ?) AS query, "
									+ "TS_RANK(titulo_v, query) AS rank "
									+ "WHERE titulo_v @@ query AND i.id != ? "
									+ "ORDER BY rank DESC limit ?), "
									+ "distancias AS ( "
									+ "SELECT i.id AS i, c.id AS c, point(c.longitude, c.latitude) AS p "
									+ "FROM cidade c "
									+ "INNER JOIN similares i on c.id = i.cidade), "
									+ "escolhida AS ( SELECT i.id AS i, c.id AS c, point(c.longitude, c.latitude) AS p "
									+ "FROM cidade c "
									+ "INNER JOIN iniciativa i on c.id = i.cidade "
									+ "WHERE i.id = ?) "
									+ "SELECT ini.* AS id "
									+ "FROM distancias d, escolhida e, iniciativa ini "
									+ "WHERE ini.id = d.i "
									+ "ORDER BY (d.p <@> e.p) asc, ini.id asc limit ?",
									Iniciativa.class)
									.setParameter(1,String.join(" | ", limpaParaConsultaDeTexto(outro.getTitulo()).split(" +")))
									.setParameter(2, id )
									.setParameter(3, LIMITE_DE_SIMILARES)
									.setParameter(4, id)
									.setParameter(5, quantidade.intValue());

			@SuppressWarnings("unchecked")
			List<Iniciativa> retorno = (List<Iniciativa>) query.getResultList();

			return adicionaSumarios(retorno, cidadao);
		});
	}

	private String limpaParaConsultaDeTexto(String titulo) {
		return titulo.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
	}

	public List<Iniciativa> adicionaSumarios(List<Iniciativa> retorno, Cidadao cidadao) {
		for (Iniciativa iniciativa : retorno) {
			adicionaSumarios(iniciativa, cidadao);
        }

		return retorno;
	}

	public Iniciativa adicionaSumarios(Iniciativa iniciativa, Cidadao cidadao) {
		iniciativa.setSumario(this.calculaSumario(iniciativa.getId()));
		return iniciativa;
	}

	public Map<String, Long> calculaSumario(Long id) {
		HashMap<String, Long> hashMap = new HashMap<String, Long>();
		hashMap.put("coracao", 0L);
		hashMap.put("coracao_partido", 0L);
		hashMap.put("bomba", 0L);
		Query query = jpaAPI.em().createQuery("SELECT o.tipo as tipo, count(id) as quantidade FROM Opiniao o "
				+ "WHERE o.iniciativa.id = :paramId "
				+ "GROUP BY o.tipo").setParameter("paramId", id);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		for (Object[] objects : resultList) {
			hashMap.put((String)objects[0], (Long)objects[1]);
		}
		return hashMap;
	}
}
