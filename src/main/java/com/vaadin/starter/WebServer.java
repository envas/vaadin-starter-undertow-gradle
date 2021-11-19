/*
 * @(#)GwUiService.java   15.11.2021
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.vaadin.starter;

import com.vaadin.flow.server.startup.RouteRegistryInitializer;
import com.vaadin.flow.server.startup.ServletDeployer;
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
                    .addServletContainerInitializer(routesInitializer)
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
}
