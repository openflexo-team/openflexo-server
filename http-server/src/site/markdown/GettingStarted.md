# Getting started with the federation server

OpenFlexo federation server provides HTTP/REST and WebSocket access to federated models. This page explains how to build, access and customize your own server to allow its deployment.

**Important**: To run and build an OpenFlexo application a Java 8 virtual machine is required.

A example server repository is available [here][1].

## Build the server

The OpenFlexo server as all OpenFlexo components doesn't do much own it's own, it's with connectors and resources that it reaches its full potential. The first thing to do it to package the server with the needed connectors and resource centers.

### Package a server

One easy way to package the server is to use a build tool, gradle for instance. Here is an example of a gradle build file that packages the server and a handful of connectors:

```gradle
group 'org.openflexo.test'
version '1.0-SNAPSHOT'

buildscript {
    ext.openflexo_version = '1.9.0-SNAPSHOT'
    ext.destination = "/home/openflexoserver"
}

apply plugin: 'java'

sourceCompatibility = 1.8

repositories {
    maven { url "https://maven.openflexo.org/artifactory/openflexo-deps/" }
}

dependencies {
    // dependency to the server
   compile "org.openflexo:http-server:${openflexo_version}"
 
    // set of connectors
    compile "org.openflexo:docxconnector:${openflexo_version}"
    compile "org.openflexo:excelconnector:${openflexo_version}"
    compile "org.openflexo:pdfconnector:${openflexo_version}"

    compile "org.openflexo:gina-http:${openflexo_version}"
 
    testCompile group: 'junit', name: 'junit', version: '4.12'
}
```

### Run the server

Once the packaging is done, the main server class is `org.openflexo.http.server.OpenFlexoServer`. Here are the arguments the server accepts:

```
Usage: server [options]

- -h|--help: show this help.
- -v|--verbose: verbose mode.
- --port port: server port.
- --host host: server host.
- --center path: resource center to register (may have several).
- --project path: path for a project to open (may have several).
```

## Access the server

Once the server runs, it provides the REST and WebSocket API described [here][2] but it also provides a set of static examples HTML files and JavaScript libraries to help the use of the API.
 
### HTML example pages

The `index.html` provides a simple page showing the technology adapters, resource centers and resources provided by the server. The `test.html` file provides a simple application allowing testing the API through simple exploration.  

![][image-1]

The `Context` field can receive urls for objects in the federation server to show its contents. The contents is navigable click on hyperlinks allowing the exploration of the content.

The `Right` field supports the evaluation of [Connie][3] expressions (named bindings). A such expression must have a context to evaluated upon, it’s uses the `Context` field as its context. When the `detailed` checkbox is checked the result will contains more details.

The expandable `Left` field supports [Connie][4] expressions as `Right` field does. These expressions must be settable allowing to assign its value to the `Right` field. 

The save icon will send a post request on the given `Context`. In case of resource urls it saves the resource.

### Javascript libraries

OpenFlexo client side is developed using [TypeScript][5], both TypeScript and Javascript files are provided. 

The following typescript examples need to have the OpenFlexo typescript files on hand to compile without errors.

**Api**

The folder `/js/openflexo/api` contains utility classes and interfaces to help the use of the federation server [API][6]. An instance of the `Api` class in `/js/openflexo/api/Api.ts` provides call to REST objects and Connie expressions on a given server. While it’s not required to use this library to access the federation server, it’s recommended to simplify the use of Connie expressions.

- Here is an example to retrieve information for a REST object:

```typescript
import { Api } from "./openflexo/api/Api";
import { Resource } from "./openflexo/api/resource";

const api = new Api();
let promise = api.call<Resource>("/resource/aHR0cDovL3d3dy5jeWFuZS5ldS9jYXJib3NvdXJjZS9Nb2RlbGlzYXRpb24udmlld3BvaW50");
promise.then(resource => {
    console.log(resource);
}).catch((error) => {
    console.log(error);
});
```

- Here is an example to evaluate a binding:

```typescript
import { Api, createRuntimeBinding } from "./openflexo/api/Api";

const api = new Api();
const vmi = "/ta/fmlrt/vmi/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvNC9zb3VyY2VfMTQ5MjE4MjI3NTEwNC9UeUFyTWVuZXpSZXRELnZpZXcvaW5zdGFsbGF0aW9uLnZteG1s/object/3";
// constructs the binding
const binding = createRuntimeBinding("name", vmi);
const promise = api.evaluate(binding);
promise.then(value => {
    console.log("Name = " + value);
}).catch((error) => {
    console.log(error);
});

// listens to changes for this binding
api.addChangeListener(binding, (event) => {
    console.log("Name changed to " + event.value);
});
```

- Here is an example to assign a binding:

```typescript
import { Api, createRuntimeBinding } from "./openflexo/api/Api";

const api = new Api();
const vmi = "/ta/fmlrt/vmi/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvNC9zb3VyY2VfMTQ5MjE4MjI3NTEwNC9UeUFyTWVuZXpSZXRELnZpZXcvaW5zdGFsbGF0aW9uLnZteG1s/object/3";
// constructs the binding
const binding = createRuntimeBinding("name", vmi);
const newValue = createRuntimeBinding('"newName"', vmi);
const promise = api.assign(binding, newValue, false);
promise.then(value => {
    console.log("Name = " + value);
}).catch((error) => {
    console.log(error);
});
```

**UI**

The folder `/js/openflexo/ui` contains a set of UI classes based on [MDL][7]. It allows to construct:

- [Buttons][8]
- [Cards][9]
- [Grids][10]
- [Tabs][11]
- [Icons][12]
- [List][13]
- [List][14]
- [Tables][15]
- [TextFields][16]

