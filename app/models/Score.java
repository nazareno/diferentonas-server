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
	private Float score;
	private Float total;
	private Float media;
	
	
	public Score() {
	}

	public Score(String tema, Float score, Float total, Float media) {
		super();
		this.tema = tema;
		this.score = score;
		this.total = total;
		this.media = media;
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


	public Float getScore() {
		return score;
	}


	public void setScore(Float score) {
		this.score = score;
	}
	public Float getTotal() {
		return total;
	}
	public void setTotal(Float total) {
		this.total = total;
	}
	public Float getMedia() {
		return media;
	}
	public void setMedia(Float media) {
		this.media = media;
	}
	
	
}
