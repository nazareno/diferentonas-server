package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class,
//        property = "@id")
@Entity
public class Cidade {

    @Id
	private Long id;
    private String nome;
    private String uf;
    private Double idhm;
    private Double idhmRenda;
    private Double idhmLongevidade;
    private Double idhmEducacao;
    private Long populacao;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable
    private List<Cidade> similares;
    
    public Cidade() {
        this.similares = new LinkedList<Cidade>();
	}

	public Cidade(Long id, String nome, String uf, Double idhm, Double idhmRenda,
			Double idhmLongevidade, Double idhmEducacao, Long populacao) {
		this.id = id;
		this.nome = nome;
		this.uf = uf;
		this.idhm = idhm;
		this.idhmRenda = idhmRenda;
		this.idhmLongevidade = idhmLongevidade;
		this.idhmEducacao = idhmEducacao;
		this.populacao = populacao;
        this.similares = new LinkedList<Cidade>();
	}

	public Cidade(String nome) {
        this.nome = nome;
        this.similares = new LinkedList<Cidade>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public Double getIdhm() {
		return idhm;
	}

	public void setIdhm(Double idhm) {
		this.idhm = idhm;
	}

	public Double getIdhmRenda() {
		return idhmRenda;
	}

	public void setIdhmRenda(Double idhmRenda) {
		this.idhmRenda = idhmRenda;
	}

	public Double getIdhmLongevidade() {
		return idhmLongevidade;
	}

	public void setIdhmLongevidade(Double idhmLongevidade) {
		this.idhmLongevidade = idhmLongevidade;
	}

	public Double getIdhmEducacao() {
		return idhmEducacao;
	}

	public void setIdhmEducacao(Double idhmEducacao) {
		this.idhmEducacao = idhmEducacao;
	}

	public Long getPopulacao() {
		return populacao;
	}

	public void setPopulacao(Long populacao) {
		this.populacao = populacao;
	}

	public List<Cidade> getSimilares() {
        return similares;
    }

    public void setSimilares(List<Cidade> similares) {
        this.similares = similares;
    }

	@Override
	public String toString() {
		return "Cidade [id=" + id + ", nome=" + nome + ", uf=" + uf + ", idhm="
				+ idhm + ", idhmRenda=" + idhmRenda + ", idhmLongevidade="
				+ idhmLongevidade + ", idhmEducacao=" + idhmEducacao
				+ ", populacao=" + populacao + ", similares=" + similares + "]";
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
		Cidade other = (Cidade) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
    
    
}