- Here is an example for a button:

```typescript
import { Button } from "./openflexo/ui/Button";
const button = new Button("Name");
button.container.onclick = (event) => {
    console.log("click");
}
```

- Here is an example for a text field:

```typescript
import { TextField } from "./openflexo/ui/TextField";
const textfield = new TextField("test", "here a value", "Label", true);
textfield.container.onchange = (event) => {
    console.log("Changed test");
}
```

**MVC**

The folder `/js/openflexo/mvc` contains a set of classes of UI classes controlled by Connie expression. 
They combine the two previous sections: API and UI. 
For example, a `BoundTextField` constructs a textfield controlled by a binding. 
Every time the value is changed on the client it assigns the new value and sends it to the server. 
It also listens to the changes from the server and update the value on the client when changed.

Here is the current list of bound controllers:

- `BoundButton`,
- `BoundLabel`,
- `BoundTable`,
- `BoundTextField`,
- `BoundTextArea`,
- `BoundTree`, ...

- Here is an exemple for a table, label, button and textfield:

```typescript
import { Resource } from "./openflexo/api/resource";

import { Api, createRuntimeBinding } from "./openflexo/api/Api";

import { BoundTable, BoundColumn } from "./openflexo/mvc/BoundTable";
import { BoundTextField } from "./openflexo/mvc/BoundTextField";
import { BoundButton } from "./openflexo/mvc/BoundButton";
import { BoundLabel } from "./openflexo/mvc/BoundLabel";
import { BoundIcon } from "./openflexo/mvc/BoundIcon";

import { Icon } from "./openflexo/ui/Icon"
import { Flow } from "./openflexo/ui/Flow"

function r(resource: Resource): BoundTable {
  let columns = [
          new BoundColumn(
              "Type",
              (api, element: any) => element.flexoConcept.name
          ),
          new BoundColumn(
              "Info",
              (api, element) => {
                let icon = new BoundIcon(api, "icon");
                icon.visible ="this.flexoConcept.name = 'File'";
                icon.updateRuntime(element.url);

                let count = new BoundLabel(api, "this.getChildren().size()");
                count.visible = "this.flexoConcept.name = 'Directory'";
                count.updateRuntime(element.url);

                return new Flow(icon, count);
              }
          ),
          new BoundColumn(
              "Icon",
              (api, element) => {
                let result = new BoundTextField(api, "icon");
                result.visible = "this.flexoConcept.name = 'File'";
                result.updateRuntime(element.url);
                return result;
              }
          ),
          new BoundColumn(
              "Name",
              (api, element) => new BoundTextField(api, "name", "Name", element.url, false)
          ),
          new BoundColumn(
              "Delete",
              (api, element) => new BoundButton(api, new Icon("delete"),
                "parent.deleteElement(this)",
                  element.url, null,   "icon")
          )

      ];
      const table = new BoundTable(this.context.api, "this.allElements()", columns);
      table.updateRuntime(this.context.modelUrl);
      return table;
}

```

## Customize the server

The federation server can be customize for client and server side. Static resources may be added or replaced allowing to write a new client. On the other side, Java services add new behaviors to the federation server.

### Static resources

The OpenFlexo server packaging contains a set of static resources (as seen in previous section) embedded in the jar of the application.
To embedded your own content there are two solutions: 
- adds a `webroot` folder in the directory where you run the server or
- adds a `webroot` folder in the sources you add to the server.
In the latter, the resources will be added to the jar simplify the deployment of the server (See [Vertx][17] documentation for more information on this matter).

Alls the files in our `webroot` folder will be served with a higher priority than the existing one in the federation server (allowing to replace existing one like `index.html`).

### Server services

**TODO**

- [TechnologyAdatperRoutes][18]
- [ComplementJson][19] 
- [ApplicationRoutes][20]

## Deploy the server

There are many ways to deploy the server. An easy one is to use the gradle `application` plugin. 
Add the following to grade build file in the `build` section (See [gradle][21] documentation on the plugin):

```gradle
apply plugin: 'application'
mainClassName = "org.openflexo.http.server.OpenFlexoServer"
distributions {
    main {
        contents { from('./webroot') into "./webroot" }
    }
}
```

This will allow you to run the server and construct a distribution for deployment.

The [demo](https://github.com/openflexo-team/openflexo-demo) repository contains a complete example to deploy the server using gradle.

[1]:	https://github.com/openflexo-team/openflexo-demo
[2]:	API.md
[3]:	https://connie.openflexo.org/
[4]:	https://connie.openflexo.org/
[5]:	https://www.typescriptlang.org
[6]:	API.md
[7]:	https://getmdl.io "Material Design Lite"
[8]:	https://getmdl.io/components/index.html#buttons-section
[9]:	https://getmdl.io/components/index.html#cards-section
[10]:	https://getmdl.io/components/index.html#layout-section/grid
[11]:	https://getmdl.io/components/index.html#layout-section/tabs
[12]:	http://google.github.io/material-design-icons/
[13]:	https://getmdl.io/components/index.html#lists-section
[14]:	https://getmdl.io/components/index.html#tables-section
[15]:	https://getmdl.io/components/index.html#textfields-section
[16]:	https://getmdl.io/components/index.html#textfields-section
[17]:	http://vertx.io/docs/vertx-web/java/#_serving_static_resources
[18]:	TechnologyAdapterRoutes.md
[19]:	ComplementJson.md
[20]:	ApplicationRoutes.md
[21]:	https://docs.gradle.org/current/userguide/application_plugin.html

[image-1]:	ServerTestApplication.png "Test Application"
