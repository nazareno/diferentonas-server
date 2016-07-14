package models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Cidadao implements Serializable {

	private static final long serialVersionUID = -8912114826094647736L;

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	private UUID id;
	private String login;
	private String token;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Iniciativa> iniciativasAcompanhadas;

	// se o cidadao é funcionário de um ministério
	private boolean funcionario;
	// o ministério, caso seja funcionaário
	private String ministerioDeAfiliacao;


	public Cidadao() {
		this.iniciativasAcompanhadas = new HashSet<>();
	}
	
	public Cidadao(String login) {
		this();
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
	
	public Set<Iniciativa> getIniciativasAcompanhadas() {
		return iniciativasAcompanhadas;
	}

	public void setIniciativasAcompanhadas(Set<Iniciativa> iniciativasAcompanhadas) {
		this.iniciativasAcompanhadas = iniciativasAcompanhadas;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

	public boolean inscreverEm(Iniciativa iniciativa) {
		return this.iniciativasAcompanhadas.add(iniciativa);
	}

	public boolean desinscreverDe(Iniciativa iniciativa) {
		return this.iniciativasAcompanhadas.remove(iniciativa);
	}

	public boolean isInscritoEm(Iniciativa iniciativa) {
		return this.iniciativasAcompanhadas.contains(iniciativa);
	}

	public boolean isFuncionario() {
		return funcionario;
	}

	public String getMinisterioDeAfiliacao() {
		return ministerioDeAfiliacao;
	}

	public void setMinisterioDeAfiliacao(String ministerioDeAfiliacao) {
		this.ministerioDeAfiliacao = ministerioDeAfiliacao;
	}

	public void setFuncionario(boolean funcionario) {
		this.funcionario = funcionario;
	}
}
