package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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

    public Cidadao saveAndUpdate(Cidadao cidadao) {
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
    	return list.isEmpty()? null: list.get(0);
    }
    
    public List<Novidade> getNovidadesRecentes(UUID cidadaoId, int pagina, int tamanhoDaPagina) {
        TypedQuery<Novidade> query = jpaAPI.em()
                .createQuery("SELECT n "
                        + "FROM Cidadao c "
                        + "JOIN c.iniciativasAcompanhadas as acompanhadas "
                        + "JOIN acompanhadas.novidades as n "
                        + "WHERE c.id = :cidadao_id AND n.tipo = 'NOVA_OPINIAO' "
                        + "ORDER BY n.criadaEm DESC", Novidade.class)
                .setParameter("cidadao_id", cidadaoId)
                .setFirstResult(pagina * tamanhoDaPagina)
                .setMaxResults(tamanhoDaPagina);
        return query.getResultList();
    }

    public List<Cidadao> getCidadaos(String queryString, int pagina, int tamanhoDaPagina) {
        TypedQuery<Cidadao> query = jpaAPI.em()
                .createQuery("SELECT c "
                        + "FROM Cidadao c "
                        + "WHERE c.login LIKE :query_string "
                        + "ORDER BY c.login", Cidadao.class)
                .setParameter("query_string", "%" + queryString + "%")
                .setFirstResult(pagina * tamanhoDaPagina)
                .setMaxResults(tamanhoDaPagina);
        return query.getResultList();
    }

    public void remove(Cidadao cidadao) {
        jpaAPI.em().remove(cidadao);
    }

    public Cidadao findByToken(String token) {
    	List<Cidadao> list = jpaAPI.em().createQuery("from Cidadao where token = :paramToken", Cidadao.class).setParameter("paramToken", token).getResultList();
    	return list.isEmpty()? null: list.get(0);
	}

    public List<Cidadao> getFuncionarios(String queryString, int pagina, int tamanhoDaPagina) {
        TypedQuery<Cidadao> query = jpaAPI.em()
                .createQuery("SELECT c "
                        + "FROM Cidadao c "
                        + "WHERE c.login LIKE :query_string "
                        + "AND c.funcionario = true "
                        + "ORDER BY c.login", Cidadao.class)
                .setParameter("query_string", "%" + queryString + "%")
                .setFirstResult(pagina * tamanhoDaPagina)
                .setMaxResults(tamanhoDaPagina);
        return query.getResultList();
    }
}