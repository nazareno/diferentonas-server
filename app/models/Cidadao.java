package models;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@NamedQueries({ @NamedQuery(name = "findByLogin", query = "SELECT c FROM Cidadao c WHERE c.login = :paramlogin") })
public class Cidadao implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8912114826094647736L;
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
	private String login;
	
	public Cidadao() {
		// TODO Auto-generated constructor stub
	}
	
	public Cidadao(String login) {
		this.login = login;
	}

	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
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
		Cidadao other = (Cidadao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Cidadao [id=" + id + ", nome=" + login + "]";
	}
}
