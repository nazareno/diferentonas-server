package controllers;

import models.Cidadao;
import models.CidadaoDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.UUID;

import static play.libs.Json.toJson;

/**
 * Encapsula operações relacionadas com usuários do sistema.
 */
@Singleton
@Security.Authenticated(Secured.class)
public class CidadaoController extends Controller {

    @Inject
    private CidadaoDAO daoCidadao;

    @Transactional(readOnly = true)
    public Result getCidadaos(String query, int pagina, int tamanhoPagina) {
        if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
            return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. " +
                    "Tamannho de página deve ser menor ou igual a 500.");
        }

        if(query.isEmpty() || query.length() > 50){
            return badRequest("Argumento da busca não pode ser vazio ou superior a 50 caracteres.");
        }

        List<Cidadao> cidadaos = daoCidadao.getCidadaos(query, pagina, tamanhoPagina);
        return ok(toJson(cidadaos));
    }

    @Transactional
    protected boolean removeCidadaoPorLogin(String login) {
        Cidadao cidadao = daoCidadao.findByLogin(login);
        if(cidadao != null){
            daoCidadao.remove(cidadao);
            return true;
        }
        return false;
    }

    @Transactional
    public void criaCidadao(Cidadao c) {
        // TODO login deveria ser necessariamente um email.
        daoCidadao.saveAndUpdate(c);
    }

    @Transactional
    public Result promoveAFuncionario(String id, String ministerio) {
        if(id.isEmpty() || id.length() > 50){
            return badRequest("Id do usuário não pode ser vazio ou superior a 50 caracteres.");
        }
        Cidadao cidadao = daoCidadao.find(UUID.fromString(id));
        cidadao.setFuncionario(true);
        cidadao.setMinisterioDeAfiliacao(ministerio);
        return ok(toJson(cidadao));
    }

    @Transactional
    public Result removePapelDeFuncionario(String id) {
        if(id.isEmpty() || id.length() > 50){
            return badRequest("Id do usuário não pode ser vazio ou superior a 50 caracteres.");
        }
        Cidadao cidadao = daoCidadao.find(UUID.fromString(id));
        if(! cidadao.isFuncionario()){
            return status(CONFLICT, "Usuário não é funcionário.");
        }
        cidadao.setFuncionario(false);
        cidadao.setMinisterioDeAfiliacao(null);
        return ok(toJson(cidadao));
    }

}
