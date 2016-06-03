package models;

import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;

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
        TypedQuery<Novidade> query = jpaAPI.em()
                .createQuery("SELECT n "
                		+ "FROM Cidadao c "
                		+ "JOIN c.iniciativasAcompanhadas as acompanhadas "
                		+ "JOIN acompanhadas.novidades as n "
                		+ "WHERE c.id = :cidadao_id " 
                		+ "ORDER BY n.criadaEm DESC", Novidade.class)
                .setParameter("cidadao_id", cidadao)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina);
        return query.getResultList();
    }
}
