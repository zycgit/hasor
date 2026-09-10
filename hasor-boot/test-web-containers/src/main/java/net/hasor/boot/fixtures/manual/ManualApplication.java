package net.hasor.boot.fixtures.manual;

import net.hasor.boot.fixtures.defaults.HelloController;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;

public class ManualApplication implements WebModule {
    @Override
    public void loadModule(WebApiBinder binder) {
        binder.loadMappingTo(HelloController.class);
        binder.setEncodingCharacter("UTF-8", "UTF-8");
    }
}
