package models;

import java.io.IOException;

import javax.persistence.EntityManager;

import play.Configuration;
import play.Logger;
import play.db.jpa.JPAApi;
import util.DadosUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AtualizacaoDAO {
	
    private JPAApi jpaAPI;
	private String folder;

    @Inject
    public AtualizacaoDAO(JPAApi jpaAPI, Configuration configuration) {
        this.jpaAPI = jpaAPI;
		this.folder = configuration.getString("diferentonas.data", "dist/data");
    }

    public void create() {
    	EntityManager em = jpaAPI.em();
		if(em.find(Atualizacao.class, 0L) == null){
    		em.persist(new Atualizacao());
    	}
    }

    public Atualizacao verifica(){
    	Atualizacao atualizacao = find();
    	if(atualizacao.estaAtualizando()){
    		return atualizacao;
    	}
    	atualizacao.atualiza(DadosUtil.listaAtualizacoes(folder));
    	jpaAPI.em().persist(atualizacao);
    	jpaAPI.em().flush();
    	jpaAPI.em().refresh(atualizacao);
		return atualizacao;
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
		Logger.debug(status.toString());
		status.finaliza(comErro);
		Logger.debug(status.toString());
		EntityManager em = jpaAPI.em();
		Logger.debug(status.toString());
		em.persist(status);
		Logger.debug(status.toString());
		em.flush();
		Logger.debug(status.toString());
		em.refresh(status);
		Logger.debug(status.toString());
	}
}