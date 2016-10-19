package module;

import play.libs.akka.AkkaGuiceSupport;
import models.Cidadao;
import util.InitialData;
import actors.AtualizadorActor;
import controllers.AuthUtils;

import com.google.inject.AbstractModule;

/**
 * Carrega os dados de municípios, iniciativas e etc. no BD.
 */
public class MainModule extends AbstractModule implements AkkaGuiceSupport{

	@Override
	protected void configure() {
		bind(InitialData.class).asEagerSingleton();
		bind(AuthUtils.class).asEagerSingleton();
		Cidadao admin = new Cidadao();
		admin.setLogin("Dilma");
		bind(Cidadao.class).toInstance(admin);
		bindActor(AtualizadorActor.class, "atualizador-actor");
	}
}
