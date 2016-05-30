package controllers;

import static play.libs.Json.toJson;
import models.IniciativaDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class IniciativaController extends Controller {
	
	private IniciativaDAO dao;

	@Inject
	public IniciativaController(IniciativaDAO dao) {
		this.dao = dao;
	}
	
    @Transactional(readOnly = true)
    public Result get(Long id) {
    	return ok(toJson(dao.find(id)));
    }

    @Transactional(readOnly = true)
    public Result similares(Long id, Long quantidade) {
    	return ok(toJson(dao.findSimilares(id, quantidade)));
    }

}
