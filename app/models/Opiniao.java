package models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import play.data.validation.Constraints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @JsonIgnore
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
    
    @OneToOne(mappedBy="opiniao", targetEntity=Novidade.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JsonIgnore
    private Novidade novidade;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnore
    private Set<Cidadao> apoiadores;

    public Opiniao(){
        this.criadaEm = new Date();
        this.apoiadores = new HashSet<Cidadao>();
    }
    
    public Opiniao(String conteudo, String tipo) {
		this();
		this.conteudo = conteudo;
		this.tipo = tipo;
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

	public Novidade getNovidade() {
		return novidade;
	}

	public void setNovidade(Novidade novidade) {
		this.novidade = novidade;
	}

	public Set<Cidadao> getApoiadores() {
		return apoiadores;
	}

	public void setApoiadores(Set<Cidadao> apoiadores) {
		this.apoiadores = apoiadores;
	}
    
	public boolean addApoiador(Cidadao apoiador) {
		return this.apoiadores.add(apoiador);
	}
    
	public boolean removeApoiador(Cidadao apoiador) {
		return this.apoiadores.remove(apoiador);
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
		Opiniao other = (Opiniao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean ehApoiada(Cidadao cidadao) {
		System.out.println(this.apoiadores);
		return this.apoiadores.contains(cidadao);
	}
    
}
