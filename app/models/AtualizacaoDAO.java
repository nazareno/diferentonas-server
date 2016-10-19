package models;

import java.util.Arrays;

import javax.persistence.EntityManager;

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

    public void create() {
    	EntityManager em = jpaAPI.em();
		if(em.find(Atualizacao.class, 0L) == null){
    		em.persist(new Atualizacao());
    	}
    }

    public Atualizacao find() {
    	return jpaAPI.em().find(Atualizacao.class, 0L);
    }

    public Atualizacao atualiza(String... datas) {
    	EntityManager em = jpaAPI.em();
		Atualizacao atualizacao = em.find(Atualizacao.class, 0L);
		atualizacao.atualiza(Arrays.asList(datas));
		em.persist(atualizacao);
		em.flush();
		em.refresh(atualizacao);
		return atualizacao;
    }

	public String getFolder() {
		return folder;
	}
	
	public boolean inicia(){
		EntityManager em = jpaAPI.em();
		Atualizacao status = em.find(Atualizacao.class, 0L);
		boolean iniciado = status.inicia();
		em.persist(status);
		em.flush();
		em.refresh(status);
		return iniciado;
	}
	
	public void finaliza(boolean comErro){
		Atualizacao status = find();
		status.finaliza(comErro);
		EntityManager em = jpaAPI.em();
		em.persist(status);
		em.flush();
		em.refresh(status);
	}
}