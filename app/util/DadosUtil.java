package util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import models.Iniciativa;
import play.Logger;

public class DadosUtil {

    public static List<String> listaAtualizacoes(String folder) {

        List<String> paths = new ArrayList<>();

        Path dir = Paths.get(folder);

        Logger.debug("Procurando autalizações em " + dir.toAbsolutePath().toString());

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                "iniciativas-[0-9]*.csv");) {

            for (Path path : ds) {
                String name = path.getFileName().toString();

                paths.add(name.substring(name.lastIndexOf("-") + 1,
                        name.indexOf(".csv")));
            }

            Collections.sort(paths);
        } catch (IOException e) {
            Logger.error(" Listagem de Atualizações: ", e);
        }
        return paths;
    }


    public static Iniciativa parseIniciativa(ResultSet resultSet) {
        try {
            float verbaGovernoFederal = resultSet.getString("VL_REPASSE_CONV").contains("NA") ? 0f : resultSet.getFloat("VL_REPASSE_CONV"); // repasse
            float verbaMunicipio = resultSet.getString("VL_CONTRAPARTIDA_CONV").contains("NA") ? 0f : resultSet.getFloat("VL_CONTRAPARTIDA_CONV");    // contrapartida

            DateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
            Date dataConclusao = formatter.parse(resultSet.getString("DIA_FIM_VIGENC_CONV"));
            Date dataConclusaoGovernoFederal = formatter.parse(resultSet.getString("DIA_LIMITE_PREST_CONTAS"));

            return new Iniciativa(
                    resultSet.getLong("NR_CONVENIO"),        // id
                    resultSet.getInt("ANO"),        // ano
                    resultSet.getString("OBJETO_PROPOSTA"),    // titulo
                    resultSet.getString("Nome Programa"),    // programa
                    resultSet.getString("funcao.imputada"),    // area
                    resultSet.getString("DESC_ORGAO_SUP"),        // fonte
                    resultSet.getString("DESC_ORGAO"),    // concedente
                    resultSet.getString("TX_STATUS"),    // status
                    false,//resultSet.getBoolean(50),    // temAditivo
                    verbaGovernoFederal,        // verba do governo federal
                    verbaMunicipio,                // verba do municipio
                    formatter.parse(resultSet.getString("DIA_INIC_VIGENC_CONV")),        // data de inicio
                    dataConclusao,    // data de conclusao municipio
                    dataConclusaoGovernoFederal);
        } catch (SQLException | ParseException e) {
            Logger.error("Erro no parsing da iniciativa em: " + resultSet.toString());
        }
        return null;
    }

}
