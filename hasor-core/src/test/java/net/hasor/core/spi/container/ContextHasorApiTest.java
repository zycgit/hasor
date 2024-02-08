///*
// * Copyright 2008-2009 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.hasor.core.spi.container;
//import net.hasor.cobble.ResourcesUtils;
//import net.hasor.core.*;
//import net.hasor.test.core.aop.ignore.types.GrandFatherBean;
//import net.hasor.test.core.aop.ignore.types.JamesBean;
//import net.hasor.test.core.aop.ignore.types.WilliamSonBean;
//import net.hasor.test.core.basic.inject.constructor.NativeConstructorPojoBeanRef;
//import net.hasor.test.core.basic.inject.constructor.SingleConstructorPojoBeanRef;
//import net.hasor.test.core.basic.pojo.PojoBean;
//import net.hasor.test.core.scope.AnnoSingletonBean;
//import net.hasor.test.core.scope.CustomHashBean;
//import org.junit.Test;
//
//import javax.inject.Singleton;
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Constructor;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Properties;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class ContextHasorApiTest {
//    @Test
//    public void contextTest1() {
//        AppContext appContext = new AppContextWarp(Hasor.create().build(apiBinder -> {
//            apiBinder.bindType(PojoBean.class).bothWith("pojo");
//            apiBinder.bindType(GrandFatherBean.class).nameWith("james").to(JamesBean.class);
//            apiBinder.bindType(GrandFatherBean.class).nameWith("william").to(WilliamSonBean.class);
//        }));
//        //
//        String[] names = appContext.getNames(GrandFatherBean.class);
//        assert names.length == 2;
//        assert names[0].equals("james");
//        assert names[1].equals("william");
//        //
//        assert appContext.findBindingBean(PojoBean.class).size() == 1;
//        assert appContext.findBindingBean(GrandFatherBean.class).size() == 2;
//        //
//        assert appContext.findBindingProvider(PojoBean.class).size() == 1;
//        assert appContext.findBindingProvider(GrandFatherBean.class).size() == 2;
//        //
//        assert appContext.findBindingBean("pojo", PojoBean.class) != null;
//        assert appContext.findBindingBean("james", GrandFatherBean.class) instanceof JamesBean;
//        assert appContext.findBindingBean("", GrandFatherBean.class) == null;
//        //
//        assert appContext.findBindingProvider("pojo", PojoBean.class).get() != null;
//        assert appContext.findBindingProvider("james", GrandFatherBean.class).get() instanceof JamesBean;
//        assert appContext.findBindingProvider("", GrandFatherBean.class) == null;
//    }
//
//    @Test
//    public void contextTest2() {
//        AppContext appContext = new AppContextWarp(Hasor.create().build(apiBinder -> {
//            apiBinder.bindType(PojoBean.class).bothWith("pojo").asEagerSingleton();
//        }));
//        //
//        PojoBean pojoBean1 = appContext.getInstance("pojo");
//        assert pojoBean1 instanceof PojoBean;
//        //
//        BindInfo<PojoBean> register = appContext.findBindingRegister("pojo", PojoBean.class);
//        PojoBean pojoBean2 = appContext.getInstance(register);
//        assert pojoBean2 instanceof PojoBean;
//        assert pojoBean1 == pojoBean2;
//        //
//        assert appContext.getInstance("abc") == null;
//    }
//
//    @Test
//    public void contextTest3() {
//        AppContext appContext = new AppContextWarp(() -> Hasor.create().build());
//        //
//        PojoBean pojoBean = new PojoBean();
//        SingleConstructorPojoBeanRef refBean = appContext.getInstance(SingleConstructorPojoBeanRef.class, pojoBean);
//        assert refBean instanceof SingleConstructorPojoBeanRef;
//        assert refBean.getPojoBean() == pojoBean;
//    }
//
//    @Test
//    public void contextTest4() throws NoSuchMethodException {
//        AppContext appContext = new AppContextWarp(() -> Hasor.create().build());
//        //
//        PojoBean pojoBean = new PojoBean();
//        Constructor<SingleConstructorPojoBeanRef> refConstructor = SingleConstructorPojoBeanRef.class.getConstructor(PojoBean.class);
//        SingleConstructorPojoBeanRef refBean = appContext.getInstance(refConstructor, pojoBean);
//        assert refBean instanceof SingleConstructorPojoBeanRef;
//        assert refBean.getPojoBean() == pojoBean;
//    }
//
//    @Test
//    public void contextTest5() throws NoSuchMethodException {
//        AppContext appContext = new AppContextWarp(() -> Hasor.create().build());
//        //
//        PojoBean pojoBean = new PojoBean();
//        Constructor<NativeConstructorPojoBeanRef> refConstructor = NativeConstructorPojoBeanRef.class.getConstructor(PojoBean.class);
//        NativeConstructorPojoBeanRef refBean = appContext.getInstance(refConstructor, pojoBean);
//        assert refBean instanceof NativeConstructorPojoBeanRef;
//        assert refBean.getPojoBean() == pojoBean;
//    }
//
//    @Test
//    public void contextTest6() {
//        AppContext context = Hasor.create().build();
//        AppContextWarp appContext = new AppContextWarp(() -> context);
//        //
//        assert appContext.getAppContext() == context;
//        assert appContext.getSettings() == context.getSettings();
//        //
//        appContext.setMetaData("abc", "abc");
//        assert "abc".equals(appContext.getMetaData("abc"));
//        appContext.removeMetaData("abc");
//        assert appContext.getMetaData("abc") == null;
//    }
//
//    @Test
//    public void hasorTest6() throws IOException, URISyntaxException {
//        URL resource = ResourcesUtils.getResource();
//        File file = new File("src/test/resources/net_hasor_core_context/variable_1.properties");
//        //
//        //
//        AppContext appContext1 = Hasor.create().mainSettingWith("/net_hasor_core_context/variable_1.properties").build();
//        assert appContext1.getSettings().getString("var_d").equalsIgnoreCase("d");
//        //
//        AppContext appContext2 = Hasor.create().mainSettingWith(resource).build();
//        assert appContext2.getSettings().getString("var_d").equalsIgnoreCase("d");
//        //
//        AppContext appContext3 = Hasor.create().mainSettingWith(resource.toURI()).build();
//        assert appContext3.getSettings().getString("var_d").equalsIgnoreCase("d");
//        //
//        AppContext appContext4 = Hasor.create().mainSettingWith(file).build();
//        assert appContext4.getSettings().getString("var_d").equalsIgnoreCase("d");
//        //
//        AppContext appContext6 = Hasor.create().addSettings("abc", "test_1", "t").build();
//        assert appContext6.getSettings().getString("test_1").equalsIgnoreCase("t");
//    }
//
//    @Test
//    public void hasorTest7() {
//        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
//        Hasor.create().addModules(new ArrayList<Module>() {{
//            add(apiBinder -> {
//                atomicBoolean.set(true);
//            });
//        }}).build();
//        //
//        assert atomicBoolean.get();
//    }
//
//    @Test
//    public void hasorTest8() {
//        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
//        Hasor.create().addModules(apiBinder -> {
//            atomicBoolean.set(true);
//        }).build();
//        //
//        assert atomicBoolean.get();
//    }
//
//    @Test
//    public void hasorTest11() {
//        AppContext build = Hasor.create().build(apiBinder -> {
//            apiBinder.bindType(CustomHashBean.class).asEagerSingleton();
//        });
//        build = new AppContextWarp(build);
//        assert build.isSingleton(AnnoSingletonBean.class);
//        assert build.isSingleton(build.getBindInfo(CustomHashBean.class));
//        assert build.isSingleton(CustomHashBean.class);
//        assert build.isStart();
//        //
//        assert build.findScope(Singleton.class) != null;
//        assert build.findScope(Singleton.class.getName()) != null;
//        assert build.findScope("sss") == null;
//    }
//
//    @Test
//    public void hasorTest12() {
//        Properties properties = new Properties();
//        properties.put("msg", "ABCDEFG");
//        //
//        AppContext appContext = Hasor.create()//
//                .mainSettingWith("/net_hasor_core_context/hello.xml")//
//                .loadSettings(properties)//
//                .build();//
//        //
//        assert appContext.getSettings().getString("msg_hallo").equals("Hello Word");
//        assert appContext.getSettings().getString("msg").equals("ABCDEFG");
//    }
//}
