
"use strict";

module openflexo {
    
    export class Description {
        name: string;
        id: string;
        url: string;
        type: string;
    }

    export class ResourceCenter extends Description {
        uri: string;
    }

    export class Resource extends Description{
        uri: string;
        resourceCenterId: string;
        resourceCenterUrl: string;
    }

    export class TechnologyAdapter extends Description {
    }

    function error(url: string) {
        console.log("Error can't access " + url + '", check that it exists and is accessible');
    }

    export function resourceCenters(callback: (centers: ResourceCenter[]) => void) {
        call("http://localhost:8080/rc", callback);
    }

    export function resources(callback: (resources: Resource[]) => void) {
        call("http://localhost:8080/resource", callback);
    }

    export function technologyAdapters(callback: (tas: TechnologyAdapter[]) => void) {
        call("http://localhost:8080/ta", callback);
    }

    export function call<T>(url: string, callback: (result: T) => void) {
        let request = new XMLHttpRequest();
        request.open("get", url);
        request.onload = (ev) => {
            if (request.status >= 200 && request.status < 300) {
                callback(<T>JSON.parse(request.responseText));
            }
		}
        request.onerror = (ev) => {
            error(url);
		};
        request.send();
    }
}

function getDataUrl(element : HTMLElement) {
    let current = element;
    let dataUrl = current.getAttribute("data-url");
    while (dataUrl === null) {
        current = current.parentElement;
        if (!current) { break; }
        dataUrl = current.getAttribute("data-url");
    }
    return dataUrl;
}

function getDescriptionDiv(element: HTMLElement) {
    let current = element;
    let dataUrl = current.getAttribute("data-url");
    while (dataUrl === null) {
        current = current.parentElement;
        if (!current) { break; }
        dataUrl = current.getAttribute("data-url");
    }
    return current;   
}

function showDetails(event: MouseEvent) {
    let description = getDescriptionDiv(<HTMLElement>event.target);
    let details = <HTMLElement>description.nextSibling;
    if (details.style.display === "block") {
        details.style.display = 'none'
    } else {
        details.style.display = 'block'
    }
    event.preventDefault();
}

function createCount(source : any[]) {
    let count = document.createElement("span")
    count.innerText = `Found ${source.length} elements`;
    return count;
}

function createDescriptionElement(source: openflexo.Description) {
    let description = <HTMLDivElement> document.createElement("div");
    description.setAttribute("data-url", source.url);
    description.onclick = showDetails;
    
    let a = <HTMLAnchorElement> document.createElement("a");
    a.href = source.url;
    a.text = source.name;
    description.appendChild(a);

    return description;
}

function createPreElement(source: any) {
    var all = document.createElement("div");
    all.className = 'details';

    for (let key of Object.keys(source)) {
        let element = document.createElement("div");
        
        let keySpan = document.createElement("b");
        keySpan.innerText = `${key}: `;
        element.appendChild(keySpan);

        if (key.length >= 3 && key.substr(-3).toLowerCase() === "url") {
            let valueCode = document.createElement("a");
            valueCode.href = source[key];
            valueCode.innerText = source[key];
            element.appendChild(valueCode);
        } else {
            let valueCode = document.createElement("code");
            valueCode.innerText = source[key];
            element.appendChild(valueCode);
        }

        all.appendChild(element);
    }

    return all;
}

openflexo.technologyAdapters((tas) => {
    let div = document.querySelector("#tas");
    div.appendChild(createCount(tas));
    for (let ta of tas) {
        div.appendChild(createDescriptionElement(ta));
        div.appendChild(createPreElement(ta))
    }
});

openflexo.resourceCenters((centers) => {
    let div = document.querySelector("#centers");
    div.appendChild(createCount(centers));
    for (let center of centers) {
        div.appendChild(createDescriptionElement(center));
        div.appendChild(createPreElement(center))
    }
});

openflexo.resources((resources) => {
    let div = document.querySelector("#resources");
    div.appendChild(createCount(resources));
    for (let resource of resources) {
        div.appendChild(createDescriptionElement(resource));
        div.appendChild(createPreElement(resource))
    }
});