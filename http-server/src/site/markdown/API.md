
# OpenFlexo REST API

The OpenFlexo's HTTP server provides a REST API to access:

- resources in all resources centers
- apis from technology adapters:
	- federated models `FML`/`FML-RT` 
	- gina views `Gina`
	- diagrams `Diana`

It also provides a WebSocket API to handle connie expressions and updates the server.

## General view

Each REST object provided by the server will contain: 

- `id` is the internal id for the object, 
- `type` gives information concerning it's type,
- `url` provide a path to access the object,
- a `name` if it exists and
- a series of properties specific to the object.

The first 4 fields constitute a reference for an object. 
For instance, a `FlexoConceptInstance` refers to it `FlexoConcept` using a reference like so:

```json
"flexoConcept" : {
   "name" : "Directory",
   "id" : "11",
   "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/11",
   "type" : "FlexoConcept"
}
```

## Resource Centers

The registered resource centers are presented with the prefix: `/rc`.

**Note**: The TypeScript definition file for the resource centers is 
[here][1].

### Get

- Get on **`/rc`**  lists of resource centers, for instance:

```json
[{
   "name" : "demo",
   "type" : "ResourceCenter",
   "uri" : "http://www.openflexo.org/projects/2017/9/Untitled_1505918219989",
   "id" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
   "url" : "/rc/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
   "resourceUrl" : "/rc/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5/resource"
 }, {
  "name" : "http://openflexo.org/docx-test",
  "type" : "ResourceCenter",
  "uri" : "http://openflexo.org/docx-test",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "url" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "resourceUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0/resource"
}, {
  "name" : "http://openflexo.org/excel-test",
  "type" : "ResourceCenter",
  "uri" : "http://openflexo.org/excel-test",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZXhjZWwtdGVzdA",
  "url" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZXhjZWwtdGVzdA",
  "resourceUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZXhjZWwtdGVzdA/resource"
}]
```

- Get on **`/rc/{rc_id}`** returns the information for resource center with given id.

Example results for `/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0`:

```json
{
  "name" : "http://openflexo.org/docx-test",
  "type" : "ResourceCenter",
  "uri" : "http://openflexo.org/docx-test",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "url" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "resourceUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0/resource"
}
```

- Get on **`/rc/{rc_id}/resource{path}`** lists of resources for resource center with given id.

Example results for `/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0/resource`:

```json
[{
  "name" : "Step3-alter-MSWord.docx",
  "type" : "Resource",
  "uri" : "http://openflexo.org/docx-test/TestResourceCenter/HeterogeneousDocumentEdition2/Step3-alter-MSWord.docx",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9IZXRlcm9nZW5lb3VzRG9jdW1lbnRFZGl0aW9uMi9TdGVwMy1hbHRlci1NU1dvcmQuZG9jeA",
  "modified" : false,
  "resourceCenterId" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "resourceCenterUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "url" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9IZXRlcm9nZW5lb3VzRG9jdW1lbnRFZGl0aW9uMi9TdGVwMy1hbHRlci1NU1dvcmQuZG9jeA",
  "contentUrl" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9IZXRlcm9nZW5lb3VzRG9jdW1lbnRFZGl0aW9uMi9TdGVwMy1hbHRlci1NU1dvcmQuZG9jeA/contents",
  "technologyAdapterId" : "docx",
  "technologyAdapterUrl" : "/ta/docx"
}, {
  "name" : "Step2.docx",
  "type" : "Resource",
  "uri" : "http://openflexo.org/docx-test/TestResourceCenter/MSWordDocumentEdition/Step2.docx",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9NU1dvcmREb2N1bWVudEVkaXRpb24vU3RlcDIuZG9jeA",
  "modified" : false,
  "resourceCenterId" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "resourceCenterUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "url" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9NU1dvcmREb2N1bWVudEVkaXRpb24vU3RlcDIuZG9jeA",
  "contentUrl" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9NU1dvcmREb2N1bWVudEVkaXRpb24vU3RlcDIuZG9jeA/contents",
  "technologyAdapterId" : "docx",
  "technologyAdapterUrl" : "/ta/docx"
}]
```

