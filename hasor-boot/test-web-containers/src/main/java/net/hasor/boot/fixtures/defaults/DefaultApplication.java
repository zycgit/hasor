package net.hasor.boot.fixtures.defaults;

public class DefaultApplication implements net.hasor.web.WebModule {
    @Override
    public void loadModule(net.hasor.web.WebApiBinder binder) {
        binder.setResponseCharacter("UTF-8");
    }
}
