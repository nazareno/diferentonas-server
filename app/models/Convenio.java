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

    private static final long serialVersionUID = 811023123992501763L;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Convenio other = (Convenio) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
