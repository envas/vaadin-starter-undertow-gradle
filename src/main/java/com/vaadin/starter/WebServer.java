/*
 * @(#)GwUiService.java   15.11.2021
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.vaadin.starter;


import com.vaadin.flow.di.LookupInitializer;
import com.vaadin.flow.router.InternalServerError;
import com.vaadin.flow.router.RouteNotFoundError;
import com.vaadin.flow.server.startup.*;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import java.util.HashSet;
import java.util.Set;
import static io.undertow.Handlers.redirect;

/**
 * Undertow embedded WebServer
 *
 */
public class WebServer {

    public static final String  SERVER_HOST = "0.0.0.0";
    public static final Integer SERVER_PORT = 8080;
    Undertow                    server      = null;

    public WebServer() {

        //
    }

    public void startServer() {

        Starter.logger.info(">>> Starting WebServer");

        try {

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            WebSocketDeploymentInfo wsdi = new WebSocketDeploymentInfo()
                    .setBuffers(new DefaultByteBufferPool(false, 8192));

            ClassPathResourceManager resMgr = new ClassPathResourceManager(classLoader, "META-INF/resources");

            ServletContainerInitializerInfo routesInitializer =
                 new ServletContainerInitializerInfo(RouteRegistryInitializer.class, setOfRouteAnnotatedClasses());

            //J-
            DeploymentInfo deploymentInfo = Servlets.deployment()
                    .setClassLoader(classLoader).setContextPath("/")
                    .setDeploymentName("starter")
                    .setDefaultEncoding("UTF-8")
                    .setResourceManager(resMgr)
                    .addServletContainerInitializer(getRouteRegistryInitializer())
                    .addServletContainerInitializer(getDevModeInitializer())
                    .addServletContainerInitializer(getErrorNavigationTargetInitializer())
                    .addServletContainerInitializer(getLookupServletContainerInitializer())
                    .addListener(Servlets.listener(ServletDeployer.class))
                    .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, wsdi);
            //J+

            DeploymentManager deploymentMgr = Servlets.defaultContainer().addDeployment(deploymentInfo);

            deploymentMgr.deploy();

            PathHandler path = Handlers.path(redirect("/")).addPrefixPath("/", deploymentMgr.start());

            server = Undertow.builder().addHttpListener(SERVER_PORT, SERVER_HOST).setHandler(path).build();
            server.start();
            Starter.logger.info(">>> WebServer started");

        } catch (Exception e) {
            Starter.logger.severe("!!! Unable to start the web server");
        }
    }

    public void stopServer() {
        server.stop();
    }

    public Set<Class<?>> setOfRouteAnnotatedClasses() {

        Set<Class<?>> set = new HashSet<>();

        set.add(MainView.class);

        return set;
    }


    /**
     * Returns initializer info for @Route annotated classes.
     *
     * @return registry initializer for @Route annotated classes
     */
    public ServletContainerInitializerInfo getRouteRegistryInitializer() {

        Set<Class<?>> set = new HashSet<>();
        set.add(MainView.class);

        return new ServletContainerInitializerInfo(RouteRegistryInitializer.class,set) ;
    }

    /**
     * Returns initializer info for @NpmPackage, @JsModule, @CssImport, @JavaScript or @Theme
     * annotated classes.
     *
     * @return initializer for @NpmPackage, @JsModule, @CssImport, @JavaScript or @Theme
     *         annotated classes
     */
    public ServletContainerInitializerInfo getDevModeInitializer() {

        Set<Class<?>> set = new HashSet<>();
        set.add(MainView.class);

        return new ServletContainerInitializerInfo(DevModeInitializer.class,set);
    }

    /**
     * Returns initializer info for anything implementing HasErrorParameter.
     *
     * @return initializer for classes implementing HasErrorParameter
     */
    public ServletContainerInitializerInfo getErrorNavigationTargetInitializer() {

        Set<Class<?>> set = new HashSet<>();
        set.add(RouteNotFoundError.class);
        set.add(InternalServerError.class);

        return new ServletContainerInitializerInfo(ErrorNavigationTargetInitializer.class,set);
    }

    /**
     * Returns initializer info for internals used for integrating with either of Spring, CDI or OSGi
     *
     * <b>IMPORTANT</b>: remove for Vaadin version < 14.6
     *
     * @return initializer for internals used for integrating with either of Spring, CDI or OSGi
     */
    public ServletContainerInitializerInfo getLookupServletContainerInitializer() {

        Set<Class<?>> set = new HashSet<>();
        set.add(LookupInitializer.class);

        return new ServletContainerInitializerInfo(LookupServletContainerInitializer.class,set);
    }


}
