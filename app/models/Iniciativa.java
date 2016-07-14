package models;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import play.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Uma iniciativa de um município em conjunto com o governo federal, realizada através de convênio.
 */
@Entity
public class Iniciativa implements Serializable {
	
	private static String[] camposAtualizaveis = { "id", "ano", "titulo",
			"programa", "area", "fonte", "concedente", "status", "temAditivo",
			"verbaGovernoFederal", "verbaMunicipio", "dataInicio",
			"dataConclusaoMunicipio", "dataConclusaoGovernoFederal" };
	
	private static SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");

    private static final long serialVersionUID = 811023123992501763L;

    @Id
    private Long id;
    private Integer ano;
    
    @Column(columnDefinition = "TEXT")
    private String titulo;                // objeto
    private String programa;
    private String area;                // funcao
    private String fonte;                // orgaoSuperior
    private String concedente;
    private String status;                // situacao
    private Boolean temAditivo;            // in aditivo sn
    private Float verbaGovernoFederal;    // repasse
    private Float verbaMunicipio;        // contrapartida
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "America/Recife")
    private Date dataInicio;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "America/Recife")
    private Date dataConclusaoMunicipio;    // dataTerminoVigencia
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "America/Recife")
    private Date dataConclusaoGovernoFederal;    // dataLimitePrestacaoContas

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cidade")
    @JsonIgnore
    private Cidade cidade;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "iniciativa")
    @JsonIgnore
    private List<Opiniao> opinioes;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "iniciativa", targetEntity=Novidade.class)
    @JsonIgnore
    private List<Novidade> novidades;

    /* Adicionados para facilitar no json retornado para o cliente:    */
    @Transient
    private Map<String, Long> sumario;
    @Transient
    private boolean seguidaPeloRequisitante;

    public Iniciativa() {
        opinioes = new LinkedList<>();
        novidades = new LinkedList<>();
    }


    public Iniciativa(Long id, Integer ano, String titulo, String programa, String area, String fonte,
                      String concedente, String status, Boolean temAditivo, Float verbaGovernoFederal, Float verbaMunicipio,
                      Date dataInicio, Date dataConclusaoMunicipio, Date dataConclusaoGovernoFederal) {
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


	public boolean fullEquals(Iniciativa other) {
		if (ano == null) {
			if (other.ano != null)
				return false;
		} else if (!ano.equals(other.ano))
			return false;
		if (area == null) {
			if (other.area != null)
				return false;
		} else if (!area.equals(other.area))
			return false;
		if (concedente == null) {
			if (other.concedente != null)
				return false;
		} else if (!concedente.equals(other.concedente))
			return false;
		if (dataConclusaoGovernoFederal == null) {
			if (other.dataConclusaoGovernoFederal != null)
				return false;
		} else if (!dataConclusaoGovernoFederal
				.equals(other.dataConclusaoGovernoFederal))
			return false;
		if (dataConclusaoMunicipio == null) {
			if (other.dataConclusaoMunicipio != null)
				return false;
		} else if (!dataConclusaoMunicipio.equals(other.dataConclusaoMunicipio))
			return false;
		if (dataInicio == null) {
			if (other.dataInicio != null)
				return false;
		} else if (!dataInicio.equals(other.dataInicio))
			return false;
		if (fonte == null) {
			if (other.fonte != null)
				return false;
		} else if (!fonte.equals(other.fonte))
			return false;
		if (programa == null) {
			if (other.programa != null)
				return false;
		} else if (!programa.equals(other.programa))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (temAditivo == null) {
			if (other.temAditivo != null)
				return false;
		} else if (!temAditivo.equals(other.temAditivo))
			return false;
		if (titulo == null) {
			if (other.titulo != null)
				return false;
		} else if (!titulo.equals(other.titulo))
			return false;
		if (verbaGovernoFederal == null) {
			if (other.verbaGovernoFederal != null)
				return false;
		} else if (!verbaGovernoFederal.equals(other.verbaGovernoFederal))
			return false;
		if (verbaMunicipio == null) {
			if (other.verbaMunicipio != null)
				return false;
		} else if (!verbaMunicipio.equals(other.verbaMunicipio))
			return false;
		return true;
	}


	public void addOpiniao(Opiniao opiniao) {
        this.opinioes.add(opiniao);
        opiniao.setIniciativa(this);
        opiniao.getAutor().inscreverEm(this);
        opiniao.setNovidade(new Novidade(TipoDaNovidade.NOVA_OPINIAO, opiniao, this, this.cidade));
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


	public Map<String, Long> getSumario() {
		return sumario;
	}


	public void setSumario(Map<String, Long> sumario) {
		this.sumario = sumario;
	}

    public void setSeguidaPeloRequisitante(boolean seguidaPeloRequisitante) {
        this.seguidaPeloRequisitante = seguidaPeloRequisitante;
    }

    public boolean isSeguidaPeloRequisitante() {
        return seguidaPeloRequisitante;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }


	public List<Novidade> getNovidades() {
		return novidades;
	}


	public void setNovidades(List<Novidade> novidades) {
		this.novidades = novidades;
	}
    
	public void atualiza(Iniciativa iniciativaAtualizada, Date dataDaAtualizacao) {
		for (String campo : camposAtualizaveis) {
			try {
				Field field = Iniciativa.class.getDeclaredField(campo);
				Object valorAntigo = field.get(this);
				Object valorNovo = field.get(iniciativaAtualizada);

				if(!valorAntigo.equals(valorNovo)){
					field.setAccessible(true);
					field.set(this, valorNovo);
					if(Date.class.equals(field.getType())){
						String dataAntigaFormatada = formatoData.format(valorAntigo);
						String dataNovaFormatada = formatoData.format(valorNovo);
						
						novidades.add(new Novidade(TipoDaNovidade.ATUALIZACAO_DE_INICIATIVA, dataDaAtualizacao, this.cidade, this, campo, dataAntigaFormatada, dataNovaFormatada));
						
					}else{
						novidades.add(new Novidade(TipoDaNovidade.ATUALIZACAO_DE_INICIATIVA, dataDaAtualizacao, this.cidade, this, campo, valorAntigo.toString(), valorNovo.toString()));
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException
					| NoSuchFieldException | SecurityException e) {
				Logger.error("não atualizou iniciativa: x", e);
			}
		}
	}
    
}
