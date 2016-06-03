package models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Um acontecimento (visto ou não) na linha do tempo de um cidadão.
 */
@Entity
public class Novidade implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6390884039407731417L;

	public static final String TIPO_OPINIAO = "opiniao";

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy,HH:mm", timezone="BRT")
    private Date criadaEm;

    private String tipo;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "iniciativa")
//    private Iniciativa iniciativa;

    private Opiniao opiniao;

    /**
     * Para o JPA
     */
    public Novidade() {
        this.criadaEm = new Date();
    }

    public Novidade(Opiniao opiniao) {
        this();
        this.opiniao = opiniao;
        this.tipo = TIPO_OPINIAO;
//        this.iniciativa = opiniao.getIniciativa();
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Date criadaEm) {
        this.criadaEm = criadaEm;
    }

    public String getTipo() {
        return tipo;
    }

//    public Iniciativa getIniciativa() {
//        return iniciativa;
//    }
//
//    public void setIniciativa(Iniciativa iniciativa) {
//        this.iniciativa = iniciativa;
//    }

	public Opiniao getOpiniao() {
		return opiniao;
	}

	public void setOpiniao(Opiniao opiniao) {
		this.opiniao = opiniao;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
    
    
}
