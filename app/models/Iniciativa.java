package models;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

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
    private String titulo;				// objeto
    private String programa;	
    private String area;				// funcao
    private String fonte;				// orgaoSuperior
    private String concedente;
    private String status;				// situacao
    private Boolean temAditivo;			// in aditivo sn
    private Float verbaGovernoFederal;	// repasse
	private Float verbaMunicipio;		// contrapartida
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="DD/mm/yyyy")
	private Date dataInicio;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="DD/mm/yyyy")
	private Date dataConclusaoMunicipio;	// dataTerminoVigencia
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="DD/mm/yyyy")
    private Date dataConclusaoGovernoFederal;	// dataLimitePrestacaoContas

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Opiniao> opinioes;


	public Iniciativa(){
		opinioes = new LinkedList<>();
    }
    
    public Iniciativa(Long id, Integer ano, String titulo, String programa, String area, String fonte, String concedente,
    				  String status, Boolean temAditivo, Float verbaGovernoFederal, Float verbaMunicipio, Date dataInicio,
    				  Date dataConclusaoMunicipio) {
		this();
		this.id = id;
		this.ano = ano;
		this.titulo = titulo;
		this.programa = programa;
		this.area = area;
		this.fonte = fonte;
		this.concedente = concedente;
		this.status = status;
		this.temAditivo = temAditivo;
		this.verbaGovernoFederal = verbaGovernoFederal;
		this.verbaMunicipio = verbaMunicipio;
		this.dataInicio = dataInicio;
		this.dataConclusaoMunicipio = dataConclusaoMunicipio;
//		this.dataConclusaoGovernoFederal = dataConclusaoGovernoFederal;
		
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

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
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

	public String getConcedente() {
		return concedente;
	}

	public void setConcedente(String concedente) {
		this.concedente = concedente;
	}

	public Float getVerbaGovernoFederal() {
		return verbaGovernoFederal;
	}

	public void setVerbaGovernoFederal(Float verbaGovernoFederal) {
		this.verbaGovernoFederal = verbaGovernoFederal;
	}

	public Float getVerbaMunicipio() {
		return verbaMunicipio;
	}

	public void setVerbaMunicipio(Float verbaMunicipio) {
		this.verbaMunicipio = verbaMunicipio;
	}

	public Boolean getTemAditivo() {
		return temAditivo;
	}

	public void setTemAditivo(Boolean temAditivo) {
		this.temAditivo = temAditivo;
	}

	public List<Opiniao> getOpinioes() {
		return opinioes;
	}

	public void addOpiniao(Opiniao opiniao) {
		this.opinioes.add(opiniao);
	}

	public boolean removeOpiniao(Opiniao paraRemover) {
		return this.opinioes.remove(paraRemover);
	}
}
