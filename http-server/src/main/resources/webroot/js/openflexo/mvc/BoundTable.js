define(["require", "exports", "./BoundComponent", "./utils", "../ui/Table"], function (require, exports, BoundComponent_1, utils_1, Table_1) {
    "use strict";
    class BoundTable extends BoundComponent_1.BoundComponent {
        constructor(api, elements, columns, runtime = null, header = true, selectable = false) {
            super(api);
            this.elements = elements;
            this.columns = columns;
            this.header = header;
            this.selectable = selectable;
            this.lines = [];
            this.elementsRuntimeBinding = null;
            this.elementsChangelistener = (elements) => this.updateList(elements);
            this.create();
        }
        create() {
            this.table = new Table_1.Table(this.selectable);
            if (this.header) {
                let header = new Table_1.TableLine();
                this.columns.forEach(column => {
                    let cell = new Table_1.TableCell(column.name);
                    header.addCell(cell);
                });
                this.table.head.addLine(header);
            }
            this.container = this.table.container;
        }
        updateRuntime(runtime, extensions = new Map()) {
            super.updateRuntime(runtime, extensions);
            this.elementsRuntimeBinding = utils_1.updateBindingRuntime(this.api, this.elements, this.elementsRuntimeBinding, this.elementsChangelistener, runtime, extensions);
        }
        updateList(elements) {
            let body = this.table.body;
            let lineCount = 0;
            let elementCount = 0;
            // checks elements with current lines
            while (lineCount < this.lines.length && elementCount < elements.length) {
                let line = this.lines[lineCount];
                let element = elements[elementCount];
                if (line.url !== element.url) {
                    // line if different from current element
                    // removes the line current line
                    this.removeLine(lineCount);
                }
                else {
                    // line is the same checks next
                    lineCount += 1;
                    elementCount += 1;
                }
            }
            // removes the rest of the lines if any
            while (lineCount < this.lines.length) {
                this.removeLine(lineCount);
            }
            // adds the rest of elements if any
            while (elementCount < elements.length) {
                this.addLine(elements[elementCount]);
                elementCount += 1;
            }
        }
        addLine(element) {
            let line = new Table_1.TableLine();
            this.columns.forEach(column => {
                let component = column.component(this.api, element);
                let cell = new Table_1.TableCell(component);
                line.addCell(cell);
            });
            this.lines.push(new BoundTableLine(element.url, line));
            this.table.body.addLine(line);
        }
        removeLine(index) {
            let line = this.lines[index];
            this.lines.splice(index, 1);
            this.table.body.removeLine(line.line);
        }
        setEnable(enable) {
            this.table.setEnable(false);
        }
    }
    exports.BoundTable = BoundTable;
    class BoundColumn {
        constructor(name, component) {
            this.name = name;
            this.component = component;
        }
    }
    exports.BoundColumn = BoundColumn;
    class BoundTableLine {
        constructor(url, line) {
            this.url = url;
            this.line = line;
        }
    }
});
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiQm91bmRUYWJsZS5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIkJvdW5kVGFibGUudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6Ijs7SUFTQSxnQkFBd0IsU0FBUSwrQkFBYztRQVkxQyxZQUNJLEdBQVEsRUFDQSxRQUF1QyxFQUN2QyxPQUFzQixFQUM5QixVQUF3QixJQUFJLEVBQ3BCLFNBQWtCLElBQUksRUFDdEIsYUFBc0IsS0FBSztZQUVuQyxLQUFLLENBQUMsR0FBRyxDQUFDLENBQUM7WUFOSCxhQUFRLEdBQVIsUUFBUSxDQUErQjtZQUN2QyxZQUFPLEdBQVAsT0FBTyxDQUFlO1lBRXRCLFdBQU0sR0FBTixNQUFNLENBQWdCO1lBQ3RCLGVBQVUsR0FBVixVQUFVLENBQWlCO1lBZHZDLFVBQUssR0FBcUIsRUFBRSxDQUFDO1lBSXJCLDJCQUFzQixHQUE4QyxJQUFJLENBQUM7WUFFaEUsMkJBQXNCLEdBQUcsQ0FBQyxRQUFRLEtBQUssSUFBSSxDQUFDLFVBQVUsQ0FBQyxRQUFRLENBQUMsQ0FBQztZQVc5RSxJQUFJLENBQUMsTUFBTSxFQUFFLENBQUM7UUFDbEIsQ0FBQztRQUVTLE1BQU07WUFDWixJQUFJLENBQUMsS0FBSyxHQUFHLElBQUksYUFBSyxDQUFDLElBQUksQ0FBQyxVQUFVLENBQUMsQ0FBQztZQUV4QyxFQUFFLENBQUMsQ0FBQyxJQUFJLENBQUMsTUFBTSxDQUFDLENBQUMsQ0FBQztnQkFDZCxJQUFJLE1BQU0sR0FBRyxJQUFJLGlCQUFTLEVBQUUsQ0FBQztnQkFDN0IsSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLENBQUUsTUFBTTtvQkFDeEIsSUFBSSxJQUFJLEdBQUcsSUFBSSxpQkFBUyxDQUFDLE1BQU0sQ0FBQyxJQUFJLENBQUMsQ0FBQztvQkFDdEMsTUFBTSxDQUFDLE9BQU8sQ0FBQyxJQUFJLENBQUMsQ0FBQztnQkFDekIsQ0FBQyxDQUFDLENBQUM7Z0JBQ0gsSUFBSSxDQUFDLEtBQUssQ0FBQyxJQUFJLENBQUMsT0FBTyxD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