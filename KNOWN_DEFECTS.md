# openflexo-server — known defects

Known bugs, not fixed yet. Identifiers `SERVER-D-<n>`, never reused; fixed entries move to `FIXED_DEFECTS.md`.

## REST API

### SERVER-D-1 — The edits of a failed FML action stay in memory, and the next successful request saves them  ·  `TODO`

**Symptom (traced by the review of CORE-F-11 phase 5, 2026-11-15; read, not run).** Since CORE-F-11 phase 5 a failing action
(`GenericController.performAction`: creating a concept instance, a virtual model instance, running a behaviour) answers its FML error
and saves nothing. But the editor of the server (`Helpers.getDefaultFlexoEditor`, a `DefaultFlexoEditor`) has no undo manager: a
behaviour that set properties before failing leaves them set in memory, and a failed creation scheme leaves its new virtual model
instance resource registered. The next successful request on the same resource saves those edits.

**To reproduce.** A behaviour that assigns a property, then fails (a `log` of an unresolved expression); call `executeBehaviour`
(400), then a successful behaviour on the same instance; the property of the first call is saved.

**Fix direction.** Give the server editor an undo manager and undo the edits of a failed action before answering, as
`InteractiveFlexoEditor` does (CORE-F-11 phase 4); or discard the resource of a failed creation.
