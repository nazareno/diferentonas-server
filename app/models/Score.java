package models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Score implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 392920310936985766L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String tema;
	private Double score;
	
	
	public Score() {
	}


	public Score(String tema, Double score) {
		this.tema = tema;
		this.score = score;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getTema() {
		return tema;
	}


	public void setTema(String tema) {
		this.tema = tema;
	}


	public Double getScore() {
		return score;
	}


	public void setScore(Double score) {
		this.score = score;
	}
}
