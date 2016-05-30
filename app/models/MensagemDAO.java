package models;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import play.db.jpa.JPA;
import play.db.jpa.JPAApi;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MensagemDAO {
	
	private JPAApi jpaAPI;

	@Inject
	public MensagemDAO(JPAApi jpaAPI) {
		this.jpaAPI = jpaAPI;
	}
	
    /**
     * Create an Mensagem
     *
     * @param Mensagem model
     *
     * @return Mensagem
     */
    public Mensagem create (Mensagem model) {
        EntityManager entityManager = jpaAPI.em();
		entityManager.persist(model);
        entityManager.flush();
        return model;
    }

    /**
     * Find an Mensagem by id
     *
     * @param Integer id
     *
     * @return Mensagem
     */
    public Mensagem find(Long id) {
        return jpaAPI.em().find(Mensagem.class, id);
    }

    /**
     * Update an Mensagem
     *
     * @param Mensagem model
     *
     * @return Mensagem
     */
    public Mensagem update(Mensagem model) {
        return JPA.em().merge(model);
    }

    /**
     * Delete an Mensagem by id
     *
     * @param Integer id
     */
    public void delete(Long id) {
        Mensagem model = JPA.em().getReference(Mensagem.class, id);
        JPA.em().remove(model);
    }

    /**
     * Get all Mensagems
     *
     * @return List<Mensagem>
     */
    public List<Mensagem> all() {
        return jpaAPI.em().createQuery("SELECT m FROM " + Mensagem.TABLE + " m ORDER BY id", Mensagem.class).getResultList();
    }

    /**
     * Get the page of Mensagems
     *
     * @param Integer page
     * @param Integer size
     *
     * @return List<Mensagem>
     */
    public List<Mensagem> paginate(Integer page, Integer size) {
    	EntityManager em = jpaAPI.em();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		
		CriteriaQuery<Mensagem> query = builder.createQuery(Mensagem.class);
		Root<Mensagem> m = query.from(Mensagem.class);
		query.orderBy(builder.desc(m.get("id")));
		
    	return em.createQuery(query).setFirstResult(page*size).setMaxResults(size).getResultList();
    }

    /**
     * Get the number of total row
     *
     * @return Long
     */
    public Long count() {
        return JPA.em().createQuery("SELECT count(m) FROM " + Mensagem.TABLE + " m", Long.class).getSingleResult();
    }
}