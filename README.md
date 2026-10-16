# OpenFlexo HTTP project

This project adds to OpenFlexo HTTP capabilities. It includes:

- an HTTP server which is able to serve:
	- all resources and specific API for each technology adapter and
	- FML models as dynamic APIs.
- an HTTP technology adapter to connect to HTTP services via ReST or XML-RPC APIs,
 
## HTTP Server

The HTTP server is a service that starts an HTTP server (based on [Vert.x](http://vertx.io)) with 2 distinct capabilities:

 - serving all accessible resources and technology adapter specific models: the federation server.
 - serving API described using FML model acting as contracts: the chameleon server.
  
### Federation server
 
This part of the OpenFlexo HTTP server is defined by the OpenFlexo community and provides a ReST API to access to resources and technology adapter specific models. 

**Documentation**:

- [Getting Started](src/site/GettingStarted.md) to get started with the server and it's capabilities.
- [API Description](src/site/API.md) describes the server REST API.
- [Complement Technology Adapter](src/site/TechnologyAdapterRoutes.md) presents the server extension capabilities.
- [Demo Server](https://server.openflexo.org)

**TODO**:
 
 - [x] Resources
 - [x] Connie service using WebSockets
 - [ ] Technology adapters: 
   - [x] FML and FML@RT
   - [x] Gina
   - [ ] Diana
   - [ ] ...
 

### Chameleon server

The chameleon server allows an OpenFlexo **user** create an HTTP API by defining a contract using FML.

**TODO**

Not started yet.

## HTTP Technology Adapter

The HTTP technology adapter provides the capability to connect a distant HTTP API and access it throught `VirtualModel` and `FlexoConcept` instances. 
An HTTP model slot is configured with a `VirtualModel` acting as a contract where abstract properties, HTTP properties and HTTP behaviours are resolve using HTTP connections.

**TODO**:

 - [x] HTTP ModelSlot
 - [x] Simple abstract properties
 - [x] Behaviours to retreive instances
 - [x] Properties to retreive instances 
 - [ ] Several response format:
   - [x] JSON
   - [x] XML
- [ ] HTTP Methods
  - [x] GET
  - [ ] POST
  - [ ] PUT
  - [ ] UPDATE / PATCH
  - [ ] DELETE

And more

