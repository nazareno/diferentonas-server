package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Created by nazareno on 24/04/16.
 */
@Entity
public class Iniciativa implements Serializable {

    private static final long serialVersionUID = 811023123992501763L;

    @Id
    private Long id;
    private Integer ano;
    private String titulo;			// objeto
    private String area;			// funcao
    private String fonte;			// orgaoSuperior
    private String status;			// situacao
    private Float valorGovF;		// repasse
	private Float valorMun;			// contrapartida
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="DD/mm/yyyy")
	private Date dataInicio;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="DD/mm/yyyy")
	private Date dataConclusaoMun;	// dataTerminoVigencia
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="DD/mm/yyyy")
    private Date dataConclusaoGovF;	// dataLimitePrestacaoContas
    private String programa;

    public Iniciativa(){

    }

    public Iniciativa(Long id, Integer ano, String titulo, String area, String fonte, String status, Float valorGovF,
			Float valorMun, Date dataInicio, Date dataConclusaoMun, String programa) {
		this.id = id;
		this.ano = ano;
		this.titulo = titulo;
		this.area = area;
		this.fonte = fonte;
		this.status = status;
		this.valorGovF = valorGovF;
		this.valorMun = valorMun;
		this.dataInicio = dataInicio;
		this.dataConclusaoMun = dataConclusaoMun;
		this.programa = programa;
	}
    
    
    public Iniciativa(Long id, Integer ano, String titulo, String area, String fonte, String status, Float valorGovF,
			Float valorMun, Date dataInicio, Date dataConclusaoMun, Date dataConclusaoGovF, String programa) {
		this.id = id;
		this.ano = ano;
		this.titulo = titulo;
		this.area = area;
		this.fonte = fonte;
		this.status = status;
		this.valorGovF = valorGovF;
		this.valorMun = valorMun;
		this.dataInicio = dataInicio;
		this.dataConclusaoMun = dataConclusaoMun;
		this.dataConclusaoGovF = dataConclusaoGovF;
		this.programa = programa;
	}

    public long getId() {
        return id;
    }

    public int getAno() {
        return ano;
    }

    public String getStatus() {
        return status;
    }

    public String getPrograma() {
        return programa;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }
    
    public Float getValorMun() {
		return valorMun;
	}

	public void setValorMun(Float valorMun) {
		this.valorMun = valorMun;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataConclusaoMun() {
		return dataConclusaoMun;
	}

	public void setDataConclusaoMun(Date dataConclusaoMun) {
		this.dataConclusaoMun = dataConclusaoMun;
	}

	public Date getDataConclusaoGovF() {
		return dataConclusaoGovF;
	}

	public void setDataConclusaoGovF(Date dataConclusaoGovF) {
		this.dataConclusaoGovF = dataConclusaoGovF;
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
        Iniciativa other = (Iniciativa) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getFonte() {
		return fonte;
	}

	public void setFonte(String fonte) {
		this.fonte = fonte;
	}

	public Float getValorGovF() {
		return valorGovF;
	}

	public void setValorGovF(Float valorGovF) {
		this.valorGovF = valorGovF;
	}
}
