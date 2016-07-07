package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

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
     * Cria uma nova mensagem
     *
     * @param Mensagem nova mensagem
     *
     * @return Mensagem mensagem com ID gerado pelo banco de dados.
     */
    public Mensagem create (Mensagem model) {
        EntityManager entityManager = jpaAPI.em();
		entityManager.persist(model);
        entityManager.flush();
        return model;
    }

    /**
     * Procura mensagem pelo {@link UUID}
     *
     * @param UUID id identificador da mensagem.
     *
     * @return {@link Mensagem} referente ao id ou <code>null</code> caso não exista mensagem associada a esse ID.
     */
    public Mensagem find(UUID id) {
        return jpaAPI.em().find(Mensagem.class, id);
    }

    /**
     * Atualiza uma mensagem no banco de dados.
     *
     * @param mensagem atualizada
     *
     * @return {@link Mensagem} atualizada
     */
    public Mensagem update(Mensagem mensagem) {
        return jpaAPI.em().merge(mensagem);
    }

    /**
     * Remove uma mensagem do banco de dados.
     *
     * @param mensagem a remover.
     */
    public void delete(Mensagem mensagem) {
        jpaAPI.em().remove(mensagem);
    }

    /**
     * Retorna todas as mensagens no banco de dados ordenada da mais recente para mais antiga
     *
     * @return List<Mensagem>
     */
    public List<Mensagem> all() {
    	
        return jpaAPI.em().createQuery("SELECT m FROM " + Mensagem.TABLE + " m ORDER BY id DESC", Mensagem.class).getResultList();
    }

    /**
     * Get the page of Mensagems
     *
     * @param Integer page
     * @param Integer size
     *
     * @return List<Mensagem>
     */
    public List<Mensagem> paginate(Integer pagina, Integer quantidade) {
    	
		return jpaAPI.em().createQuery("SELECT m FROM " + Mensagem.TABLE + " m ORDER BY m.criadaEm DESC", Mensagem.class)
				.setFirstResult(pagina * quantidade).setMaxResults(quantidade)
				.getResultList();
    }

    /**
     * Retorna a quantidade de mensagens no banco de dados. 
     *
     * @return o número de mensagens.
     */
    public Long count() {
        return jpaAPI.em().createQuery("SELECT count(m) FROM " + Mensagem.TABLE + " m", Long.class).getSingleResult();
    }

	/**
	 * FIXME select * from mensagem m where m.id > id;
	 * 
	 * @param idDaUltimaLida
	 * @return
	 */
	public List<Mensagem> findMaisRecentesQue(UUID idDaUltimaLida) {
		
        Mensagem ultimaLida = find(idDaUltimaLida);
		return jpaAPI.em().createQuery("SELECT m FROM " + Mensagem.TABLE + " m WHERE m.criadaEm > :dataDaUltimaLida ORDER BY criadaEm desc", Mensagem.class)
                .setParameter("dataDaUltimaLida", ultimaLida.getCriadaEm()).getResultList();
	}
}