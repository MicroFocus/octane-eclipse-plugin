# octane-eclipse-plugin
## Eclipse IDE Integration for OpenText™️ Core Software Delivery Platform

The plugin shares a common service layer with: https://github.com/MicroFocus/octane-intellij-plugin <br>
Common: https://github.com/MicroFocus/octane-plugin-common <br>

Connection to the server is done using the REST SDK for OpenText™️ Core Software Delivery Platform, see: <br>
* https://github.com/MicroFocus/ALMOctaneJavaRESTSDK

### How to build:
The plugin is build using mvn using tycho.

Go to the project root folder and run: 
```
mvn clean install
```

### How to debug: 
Import the project into eclipse, you'll need a verison of Eclipse for RCP and RAP Developers.
The maven module: octane-eclipse-plugin should be recogized as an eclipse plugin project.
