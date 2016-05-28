package models;

import com.google.inject.Inject;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;

import javax.inject.Singleton;
import javax.persistence.PersistenceContext;
import java.util.UUID;

@Singleton
public class OpiniaoDAO {

    @PersistenceContext
    private JPAApi jpaAPI;

    @Inject
    public OpiniaoDAO(JPAApi jpaAPI) {
        this.jpaAPI = jpaAPI;
    }

    public Opiniao find(String idOpiniao) {
        UUID idOpiniaoUUID = UUID.fromString(idOpiniao);
        return jpaAPI.em().find(Opiniao.class, idOpiniaoUUID);
    }

    public void delete(Opiniao paraRemover) {
        jpaAPI.em().remove(paraRemover);
    }
}
