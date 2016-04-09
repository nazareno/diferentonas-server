package controllers;

import models.Cidade;
import play.mvc.*;
import play.db.jpa.*;

import java.security.cert.CollectionCertStoreParameters;
import java.util.Collections;
import java.util.List;

import static play.libs.Json.*;

public class CidadeController extends Controller {

    public Result index() {
        return ok("Olar.");
    }

    @Transactional(readOnly = true)
    public Result getSimilares(Long id) {
        Cidade cidade = JPA.em().find(Cidade.class, id);

        return ok(toJson(cidade == null? Collections.emptyList() : cidade.getSimilares()));
    }


}
