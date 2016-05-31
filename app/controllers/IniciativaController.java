package controllers;

import static play.libs.Json.toJson;
import models.Cidadao;
import models.CidadaoDAO;
import models.IniciativaDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class IniciativaController extends Controller {
	
	private IniciativaDAO iniciativaDAO;
	private CidadaoDAO cidadaoDAO;

	@Inject
	public IniciativaController(IniciativaDAO iniciativaDAO, CidadaoDAO cidadaoDAO) {
		this.iniciativaDAO = iniciativaDAO;
		this.cidadaoDAO = cidadaoDAO;
	}
	
    @Transactional(readOnly = true)
    public Result get(Long id) {
    	return ok(toJson(iniciativaDAO.find(id)));
    }

    @Transactional(readOnly = true)
    public Result similares(Long id, Long quantidade) {
    	return ok(toJson(iniciativaDAO.findSimilares(id, quantidade)));
    }
    
    /**
     * Só funciona dentro de uma transação. Use enquanto não houver lógica de login.
     * 
     * 
     * @return o Cidadao da sessão
     */
    private Cidadao getCidadaoAtual(String accessToken){
    	// não faz nada com o access-token
    	return cidadaoDAO.findByLogin("admin");
    }
    
}
