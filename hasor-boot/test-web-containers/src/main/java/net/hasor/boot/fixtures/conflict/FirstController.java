package net.hasor.boot.fixtures.conflict;

import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo("/duplicate/{first}")
public class FirstController {
    @Get
    public void execute() {
    }
}
