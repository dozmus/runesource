package com.rs;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.rs.io.JsonPlayerFileHandler;
import com.rs.io.PlayerFileHandler;

public class ServerModule extends AbstractModule {

    protected void configure() {
        bind(PlayerFileHandler.class).to(JsonPlayerFileHandler.class).in(Singleton.class);
    }
}
