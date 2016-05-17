package module;

import util.InitialData;

import com.google.inject.AbstractModule;

/**
 * Guice module.
 * 
 * @author ricardoas
 */
public class MainModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(InitialData.class).asEagerSingleton();
	}
}
