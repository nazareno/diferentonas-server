package models;

import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;

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

    public List<Opiniao> findByIniciativa(Long idIniciativa, int pagina, int tamanhoPagina) {
        TypedQuery<Opiniao> query = jpaAPI.em().createQuery(
                "SELECT o FROM Opiniao o WHERE o.iniciativa.id = :id ORDER BY criadaEm desc", Opiniao.class)
                .setParameter("id", idIniciativa)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina);
        return query.getResultList();
    }
}