Example results for `/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0/resource/TestResourceCenter`:

```json
[ {
  "name" : "DocumentWithImage.docx",
  "type" : "Resource",
  "uri" : "http://openflexo.org/docx-test/TestResourceCenter/DocumentWithImage.docx",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9Eb2N1bWVudFdpdGhJbWFnZS5kb2N4",
  "modified" : false,
  "resourceCenterId" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "resourceCenterUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "url" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9Eb2N1bWVudFdpdGhJbWFnZS5kb2N4",
  "contentUrl" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9Eb2N1bWVudFdpdGhJbWFnZS5kb2N4/contents",
  "technologyAdapterId" : "docx",
  "technologyAdapterUrl" : "/ta/docx"
}, {
  "name" : "DocumentWithManyTables.docx",
  "type" : "Resource",
  "uri" : "http://openflexo.org/docx-test/TestResourceCenter/DocumentWithManyTables.docx",
  "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9Eb2N1bWVudFdpdGhNYW55VGFibGVzLmRvY3g",
  "modified" : false,
  "resourceCenterId" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "resourceCenterUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
  "url" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9Eb2N1bWVudFdpdGhNYW55VGFibGVzLmRvY3g",
  "contentUrl" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9Eb2N1bWVudFdpdGhNYW55VGFibGVzLmRvY3g/contents",
  "technologyAdapterId" : "docx",
  "technologyAdapterUrl" : "/ta/docx"
} ]
```

## Resources

The resources in all resource centers are presented with the prefix `/resource`.
One resource can be accessed using it's URI encoded in Base64 to avoid forbidden characters in URLs. 

Here is an example:

- uri: `http://openflexo.org/docx-test/TestResourceCenter/TestLibraryViewPoint2.fml/LibraryVirtualModel.fml`
- id: `aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9UZXN0TGlicmFyeVZpZXdQb2ludDIuZm1sL0xpYnJhcnlWaXJ0dWFsTW9kZWwuZm1s`
A resource is described with several properties:
- `loaded` indicates if the resource has been loaded. 
- `modified` tells if the resource has been changed in memory (it must be loaded).
- `resourceCenterId|Url` references the resource center.


**Note**: The TypeScript definition file for the resources is 
[here][2].

### Get

- Get on **`/resource`** lists of all resources in all resource centers.

Example results for `/resource`:

```json
[ {
    "name" : "LibraryVirtualModel",
    "type" : "Resource",
    "uri" : "http://openflexo.org/docx-test/TestResourceCenter/TestLibraryViewPoint2.fml/LibraryVirtualModel.fml",
    "id" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9UZXN0TGlicmFyeVZpZXdQb2ludDIuZm1sL0xpYnJhcnlWaXJ0dWFsTW9kZWwuZm1s",
    "modified" : false,
    "loaded" : false,
    "resourceCenterId" : "aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
    "resourceCenterUrl" : "/rc/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0",
    "url" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9UZXN0TGlicmFyeVZpZXdQb2ludDIuZm1sL0xpYnJhcnlWaXJ0dWFsTW9kZWwuZm1s",
    "contentUrl" : "/resource/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9UZXN0TGlicmFyeVZpZXdQb2ludDIuZm1sL0xpYnJhcnlWaXJ0dWFsTW9kZWwuZm1s/contents",
    "technologyAdapterId" : "fml",
    "technologyAdapterUrl" : "/ta/fml",
    "objectsUrl" : "/ta/fml/model/aHR0cDovL29wZW5mbGV4by5vcmcvZG9jeC10ZXN0L1Rlc3RSZXNvdXJjZUNlbnRlci9UZXN0TGlicmFyeVZpZXdQb2ludDIuZm1sL0xpYnJhcnlWaXJ0dWFsTW9kZWwuZm1s/object"
} ]
```

