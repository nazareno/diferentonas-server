package models;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy", timezone="UTC")
	private Date dataInicio;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy", timezone="UTC")
	private Date dataConclusaoMunicipio;	// dataTerminoVigencia
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy", timezone="UTC")
    private Date dataConclusaoGovernoFederal;	// dataLimitePrestacaoContas

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "iniciativa")
	@JsonBackReference
	private List<Opiniao> opinioes;


	public Iniciativa(){
		opinioes = new LinkedList<>();
    }
   

    public Iniciativa(Long id, Integer ano, String titulo, String programa, String area, String fonte,
			String concedente, String status, Boolean temAditivo, Float verbaGovernoFederal, Float verbaMunicipio,
			Date dataInicio, Date dataConclusaoMunicipio, Date dataConclusaoGovernoFederal) {
		super();
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
		this.dataConclusaoGovernoFederal = dataConclusaoGovernoFederal;
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

	public List<Opiniao> getOpinioes() {
		return opinioes;
	}

	public void addOpiniao(Opiniao opiniao) {
		this.opinioes.add(opiniao);
		opiniao.setIniciativa(this);
	}

	public boolean removeOpiniao(Opiniao paraRemover) {
		return this.opinioes.remove(paraRemover);
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public Integer getAno() {
		return ano;
	}



	public void setAno(Integer ano) {
		this.ano = ano;
	}



	public String getTitulo() {
		return titulo;
	}



	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}



	public String getPrograma() {
		return programa;
	}



	public void setPrograma(String programa) {
		this.programa = programa;
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



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Boolean getTemAditivo() {
		return temAditivo;
	}



	public void setTemAditivo(Boolean temAditivo) {
		this.temAditivo = temAditivo;
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



	public Date getDataInicio() {
		return dataInicio;
	}



	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}



	public Date getDataConclusaoMunicipio() {
		return dataConclusaoMunicipio;
	}



	public void setDataConclusaoMunicipio(Date dataConclusaoMunicipio) {
		this.dataConclusaoMunicipio = dataConclusaoMunicipio;
	}



	public Date getDataConclusaoGovernoFederal() {
		return dataConclusaoGovernoFederal;
	}



	public void setDataConclusaoGovernoFederal(Date dataConclusaoGovernoFederal) {
		this.dataConclusaoGovernoFederal = dataConclusaoGovernoFederal;
	}



	public void setOpinioes(List<Opiniao> opinioes) {
		this.opinioes = opinioes;
	}

}
