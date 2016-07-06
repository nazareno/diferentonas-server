package util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import play.Logger;

public class DadosUtil {

	public static List<String> listaAtualizacoes(String folder) throws IOException {

		List<String> paths = new ArrayList<>();

		Path dir = Paths.get(folder);

		Logger.debug(dir.toAbsolutePath().toString());

		try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
				"iniciativas-[0-9]*.csv");) {

			for (Path path : ds) {
				String name = path.getFileName().toString();

				paths.add(name.substring(name.lastIndexOf("-") + 1,
						name.indexOf(".csv")));
			}

			Collections.reverse(paths);
			return paths;
		}
	}

}
