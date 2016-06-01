package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.GenericGenerator;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Opinião de um cidadão sobre uma iniciativa de uma cidade.
 */
@Entity
public class Opiniao implements Serializable {

    public static final String TABLE = "Opiniao";

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iniciativa")
    @JsonBackReference
    private Iniciativa iniciativa;

    @Column(length = 1000)
    @Constraints.MaxLength(value = 1000,message = "Opiniões devem ter 1000 caracteres ou menos")
    @Constraints.Required(message = "Campo necessário")
    @Constraints.MinLength(1)
    private String conteudo;

    @Constraints.Required(message = "Campo necessário")
    @Constraints.Pattern("^(bomba|coracao|coracao_partido)$")
    private String tipo;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy,HH:mm", timezone="BRT")
    private Date criadaEm;

    public Opiniao(){
        this.criadaEm = new Date();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Iniciativa getIniciativa() {
        return iniciativa;
    }

    public void setIniciativa(Iniciativa iniciativa) {
        this.iniciativa = iniciativa;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Date criadaEm) {
        this.criadaEm = criadaEm;
    }
}
