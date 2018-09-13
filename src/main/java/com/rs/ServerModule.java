package com.rs;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.rs.io.JsonPlayerFileHandler;
import com.rs.io.PlayerFileHandler;
import com.rs.util.AbstractCredentialValidator;
import com.rs.util.LenientCredentialValidator;

public class ServerModule extends AbstractModule {

    protected void configure() {
        bind(PlayerFileHandler.class).to(JsonPlayerFileHandler.class).in(Singleton.class);
        bind(AbstractCredentialValidator.class).to(LenientCredentialValidator.class).in(Singleton.class);
    }
}
