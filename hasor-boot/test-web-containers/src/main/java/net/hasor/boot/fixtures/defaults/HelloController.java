package net.hasor.boot.fixtures.defaults;

import javax.servlet.http.HttpServletResponse;
import net.hasor.web.annotation.Get;
import net.hasor.web.annotation.MappingTo;

@MappingTo({ "/hello", "/override" })
public class HelloController {
    @Get
    public void hello(HttpServletResponse response) throws Exception {
        response.getWriter().write("你好");
    }
}
