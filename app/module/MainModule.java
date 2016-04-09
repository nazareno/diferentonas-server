package module;

import models.InitialData;

import com.google.inject.AbstractModule;

public class MainModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(InitialData.class).asEagerSingleton();
	}

}
