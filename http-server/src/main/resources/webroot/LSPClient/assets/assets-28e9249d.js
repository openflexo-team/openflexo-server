let t={};function n(r){t={...t,...r}}function o(r){const e=t[r]??t[r.replace(/[/.]/g,"_")];return new URL(e,window.location.href).toString()}window.monacoRequire={toUrl:o};export{n as r};
