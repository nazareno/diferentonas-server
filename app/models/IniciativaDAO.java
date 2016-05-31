package models;

import java.util.List;

import javax.persistence.Query;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IniciativaDAO {

    private JPAApi jpaAPI;

    @Inject
    public IniciativaDAO(JPAApi jpaAPI) {
        this.jpaAPI = jpaAPI;
    }

    public Iniciativa find(Long id) {
        return jpaAPI.em().find(Iniciativa.class, id);
    }

    public void flush() {
        jpaAPI.em().flush();
    }
    
    public List<Iniciativa> findSimilares(Long id, Long quantidade) {
    	Iniciativa outro = find(id);
    	
        Query query = jpaAPI.em().createNativeQuery("SELECT iniciativa.* "
        		+ "FROM iniciativa, TO_TSVECTOR('portuguese', titulo ) as titulo_v, TO_TSQUERY('portuguese',?) as query, TS_RANK(titulo_v, query) AS rank "
        		+ "WHERE titulo_v @@ query AND id != ? ORDER BY rank DESC"
        		, Iniciativa.class)
        		.setParameter(1, String.join(" | ", outro.getTitulo().split(" +")))
        		.setParameter(2, id);
		return query.setMaxResults(quantidade.intValue()).getResultList();
    }


}
