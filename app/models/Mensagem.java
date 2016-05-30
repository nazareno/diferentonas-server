/**
 * 
 */
package models;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


/**
 * @author ricardoas
 *
 */
@Entity
public class Mensagem implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1169792076018944039L;

	public static final String TABLE = "Mensagem";
	
	@Id
	@GeneratedValue( generator="uuid2" )
	@GenericGenerator(
			name="uuid2",
			strategy="org.hibernate.id.UUIDGenerator",
			parameters = {
					@Parameter(
							name="uuid_gen_strategy_class",
							value="org.hibernate.id.uuid.CustomVersionOneStrategy"
					)
			}
	)
	private UUID id;
	private String conteudo;
	private String titulo;
	private String autor;

	
	public Mensagem() {
	}
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	@Override
	public String toString() {
		return "Mensagem [id=" + id + ", conteudo=" + conteudo + ", titulo="
				+ titulo + ", autor=" + autor + "]";
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
		Mensagem other = (Mensagem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
