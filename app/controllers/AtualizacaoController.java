package controllers;


import static play.libs.Json.toJson;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

public class AtualizacaoController extends Controller {

    private static final String folder = "dist/data/";

	@Transactional(readOnly = true)
    public Result getAtualizacoes(){
    	
    	Path dir = Paths.get(folder);
    	
    	Logger.debug(dir.toAbsolutePath().toString());
    	
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
				"iniciativas-[0-9]*.csv");) {
			
			List<String> paths = new ArrayList<>();

			for (Path path : ds) {
				String name = path.getFileName().toString();
				
				paths.add(name.substring(name.lastIndexOf("-")+1, name.indexOf(".csv")));
			}
			
			Collections.reverse(paths);
		
			return ok(toJson(paths));
		} catch (IOException e) {
			return notFound(folder);
		}
    }

	@Transactional(readOnly = true)
	public Result aplica(){
		return TODO;
	}		
	
	@Transactional(readOnly = true)
	public Result status(){
		return TODO;
	}

}