- Get on **`/resource/{resource_id}`** retrieves information for one resource with given id.

- Get on **`/resource/{resource_id}/contents`** retrieves raw content for the resource with given id.

### Post

- Post on **`/resource/{resource_id}`** will **save** the resource with the given id.

## Technology adapters

All found technology adapters are presented with the prefix: `/ta`.

**Note**: The TypeScript definition file for the technology adapter description is 
[here][3].

### Get

- Get on **`/ta`** lists of technology adapters

Example results for `/ta`:

```json
[ {
  "name" : "FML technology adapter",
  "type" : "TechnologyAdapter",
  "id" : "fml",
  "activated" : true,
  "url" : "/ta/fml",
  "complemented" : true,
  "virtualModelResourceUrl" : "/ta/fml/model"
}, {
  "name" : "FML@runtime technology adapter",
  "type" : "TechnologyAdapter",
  "id" : "fmlrt",
  "activated" : true,
  "url" : "/ta/fmlrt",
  "complemented" : true,
  "fmlrtVirtualModelInstanceResourceUrl" : "/ta/fmlrt/instance"
}, {
  "name" : "DocX Technology Adapter",
  "type" : "TechnologyAdapter",
  "id" : "docx",
  "activated" : true,
  "url" : "/ta/docx",
  "complemented" : false
}, {
  "name" : "Excel technology adapter",
  "type" : "TechnologyAdapter",
  "id" : "xls",
  "activated" : true,
  "url" : "/ta/xls",
  "complemented" : false
}, {
  "name" : "GINA Technology Adapter",
  "type" : "TechnologyAdapter",
  "id" : "gina",
  "activated" : true,
  "url" : "/ta/gina",
  "complemented" : false
} ]
```

- Get on **`/ta/{id}`** retrieves information for the technology adapter of given id.

### Complements

When a technology adapters provides specific objects by implementing a `TechnologyAdapterRouteComplement` it sets the `complemented` field to true.
It may also provide new routes for new objects. The next sections are technology adapter complements. 

Resources that are complemented by a technology adapter will contains two more attributes:
- `objectsUrl` that contains the url for the list of all objects contained in the resource. 
- `modelUrl` that presents the root object of the resource. It's present only if the resource is loaded.

If the resource isn't `loaded`, an access to one of it's objects or to the list of all the objects will automatically load it.
Thus if there is no technology adapter complement for a given resource, it will never be loaded. 

See how to complement a technology adapter [here][4].

## FML

The FML complement adds the route `/ta/fml/model` to list all virtual models found in the resource centers.

**Note**: The TypeScript definition file for the FML is 
[here][5].

### Get

- Get on **`/ta/fml/model`** lists of all virtual models in all resource centers.

Example results for `/ta/fml/model` ([server][6]):

```json
[ {
  "name" : "Hierarchy",
  "type" : "Resource",
  "uri" : "http://www.openflexo.org/projects/2017/9/Hierarchy_1505726316188/Hierarchy.fml",
  "id" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9IaWVyYXJjaHlfMTUwNTcyNjMxNjE4OC9IaWVyYXJjaHkuZm1s",
  "modified" : false,
  "loaded" : false,
  "resourceCenterId" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9IaWVyYXJjaHlfMTUwNTcyNjMxNjE4OA",
  "resourceCenterUrl" : "/rc/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9IaWVyYXJjaHlfMTUwNTcyNjMxNjE4OA",
  "url" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9IaWVyYXJjaHlfMTUwNTcyNjMxNjE4OC9IaWVyYXJjaHkuZm1s",
  "contentUrl" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9IaWVyYXJjaHlfMTUwNTcyNjMxNjE4OC9IaWVyYXJjaHkuZm1s/contents",
  "technologyAdapterId" : "fml",
  "technologyAdapterUrl" : "/ta/fml",
  "objectsUrl" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9IaWVyYXJjaHlfMTUwNTcyNjMxNjE4OC9IaWVyYXJjaHkuZm1s/object"
}, {
  "name" : "Demo",
  "type" : "Resource",
  "uri" : "http://www.openflexo.org/projects/2017/9/Untitled_1505918219989/Demo.fml",
  "id" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s",
  "modified" : false,
  "loaded" : true,
  "resourceCenterId" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
  "resourceCenterUrl" : "/rc/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
  "url" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s",
  "contentUrl" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/contents",
  "technologyAdapterId" : "fml",
  "technologyAdapterUrl" : "/ta/fml",
  "modelUrl" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/2",
  "objectsUrl" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object"
} ]
```

