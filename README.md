## Vaadin Starter with embedded Undertow server

This project demos the possibility of having Vaadin 14 application with the embedded Undertow server packed in the uberjar file. For Vaadin >= 14.6 there is an issue in the Vaadin server causing the Undertow server to return a `(403) Forbidden` response.

### Prerequisites:
* Java 8 or higher
* node.js and npm
* Git

### Testing

To compile the fatjar, run the Gradle task 

```
$ ./gradlew shadowJar
```

The generated fatjar file is `./build/libs/vaadinStarter-all.jar`. Run the Java application

```
$ java -jar ./build/libs/vaadinStarter-all.jar
```

and open the URL [http://localhost:8080](http://localhost:8080) in your browser. 

Modify the file `./gradle.properties` to change the Vaadin version. Do not forget to change the Gradle plugin version, too - the Vaadin Gradle plugin version must match the Vaadin core version. And, of course, do not forget to clean the project before any version change.

Version 14.5.4 works fine, since 14.6 the Vaadin server returns `Forbidden`.
