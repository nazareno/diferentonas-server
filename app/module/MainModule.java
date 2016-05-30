package module;

import util.InitialData;

import com.google.inject.AbstractModule;

/**
 * Carrega os dados de municípios, iniciativas e etc. no BD.
 */
public class MainModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(InitialData.class).asEagerSingleton();
	}
}
