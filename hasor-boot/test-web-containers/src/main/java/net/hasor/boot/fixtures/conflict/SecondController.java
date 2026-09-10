package net.hasor.boot.fixtures.conflict;

import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo("/duplicate/{second}")
public class SecondController {
    @Get
    public void execute() {
    }
}