- Get on **`/ta/fml/model/{model_id}`** retrieves information for one model with given id ([server][7]).

- Get on **`/ta/fml/model/{model_id}/object`** retrieves all objects from model ([server][8])

```json
[ {
  "name" : "Directory",
  "id" : "11",
  "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/11",
  "type" : "FlexoConcept",
  "description" : null,
  "virtualModel" : {
    "name" : "Demo",
    "id" : "2",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/2",
    "type" : "VirtualModel"
  },
  "container" : null,
  "childFlexoConcepts" : [ ],
  "parents" : [ {
    "name" : "Element",
    "id" : "21",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/21",
    "type" : "FlexoConcept"
  } ],
  "properties" : { },
  "behaviors" : { ... }
}, {
  "name" : null,
  "id" : "6",
  "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/6",
  "type" : "Inspector"
}, {
  "name" : "create",
  "id" : "8",
  "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/8",
  "type" : "CreationScheme",
  "parameters" : [ {
    "name" : "parent",
    "id" : "123",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/123",
    "type" : "GenericBehaviourParameter",
    "declaredType" : {
      "type" : "FlexoConceptInstanceType"
    },
    "behavior" : {
      "name" : "create",
      "id" : "8",
      "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/8",
      "type" : "CreationScheme"
    },
    "flexoConcept" : {
      "name" : "Directory",
      "id" : "11",
      "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/11",
      "type" : "FlexoConcept"
    }
  } ],
  "returnType" : {
    "type" : "Class"
  },
  "flexoConcept" : {
    "name" : "Directory",
    "id" : "11",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/11",
    "type" : "FlexoConcept"
  },
  "controlGraph" : [ {
    "name" : null,
    "id" : "45",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/45",
    "type" : "AssignationAction"
  }, {
    "name" : null,
    "id" : "118",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/118",
    "type" : "AssignationAction"
  } ]
}, ... ]
```

## FML@runtime

The FML@Runtime complement adds the route `/ta/fmlrt/instance` to list all virtual model instances found in the resource centers.

**Note**: The TypeScript definition file for the FML@Runtime is 
[here][9].

### Get

- Get on **`/ta/fmlrt/instance`** lists of all virtual models in all resource centers.

Example results for `/ta/fmlrt/instance` ([server][10]):

```json
[ {
  "name" : "demo",
  "type" : "Resource",
  "uri" : "http://www.openflexo.org/projects/2017/9/Untitled_1505918219989/demo.fml.rt",
  "id" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0",
  "modified" : false,
  "loaded" : true,
  "resourceCenterId" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
  "resourceCenterUrl" : "/rc/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
  "url" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0",
  "contentUrl" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/contents",
  "technologyAdapterId" : "fmlrt",
  "technologyAdapterUrl" : "/ta/fmlrt",
  "modelUrl" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/1",
  "objectsUrl" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object"
} ]
```

- Get on **`/ta/fmlrt/instance/{instance_id}`** retrieves information for one model with given id ([server][11]).

- Get on **`/ta/fmlrt/instance/{instance_id}/object`** retrieves all objects from model ([server][12])

