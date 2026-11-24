# openflexo-server — Backlog

Features and evolutions to develop in openflexo-server, near to long term. Known bugs go to `KNOWN_DEFECTS.md`
(identifiers `SERVER-D-<n>`), not here.

Identifiers are stable and never reused: `SERVER-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## MCP

### SERVER-F-1 — Expose a configured set of Openflexo services as an MCP server  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-server#2](https://github.com/openflexo-team/openflexo-server/issues/2)

**Problem.** Openflexo can be a client of MCP servers, through the `mcp` technology adapter (a `.mcp` resource describes a server whose tools FML calls), but it cannot be an MCP server. The services that an Openflexo installation provides cannot be reached by an MCP client, such as an AI assistant or another tool, although the server already exposes Openflexo over HTTP.

Verified on the current `3.0` branch: `openflexo-server` provides `http-server` (a Vert.x server with REST controllers for resource centers, virtual model instances and concept instances, a Connie WebSocket service, and `TechnologyAdapterRouteService`) and `gina-http`. There is nothing that speaks MCP as a server. The `mcp` adapter works the other way round. `openflexo-mcp` is not part of this workspace yet. No existing entry covers it.

**Goal.** Expose a set of Openflexo services, as described by a server configuration, through the Model Context Protocol, so that an MCP client can discover and use them.

**Open questions.** Nothing is decided yet; the design is part of this entry.
- What a service is: a behaviour of a virtual model, an action, a fetch, a resource. Which part of the model a server configuration exposes (which virtual models, behaviours, resources), and where the configuration is declared (a file, FML, annotations).
- Mapping to MCP: which concepts are used (tools to call behaviours, resources to read objects, prompts), and whether the schema of a tool is derived from the signature of a behaviour (name, typed parameters, `throws`).
- Transport: stdio, HTTP with server-sent events, streamable HTTP.
- Security: authentication, authorization, and filtering of what is exposed.
- Relation with the existing server: built on the REST controllers of `http-server`, or a separate module; whether the MCP SDK becomes a new dependency.
- Errors: an FML error raised by a service should become an MCP tool error, following the rules of `CORE-F-11` (one report, FML stack).
- Relation with the `mcp` technology adapter and its entries (`T-MCP-1`, `T-MCP-2`).

**Acceptance criteria** (to be refined once the design is chosen).
- A server configuration declares the services to expose, and the server starts with it.
- An MCP client can list the exposed services and call one of them, with typed parameters and a result.
- A failing service answers an MCP error and leaves nothing half modified (see `SERVER-D-1`).
- Only the services listed in the configuration are reachable.
- An end-to-end test calls an exposed service from an MCP client, for example the `mcp` technology adapter.
