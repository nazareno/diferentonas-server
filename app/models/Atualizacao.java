package models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class Atualizacao implements Serializable {
	
	enum Status{
		DISPONIVEL,
		ATUALIZANDO,
		ATUALIZADO,
		SEM_SUCESSO, 
		ABANDONADA
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1889826274385838076L;
	@Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
	private UUID id;

	private String dataDePublicacao;

	private String servidorResponsavel;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy HH:mm", timezone="America/Recife")
	private Date horaDaAtualizacao;
	
	private Status status;
	
	public Atualizacao() {
		// TODO Auto-generated constructor stub
	}
	
	public Atualizacao(String dataDePublicacao, String servidorResponsavel,
			Date horaDaAtualizacao) {
		this(dataDePublicacao, servidorResponsavel, horaDaAtualizacao, Status.DISPONIVEL);
	}
	
	public Atualizacao(String dataDePublicacao, String servidorResponsavel,
			Date horaDaAtualizacao, Status status) {
		this.dataDePublicacao = dataDePublicacao;
		this.servidorResponsavel = servidorResponsavel;
		this.horaDaAtualizacao = horaDaAtualizacao;
		this.status = status;
	}
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getDataDePublicacao() {
		return dataDePublicacao;
	}

	public void setDataDePublicacao(String dataDePublicacao) {
		this.dataDePublicacao = dataDePublicacao;
	}

	public String getServidorResponsavel() {
		return servidorResponsavel;
	}

	public void setServidorResponsavel(String servidorResponsavel) {
		this.servidorResponsavel = servidorResponsavel;
	}

	public Date getHoraDaAtualizacao() {
		return horaDaAtualizacao;
	}

	public void setHoraDaAtualizacao(Date horaDaAtualizacao) {
		this.horaDaAtualizacao = horaDaAtualizacao;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dataDePublicacao == null) ? 0 : dataDePublicacao.hashCode());
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
		if (dataDePublicacao == null) {
			if (other.dataDePublicacao != null)
				return false;
		} else if (!dataDePublicacao.equals(other.dataDePublicacao))
			return false;
		return true;
	}

	public boolean estaAtualizando() {
		return Status.ATUALIZANDO.equals(this.status);
	}
}