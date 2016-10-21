package models;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import models.Atualizacao.Status;
import play.Configuration;
import play.db.jpa.JPAApi;
import play.libs.ws.WSClient;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AtualizacaoDAO {
	
    private JPAApi jpaAPI;
	private String folder;

    @Inject
    public AtualizacaoDAO(JPAApi jpaAPI, Configuration configuration, WSClient client) {
        this.jpaAPI = jpaAPI;
		this.folder = configuration.getString("diferentonas.data", "dist/data");
    }

    public List<Atualizacao> getMaisRecentes() {
    	EntityManager em = jpaAPI.em();
    	TypedQuery<Atualizacao> query = em.createQuery("FROM Atualizacao a WHERE a.status != 'DISPONIVEL' ORDER BY a.dataDePublicacao DESC, a.servidorResponsavel DESC", Atualizacao.class);
    	query.setMaxResults(10);
		List<Atualizacao> list = query.getResultList();
    	return list;
    }

	public String getFolder() {
		return folder;
	}
	
	public void vota(String dataDePublicacao, String servidorResponsavel){
		jpaAPI.em().persist(new Atualizacao(dataDePublicacao, servidorResponsavel, new Date()));
	}
	
	public Atualizacao getLider(String dataVotada) {
    	EntityManager em = jpaAPI.em();
    	TypedQuery<Atualizacao> query = em.createQuery("FROM Atualizacao a WHERE a.status = 'DISPONIVEL' AND a.dataDePublicacao = :dataVotada ORDER BY a.servidorResponsavel DESC, a.horaDaAtualizacao DESC", Atualizacao.class);
    	query.setParameter("dataVotada", dataVotada);
    	query.setMaxResults(1);
		List<Atualizacao> list = query.getResultList();
    	return list.isEmpty()? null: list.get(0);
	}
	
	
	public void inicia(String dataDePublicacao, String servidorResponsavel){
		
		Atualizacao ultimaRealizada = getUltimaRealizada();
		Atualizacao ultimaPendente = getUltimaPendente();
		if(ultimaRealizada != null && ultimaPendente != null && !ultimaRealizada.getDataDePublicacao().equals(ultimaPendente.getDataDePublicacao())){
			jpaAPI.em().persist(new Atualizacao(ultimaPendente.getDataDePublicacao(), ultimaPendente.getServidorResponsavel(), new Date(), Status.ABANDONADA));
		}
		
		jpaAPI.em().persist(new Atualizacao(dataDePublicacao, servidorResponsavel, new Date(), Status.ATUALIZANDO));
	}

	public void finaliza(String dataVotada,
			String identificadorUnicoDoServidor, boolean comErro) {
		
		jpaAPI.em().persist(new Atualizacao(dataVotada, identificadorUnicoDoServidor, new Date(), comErro?Status.SEM_SUCESSO:Status.ATUALIZADO));
	}

	public Atualizacao getUltimaRealizada() {
    	EntityManager em = jpaAPI.em();
    	TypedQuery<Atualizacao> query = em.createQuery("FROM Atualizacao a WHERE a.status = 'ATUALIZADO' ORDER BY a.dataDePublicacao DESC, a.servidorResponsavel DESC", Atualizacao.class);
    	query.setMaxResults(1);
    	List<Atualizacao> list = query.getResultList();
    	return list.isEmpty()? null: list.get(0);
	}
	
	public Atualizacao getUltimaPendente() {
    	EntityManager em = jpaAPI.em();
    	TypedQuery<Atualizacao> query = em.createQuery("FROM Atualizacao a WHERE a.status = 'ATUALIZANDO' ORDER BY a.dataDePublicacao DESC, a.servidorResponsavel DESC", Atualizacao.class);
    	query.setMaxResults(1);
		List<Atualizacao> list = query.getResultList();
    	return list.isEmpty()? null: list.get(0);
	}
	
}