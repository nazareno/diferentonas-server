package models;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.db.jpa.JPAApi;

import javax.persistence.PersistenceContext;

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
}
