package models;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Cidade {

    @Id
	private Long id;

    private String nome;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable
    private List<Cidade> similares;

    public Cidade(String nome) {
        this.nome = nome;
        similares = new LinkedList<Cidade>();
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

    public List<Cidade> getSimilares() {
        return similares;
    }

    public void setSimilares(List<Cidade> similares) {
        this.similares = similares;
    }
}
