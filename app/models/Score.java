package models;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Representa o quão diferente uma cidade é em uma determinada área.
 * O desvio do normal é representado pelo valor do score. O score é o z-score
 * desse município em relação aos seus semelhantes. Assim, positivo significa acima
 * da média; negativo abaixo. A unidade é em desvios padrao.
 *
 * A área do Score corresponde a uma função orçamentária do Governo Federal.
 * Por exemplo, Riacho da Carreira pode se destacar em Segurança: caso tenha um
 * score com valor 1, ela recebeu 1 desvio padrão mais recursos nessa área que
 * suas cidades semelhantes.
 */
@Entity
public class Score implements Serializable, Comparable<Score>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 392920310936985766L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String area;
	private Float valorScore;
	private Float repasseTotal;
	private Float mediaCidadesSemelhantes;
	private Float desvioCidadesSemelhantes;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade")
    @JsonIgnore
    private Cidade cidade;
	
    
	public Cidade getCidade() {
		return cidade;
	}

	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}

	public Score() {
	}

	public Score(String area, Float valor, Float total, Float media, Float desvio) {
		super();
		this.area = area;
		this.valorScore = valor;
		this.repasseTotal = total;
		this.mediaCidadesSemelhantes = media;
		this.desvioCidadesSemelhantes = desvio;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getArea() {
		return area;
	}


	public void setArea(String tema) {
		this.area = tema;
	}


	public Float getValorScore() {
		return valorScore;
	}


	public void setValorScore(Float score) {
		this.valorScore = score;
	}
	public Float getRepasseTotal() {
		return repasseTotal;
	}
	public void setRepasseTotal(Float total) {
		this.repasseTotal = total;
	}
	public Float getMediaCidadesSemelhantes() {
		return mediaCidadesSemelhantes;
	}
	public void setMediaCidadesSemelhantes(Float media) {
		this.mediaCidadesSemelhantes = media;
	}
	
	public Float getDesvioCidadesSemelhantes() {
		return desvioCidadesSemelhantes;
	}

	public void setDesvioCidadesSemelhantes(Float desvioCidadesSemelhantes) {
		this.desvioCidadesSemelhantes = desvioCidadesSemelhantes;
	}

	public void atualiza(Score scoreAtualizado) {
		this.area = scoreAtualizado.area;
		this.valorScore = scoreAtualizado.valorScore;
		this.repasseTotal = scoreAtualizado.repasseTotal;
		this.mediaCidadesSemelhantes = scoreAtualizado.mediaCidadesSemelhantes;
		this.desvioCidadesSemelhantes = scoreAtualizado.desvioCidadesSemelhantes;
	}

	@Override
	public int compareTo(Score o) {
		return Double.compare(Math.floor(this.valorScore), Math.floor(o.valorScore));
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
		Score other = (Score) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
}
