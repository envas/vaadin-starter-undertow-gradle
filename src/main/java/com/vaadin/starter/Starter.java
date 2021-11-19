package com.vaadin.starter;

import java.util.logging.Logger;

class Starter {

    public static Logger logger ;

    public static void main(String[] args) {
        logger = Logger.getLogger("starter");

        WebServer server = new WebServer();
        server.startServer();

    }
}