```json
[ {
  "id" : "2",
  "url" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/2",
  "type" : "FlexoConceptInstance",
  "flexoConcept" : {
    "name" : "Directory",
    "id" : "11",
    "url" : "/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object/11",
    "type" : "FlexoConcept"
  },
  "virtualModelInstance" : {
    "id" : "1",
    "url" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/1",
    "type" : "FMLRTVirtualModelInstance"
  },
  "container" : null,
  "actors" : {
    "name" : {
      "id" : "3",
      "url" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/3",
      "type" : "PrimitiveActorReference",
      "instance" : {
        "id" : "2",
        "url" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/2",
        "type" : "FlexoConceptInstance"
      },
      "value" : "[root]",
      "roleName" : "name"
    }
  },
  "embeddedFlexoConceptInstance" : [ ]
}, 
{
  "id" : "6",
  "url" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/6",
  "type" : "PrimitiveActorReference",
  "instance" : {
    "id" : "5",
    "url" : "/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object/5",
    "type" : "FlexoConceptInstance"
  },
  "value" : "Vu",
  "roleName" : "name"
}, ... ]
```
## Gina

The Gina complement adds the route `/ta/gina/fib` to list all fib components found in the resource centers.

**Note**: The TypeScript definition file for the Fib Components is 
[here][13].

- Get on **`/ta/gina/fib`** lists of all virtual models in all resource centers.

Example results for `/ta/gina/fib` ([server][14]):

```json
[ {
  "name" : "DemoUI.fib",
  "type" : "Resource",
  "uri" : "http://www.openflexo.org/projects/2017/9/Untitled_1505918219989/DemoUI.fib",
  "id" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI",
  "modified" : false,
  "loaded" : true,
  "resourceCenterId" : "aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
  "resourceCenterUrl" : "/rc/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5",
  "url" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI",
  "contentUrl" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI/contents",
  "technologyAdapterId" : "gina",
  "technologyAdapterUrl" : "/ta/gina",
  "modelUrl" : "/ta/gina/fib/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI/object/1",
  "objectsUrl" : "/ta/gina/fib/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI/object"
} ]
```

- Get on **`/ta/gina/fib/{fib_id}`** retrieves information for one model with given id ([server][15]).

- Get on **`/ta/gina/fib/{fib_id}/object`** retrieves all objects from model ([server][16])

- Get on **`/ta/gina/fib/{fib_id}/object/1`** retrieves object id `1` objects from model ([server][17])

