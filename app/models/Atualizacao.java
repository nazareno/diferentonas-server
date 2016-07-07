package models;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Atualizacao implements Serializable {

	private enum Status {
		ATUALIZADO, ATUALIZANDO, DESATUALIZADO, INDEFINIDO;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1889826274385838076L;
	@Id
	@JsonIgnore
	private Long id;

	private String proxima;

	@Enumerated(EnumType.STRING)
	private Status status;

	private String ultima;

	public Atualizacao() {
		this.id = 0L;
		this.ultima = "";
		this.proxima = "";
		this.status = Status.INDEFINIDO;
	}

	public void atualiza(List<String> atualizacoesDisponiveis) {
		if (atualizacoesDisponiveis.isEmpty()) {
			this.status = Status.INDEFINIDO;
		} else if (atualizacoesDisponiveis.get(0).equals(this.ultima)) {
			this.status = Status.ATUALIZADO;
		} else {
			this.status = Status.DESATUALIZADO;
			this.proxima = atualizacoesDisponiveis.get(0);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProxima() {
		return proxima;
	}

	public void setProxima(String proxima) {
		this.proxima = proxima;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getUltima() {
		return ultima;
	}

	public void setUltima(String ultima) {
		this.ultima = ultima;
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
		Atualizacao other = (Atualizacao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Atualizacao [id=" + id + ", ultima=" + ultima + ", proxima="
				+ proxima + ", status=" + status + "]";
	}

	public boolean estaAtualizando() {
		return Status.ATUALIZANDO.equals(status);
	}

	public boolean estaDesatualizado() {
		return Status.DESATUALIZADO.equals(status);
	}

	public void inicia() {
		this.status = Status.ATUALIZANDO;
	}

	public void finaliza(boolean comErro) {
		if(comErro){
			this.status = Status.INDEFINIDO;
		}else{
			this.ultima = this.proxima;
			this.status = Status.ATUALIZADO;
		}
	}

}