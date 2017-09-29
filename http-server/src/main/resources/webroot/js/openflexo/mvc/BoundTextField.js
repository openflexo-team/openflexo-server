define(["require", "exports", "./BoundComponent", "../ui/TextField", "./utils"], function (require, exports, BoundComponent_1, TextField_1, utils_1) {
    "use strict";
    var idSeed = 0;
    class BoundTextField extends BoundComponent_1.BoundComponent {
        constructor(api, binding, label = null, runtime = null, floatingLabel = false, invalid = false, id = null) {
            super(api);
            this.binding = binding;
            this.label = label;
            this.runtime = runtime;
            this.floatingLabel = floatingLabel;
            this.invalid = invalid;
            this.id = id;
            this.runtimeBinding = null;
            this.changelistener = (value) => this.updateValue(value);
            this.create();
            this.updateRuntime(runtime);
        }
        create() {
            let actualId = this.id !== null ? this.id : "boundTextField" + idSeed++;
            this.textField = new TextField_1.TextField(actualId, this.binding.expression, this.label, this.floatingLabel, this.invalid);
            let input = this.textField.input;
            input.onchange = (e) => this.sendToServer(e);
            input.onblur = (e) => this.sendToServer(e);
            this.container = this.textField.container;
        }
        sendToServer(e) {
            if (this.runtimeBinding !== null) {
                this.api.assign(this.runtimeBinding, this.textField.input.value, false).then(value => {
                    this.container.classList.remove("is-invalid");
                }).catch(error => {
                    this.container.classList.add("is-invalid");
                });
            }
        }
        updateValue(value) {
            this.textField.input.value = value;
        }
        updateRuntime(runtime, extensions = new Map()) {
            super.updateRuntime(runtime, extensions);
            this.runtimeBinding = utils_1.updateBindingRuntime(this.api, this.binding, this.runtimeBinding, this.changelistener, runtime, extensions);
        }
        isEnable() {
            return this.textField.isEnable();
        }
        setEnable(enable) {
            this.textField.setEnable(enable);
        }
    }
    exports.BoundTextField = BoundTextField;
});
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiQm91bmRUZXh0RmllbGQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyJCb3VuZFRleHRGaWVsZC50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiOztJQVFBLElBQUksTUFBTSxHQUFHLENBQUMsQ0FBQztJQUVmLG9CQUE0QixTQUFRLCtCQUFjO1FBVTlDLFlBQ0ksR0FBUSxFQUNBLE9BQTBCLEVBQ2pCLFFBQStCLElBQUksRUFDNUMsVUFBdUIsSUFBSSxFQUNsQixnQkFBeUIsS0FBSyxFQUM5QixVQUFtQixLQUFLLEVBQ3hCLEtBQWtCLElBQUk7WUFFdkMsS0FBSyxDQUFDLEdBQUcsQ0FBQyxDQUFDO1lBUEgsWUFBTyxHQUFQLE9BQU8sQ0FBbUI7WUFDakIsVUFBSyxHQUFMLEtBQUssQ0FBOEI7WUFDNUMsWUFBTyxHQUFQLE9BQU8sQ0FBb0I7WUFDbEIsa0JBQWEsR0FBYixhQUFhLENBQWlCO1lBQzlCLFlBQU8sR0FBUCxPQUFPLENBQWlCO1lBQ3hCLE9BQUUsR0FBRixFQUFFLENBQW9CO1lBWG5DLG1CQUFjLEdBQWtDLElBQUksQ0FBQztZQUU1QyxtQkFBYyxHQUFHLENBQUMsS0FBSyxLQUFLLElBQUksQ0FBQyxXQUFXLENBQUMsS0FBSyxDQUFDLENBQUM7WUFZakUsSUFBSSxDQUFDLE1BQU0sRUFBRSxDQUFDO1lBQ2QsSUFBSSxDQUFDLGFBQWEsQ0FBQyxPQUFPLENBQUMsQ0FBQztRQUNoQyxDQUFDO1FBRUQsTUFBTTtZQUNGLElBQUksUUFBUSxHQUFHLElBQUksQ0FBQyxFQUFFLEtBQUssSUFBSSxHQUFHLElBQUksQ0FBQyxFQUFFLEdBQUcsZ0JBQWdCLEdBQUMsTUFBTSxFQUFFLENBQUM7WUFDdEUsSUFBSSxDQUFDLFNBQVMsR0FBRyxJQUFJLHFCQUFTLENBQzFCLFFBQVEsRUFBRSxJQUFJLENBQUMsT0FBTyxDQUFDLFVBQVUsRUFBRSxJQUFJLENBQUMsS0FBSyxFQUM3QyxJQUFJLENBQUMsYUFBYSxFQUFFLElBQUksQ0FBQyxPQUFPLENBQ25DLENBQUM7WUFFRixJQUFJLEtBQUssR0FBRyxJQUFJLENBQUMsU0FBUyxDQUFDLEtBQUssQ0FBQztZQUNqQyxLQUFLLENBQUMsUUFBUSxHQUFHLENBQUMsQ0FBQyxLQUFLLElBQUksQ0FBQyxZQUFZLENBQUMsQ0FBQyxDQUFDLENBQUM7WUFDN0MsS0FBSyxDQUFDLE1BQU0sR0FBRyxDQUFDLENBQUMsS0FBSyxJQUFJLENBQUMsWUFBWSxDQUFDLENBQUMsQ0FBQyxDQUFDO1lBRTNDLElBQUksQ0FBQyxTQUFTLEdBQUcsSUFBSSxDQUFDLFNBQVMsQ0FBQyxTQUFTLENBQUM7UUFDOUMsQ0FBQztRQUVPLFlBQVksQ0FBQyxDQUFNO1lBQ3ZCLEVBQUUsQ0FBQyxDQUFDLElBQUksQ0FBQyxjQUFjLEtBQUssSUFBSSxDQUFDLENBQUMsQ0FBQztnQkFDL0IsSUFBSSxDQUFDLEdBQUcsQ0FBQyxNQUFNLENBQUMsSUFBSSxDQUFDLGNBQWMsRUFBRSxJQUFJLENBQUMsU0FBUyxDQUFDLEtBQUssQ0FBQyxLQUFLLEVBQUUsS0FBSyxDQUFDLENBQUMsSUFBSSxDQUFDLEtBQUs7b0JBQzlFLElBQUksQ0FBQyxTQUFTLENBQUMsU0FBUyxDQUFDLE1BQU0sQ0FBQyxZQUFZLENBQUMsQ0FBQztnQkFDbEQsQ0FBQyxDQUFDLENBQUMsS0FBSyxDQUFDLEtBQUs7b0JBQ1YsSUFBSSxDQUFDLFNBQVMsQ0FBQyxTQUFTLENBQUMsR0FBRyxDQUFDLFlBQVksQ0FBQyxDQUFDO2dCQUMvQyxDQUFDLENBQUMsQ0FBQztZQUNQLENBQUM7UUFDTCxDQUFDO1FBRU8sV0FBVyxDQUFDLEtBQVU7WUFDMUIsSUFBSSxDQUFDLFNBQVMsQ0FBQyxLQUFLLENBQUMsS0FBSyxHQUFHLEtBQUssQ0FBQztRQUN2QyxDQUFDO1FBRUQsYUFBYSxDQUFDLE9BQW9CLEVBQUUsVUFBVSxHQUFHLElBQUksR0FBRyxFQUFrQjtZQUN4RSxLQUFLLENBQUMsYUFBYSxDQUFDLE9BQU8sRUFBRSxVQUFVLENBQUMsQ0FBQztZQUV6QyxJQUFJLENBQUMsY0FBYyxHQUFHLDRCQUFvQixDQUN4QyxJQUFJLENBQUMsR0FBRyxFQUFFLElBQUksQ0FBQyxPQUFPLEVBQUUsSUFBSSxDQUFDLGNBQWMsRUFDM0MsSUFBSSxDQUFDLGNBQWMsRUFBRSxPQUFPLEVBQUUsVUFBVSxDQUN6QyxDQUFDO1FBQ0osQ0FBQztRQUVELFFBQVE7WUFDTixNQUFNLENBQUMsSUFBSSxDQUFDLFNBQVMsQ0FBQyxRQUFRLEVBQUUsQ0FBQztRQUNuQyxDQUFDO1FBRUQsU0FBUyxDQUFDLE1BQWU7WUFDckIsSUFBSSxDQUFDLFNBQVMsQ0FBQyxTQUFTLENBQUMsTUFBTSxDQUFDLENBQUM7UUFDckMsQ0FBQztLQUNKO0lBcEVELHdDQW9FQyJ9