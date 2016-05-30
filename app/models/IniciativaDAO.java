package models;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import play.db.jpa.JPAApi;

import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Singleton
public class IniciativaDAO {

    @PersistenceContext
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
    	    	
        Query query = jpaAPI.em().createNativeQuery("SELECT i.* "
        		+ "FROM iniciativa AS i, TS_RANK(TO_TSVECTOR('portuguese', i.titulo ), PLAINTO_TSQUERY('portuguese',?)) AS rank "
        		+ "WHERE TO_TSVECTOR('portuguese', i.titulo ) @@ PLAINTO_TSQUERY('portuguese',?) ORDER BY rank DESC"
        		, Iniciativa.class)
        		.setParameter(1, outro.getTitulo())
        		.setParameter(2, outro.getTitulo());
		return query.setMaxResults(quantidade.intValue()).getResultList();
    }


}
