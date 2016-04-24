package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by nazareno on 24/04/16.
 */
@Entity
public class Convenio implements Serializable {

    @Id
    private Long id;
    private Integer ano;
    private String situacao;
    private String orgaoSuperior;
    private String programa;
    private Float repasse;
    private String objeto;

    public Convenio(){

    }

    public Convenio(long id, int ano, String situacao, String orgaoSuperior, String programa, float repasse, String objeto) {
        this.id = id;
        this.ano = ano;
        this.situacao = situacao;
        this.orgaoSuperior = orgaoSuperior;
        this.programa = programa;
        this.repasse = repasse;
        this.objeto = objeto;
    }


    public long getId() {
        return id;
    }

    public int getAno() {
        return ano;
    }

    public String getSituacao() {
        return situacao;
    }

    public String getOrgaoSuperior() {
        return orgaoSuperior;
    }

    public String getPrograma() {
        return programa;
    }

    public float getRepasse() {
        return repasse;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public void setOrgaoSuperior(String orgaoSuperior) {
        this.orgaoSuperior = orgaoSuperior;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public void setRepasse(float repasse) {
        this.repasse = repasse;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }
}