```json
{
  "id" : "1",
  "url" : "/ta/gina/fib/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI/object/1",
  "type" : "GINAFIBComponent",
  "resourceUrl" : "/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI",
  "component" : {
    "type" : "Panel",
    "align" : "left",
    "borderTop" : 0,
    "borderLeft" : 0,
    "trackViewPortHeight" : true,
    "protectContent" : false,
    "trackViewPortWidth" : true,
    "borderBottom" : 0,
    "border" : "empty",
    "sizeAdjustment" : "OriginalSize",
    "darkLevel" : 0,
    "layout" : "border",
    "borderRight" : 0,
    "data" : "data",
    "dataClassName" : "org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance",
    "subComponents" : [ {
      "type" : "TabPanel",
      "restrictPreferredSizeToSelectedComponent" : false,
      "subComponents" : [ {
        "type" : "Tab",
        "title" : "Tab",
        "align" : "left",
        "borderTop" : 0,
        "borderLeft" : 0,
        "trackViewPortHeight" : true,
        "protectContent" : false,
        "trackViewPortWidth" : true,
        "borderBottom" : 0,
        "border" : "empty",
        "sizeAdjustment" : "OriginalSize",
        "darkLevel" : 0,
        "layout" : "twocols",
        "borderRight" : 0,
        "subComponents" : [ {
          "type" : "Label",
          "label" : "label",
          "align" : "left",
          "trimText" : false,
          "localize" : false,
          "manageDynamicModel" : false,
          "readOnly" : false,
          "data" : "(\"Size \" + data.root.getChildren().size)",
          "useScrollBar" : false,
          "parent" : {
            "type" : "Tab"
          },
          "constraints" : "twocols(expandHorizontally=true;expandVertically=false;insetsBottom=0;insetsLeft=0;insetsRight=0;insetsTop=0;location=left)",
          "controllerClass" : "org.openflexo.gina.controller.FIBController",
          "variables" : [ ],
          "explicitDependancies" : [ ],
          "name" : "label1",
          "parameters" : [ ]
        }, {
          "type" : "TextField",
          "passwd" : false,
          "columns" : 7,
          "validateOnReturn" : false,
          "localize" : false,
          "manageDynamicModel" : false,
          "readOnly" : false,
          "data" : "data.root.name",
          "useScrollBar" : false,
          "parent" : {
            "type" : "Tab"
          },
          "constraints" : "twocols(expandHorizontally=true;expandVertically=false;insetsBottom=0;insetsLeft=0;insetsRight=0;insetsTop=0;location=right)",
          "controllerClass" : "org.openflexo.gina.controller.FIBController",
          "variables" : [ ],
          "explicitDependancies" : [ ],
          "name" : "textField1",
          "parameters" : [ ]
        } ],
        "useScrollBar" : false,
        "parent" : {
          "type" : "TabPanel"
        },
        "controllerClass" : "org.openflexo.gina.controller.FIBController",
        "variables" : [ ],
        "explicitDependancies" : [ ],
        "name" : "tab1",
        "parameters" : [ ]
      } ],
      "useScrollBar" : false,
      "parent" : {
        "type" : "Panel"
      },
      "constraints" : "border(location=center)",
      "height" : 60,
      "controllerClass" : "org.openflexo.gina.controller.FIBController",
      "variables" : [ ],
      "explicitDependancies" : [ ],
      "width" : 100,
      "name" : "rootTabPanel",
      "parameters" : [ ]
    } ],
    "useScrollBar" : false,
    "controllerClass" : "org.openflexo.gina.controller.FIBController",
    "variables" : [ {
      "type" : "FIBVariable"
    } ],
    "explicitDependancies" : [ ],
    "name" : "rootPanel",
    "parameters" : [ ]
  }
}
```

## Diana

Diana complement isn't implemented yet.


[1]:	https://github.com/openflexo-team/openflexo-http/blob/1.8.1/http-server/src/main/resources/webroot/js/openflexo/api/resource.ts
[2]:	https://github.com/openflexo-team/openflexo-http/blob/1.8.1/http-server/src/main/resources/webroot/js/openflexo/api/resource.ts
[3]:	https://github.com/openflexo-team/openflexo-http/blob/1.8.1/http-server/src/main/resources/webroot/js/openflexo/api/resource.ts
[4]:	ComplementTA.md
[5]:	https://github.com/openflexo-team/openflexo-http/blob/1.9.0/http-server/src/main/resources/webroot/js/openflexo/api/fml.d.ts
[6]:	https://server.openflexo.org/ta/fml/model
[7]:	https://server.openflexo.org/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s
[8]:	https://server.openflexo.org/ta/fml/model/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW8uZm1s/object
[9]:	https://github.com/openflexo-team/openflexo-http/blob/1.9.0/http-server/src/main/resources/webroot/js/openflexo/api/fml.d.ts
[10]:	https://server.openflexo.org/ta/fmlrt/instance
[11]:	https://server.openflexo.org/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0
[12]:	https://server.openflexo.org/ta/fmlrt/instance/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L2RlbW8uZm1sLnJ0/object
[13]:	https://github.com/openflexo-team/openflexo-http/blob/1.9.0/http-server/src/main/resources/webroot/js/openflexo/api/fib.d.ts
[14]:	https://server.openflexo.org/ta/gina/fib
[15]:	https://server.openflexo.org/resource/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI
[16]:	https://server.openflexo.org/ta/gina/fib/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI/object
[17]:	https://server.openflexo.org/ta/gina/fib/aHR0cDovL3d3dy5vcGVuZmxleG8ub3JnL3Byb2plY3RzLzIwMTcvOS9VbnRpdGxlZF8xNTA1OTE4MjE5OTg5L0RlbW9VSS5maWI/object/1