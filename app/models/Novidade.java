package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Um acontecimento (visto ou não) na linha do tempo de um cidadão.
 */
@Entity
public class Novidade {

    public static final String TIPO_OPINIAO = "opiniao";

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy,HH:mm", timezone="BRT")
    private Date criadaEm;

    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iniciativa")
    private Iniciativa iniciativa;

    private Opiniao opiniao;

    /**
     * Para o JPA
     */
    public Novidade() {
        this.criadaEm = new Date();
    }

    public Novidade(Opiniao opiniao) {
        this();
        this.opiniao = opiniao;
        this.tipo = TIPO_OPINIAO;
        this.iniciativa = opiniao.getIniciativa();
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Date criadaEm) {
        this.criadaEm = criadaEm;
    }

    public String getTipo() {
        return tipo;
    }

    public Iniciativa getIniciativa() {
        return iniciativa;
    }

    public void setIniciativa(Iniciativa iniciativa) {
        this.iniciativa = iniciativa;
    }
}
