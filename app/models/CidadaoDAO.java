package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import play.db.jpa.JPAApi;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CidadaoDAO {
	
    private JPAApi jpaAPI;

    @Inject
    public CidadaoDAO(JPAApi jpaAPI) {
        this.jpaAPI = jpaAPI;
    }

    public Cidadao create(Cidadao cidadao) {
        EntityManager em = jpaAPI.em();
		em.persist(cidadao);
        em.flush();
        em.refresh(cidadao);
        return cidadao;
    }

    public Cidadao find(UUID id) {
        return jpaAPI.em().find(Cidadao.class, id);
    }

    
    public Cidadao findByLogin(String login) {
    	List<Cidadao> list = jpaAPI.em().createQuery("from Cidadao where login = :paramLogin", Cidadao.class).setParameter("paramLogin", login).getResultList();
//    	List<Cidadao> list = jpaAPI.em().createNamedQuery("findByLogin", Cidadao.class)
//		.setParameter("paramlogin", login).getResultList();
    	return list.isEmpty()? null: list.get(0);
    }
}