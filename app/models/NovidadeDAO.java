package models;

import com.google.inject.Inject;
import play.db.jpa.JPAApi;

import javax.inject.Singleton;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

/**
 * DAO da Novidade
 */
@Singleton
public class NovidadeDAO {

    private JPAApi jpaAPI;

    @Inject
    public NovidadeDAO(JPAApi jpaAPI) {
        this.jpaAPI = jpaAPI;
    }

    public List<Novidade> find(UUID cidadao, int pagina, int tamanhoPagina) {
        TypedQuery<Novidade> query = jpaAPI.em().createQuery(
                "SELECT n FROM Novidade n ORDER BY criadaEm desc", Novidade.class)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina);
        return query.getResultList();
    }
}
