package models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import play.data.validation.Constraints;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Opinião de um cidadão sobre uma iniciativa de uma cidade.
 */
@Entity
public class Opiniao implements Serializable {

	private static final long serialVersionUID = 237406949191520501L;

	public static final String TABLE = "Opiniao";

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "iniciativa")
    private Iniciativa iniciativa;

    @Column(length = 1000)
    @Constraints.MaxLength(value = 1000,message = "Opiniões devem ter 1000 caracteres ou menos")
    @Constraints.Required(message = "Campo necessário")
    @Constraints.MinLength(1)
    private String conteudo;

    @Constraints.Required(message = "Campo necessário")
    @Constraints.Pattern("^(bomba|coracao|coracao_partido)$")
    private String tipo;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy HH:mm", timezone="America/Recife")
    private Date criadaEm;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Cidadao autor;

    public Opiniao(){
        this.criadaEm = new Date();
    }
    
    public Opiniao(Iniciativa iniciativa, String conteudo, String tipo,
			Cidadao autor) {
		this();
		this.iniciativa = iniciativa;
		this.conteudo = conteudo;
		this.tipo = tipo;
		this.autor = autor;
	}



	public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Iniciativa getIniciativa() {
        return iniciativa;
    }

    public void setIniciativa(Iniciativa iniciativa) {
        this.iniciativa = iniciativa;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Date criadaEm) {
        this.criadaEm = criadaEm;
    }

    public Cidadao getAutor() {
        return autor;
    }

    public void setAutor(Cidadao autor) {
        this.autor = autor;
    }
    
    
}
