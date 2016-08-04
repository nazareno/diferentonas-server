package models;

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

	public String getFolder() {
		return folder;
	}
	
	public void inicia(){
		Atualizacao status = find();
		status.inicia();
		EntityManager em = jpaAPI.em();
		em.persist(status);
		em.flush();
		em.refresh(status);
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