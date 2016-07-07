package models;

import java.util.List;

import javax.persistence.TypedQuery;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CidadeDAO {
	
    private JPAApi jpaAPI;

    @Inject
    public CidadeDAO(JPAApi jpaAPI) {
        this.jpaAPI = jpaAPI;
    }

    /**
     * Find an Cidade by id
     *
     * @param Integer id
     * @return Cidade
     */
    public Cidade find(Long id) {
        return jpaAPI.em().find(Cidade.class, id);
    }

    /**
     * Find an Cidade by id
     *
     * @param Integer id
     * @return Cidade
     */
    public Cidade findComIniciativas(Long id) {
        return jpaAPI.em().createNamedQuery("findComIniciativas", Cidade.class)
        		.setParameter("paramId", id).getSingleResult();
    }

    /**
     * Update an Cidade
     *
     * @param Cidade model
     * @return Cidade
     */
    public void save(Cidade model) {
        jpaAPI.em().persist(model);
    }

    /**
     * Get all Cidades
     *
     * @return List<Cidade>
     */
    public List<Cidade> all() {
        return jpaAPI.em().createQuery("SELECT m FROM " + Cidade.TABLE + " m ORDER BY id", Cidade.class).getResultList();
    }

    /**
     * Retorna uma página de novidades ordenada da mais recente pra mais antiga.
     * 
     * @param id ID da {@link Cidade}
     * @param pagina
     * @param tamanhoDaPagina
     * @return Uma lista de {@link Novidade}s.
     */
    public List<Novidade> getNovidades(Long id, int pagina, int tamanhoDaPagina) {
        TypedQuery<Novidade> query = jpaAPI.em()
                .createQuery("SELECT n "
                        + "FROM Novidade n "
                        + "WHERE n.cidade.id = :cidade_id "
                        + "ORDER BY n.criadaEm DESC", Novidade.class)
                .setParameter("cidade_id", id)
                .setFirstResult(pagina * tamanhoDaPagina)
                .setMaxResults(tamanhoDaPagina);
        return query.getResultList();
    }

}