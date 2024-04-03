package org.openflexo.http.server.core.validators;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.util.IdUtils;

import java.io.FileNotFoundException;

/**
 * The Properties validator class.
 *
 * @author Ihab Benamer
 */
public class PropertiesValidator extends GenericValidator {
    private final VirtualModelLibrary virtualModelLibrary;
    private final HttpServerRequest request;
    private boolean isValid;
    private JsonArray errors;
    private String name;
    private PrimitiveType type;
    private PropertyCardinality cardinality;
    private String description;
    private FlexoConcept concept;
    private TechnologyAdapter<?> technologyAdapter;
    private boolean required;
    private boolean readOnly;
    private CompilationUnitResource virtualModelResource;

    /**
     * Instantiates a new property validator.
     *
     * @param request the request
     */
    public PropertiesValidator(HttpServerRequest request, VirtualModelLibrary virtualModelLibrary) {
        this.virtualModelLibrary    = virtualModelLibrary;
        this.request                = request;
        isValid                     = false;
    }

    public TechnologyAdapter<?> validateTechnologyAdapter(String ta) throws BadValidationException {
        if(ta == null || ta.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            TechnologyAdapter<?> technonlogyAdapter = Helpers.getTechnologyAdapterClass(ta, virtualModelLibrary.getServiceManager());

            if (technonlogyAdapter != null) {
                return technonlogyAdapter;
            } else {
                throw new BadValidationException("Invalid value");
            }
        }
    }

    /**
     * It validates the form data and returns a JsonArray of errors
     *
     * @return A JsonArray of JsonObjects.
     */
    public JsonArray validatePrimitive(){
        String rName        = request.getFormAttribute("name").trim();
        String rType        = request.getFormAttribute("type");
        String rCardinality = request.getFormAttribute("cardinality");
        String rDescription = request.getFormAttribute("description");
        errors              = new JsonArray();

        JsonObject errorLine;

        try{
            name = validateName(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        try{
            type = validatePrimitiveType(rType);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("type", e.getMessage());
            errors.add(errorLine);
        }

        try{
            cardinality = validateCardinality(rCardinality);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("cardinality", e.getMessage());
            errors.add(errorLine);
        }

        description = validateDescription(rDescription);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    public CompilationUnitResource validateVirtualModelResource(String vmid) throws BadValidationException {
        if(vmid == null || vmid.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            try{
                VirtualModel model = virtualModelLibrary.getVirtualModel(IdUtils.decodeId(vmid));
                if(model == null){
                    throw new BadValidationException("Invalid value");
                } else {
                    return model.getResource();
                }
            } catch (ResourceLoadingCancelledException | FlexoException | FileNotFoundException e){
                throw new BadValidationException("Invalid value");
            }
        }
    }

    /**
     * It validates the form data and returns a JSON array of errors
     *
     * @return A JsonArray of JsonObjects.
     */
    public JsonArray validateConceptInstanceRole(){
        String rName        = request.getFormAttribute("name").trim();
        String rConceptId   = request.getFormAttribute("concept_id");
        String rCardinality = request.getFormAttribute("cardinality");
        String rDescription = request.getFormAttribute("description");
        errors              = new JsonArray();
        JsonObject errorLine;

        try{
            name = validateName(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        try{
            concept = validateConcept(rConceptId, virtualModelLibrary);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("concept_id", e.getMessage());
            errors.add(errorLine);
        }

        try{
            cardinality = validateCardinality(rCardinality);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("cardinality", e.getMessage());
            errors.add(errorLine);
        }

        description = validateDescription(rDescription);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    public JsonArray validateModelSlot(){
        String rName            = request.getFormAttribute("name").trim();
        String rDescription     = request.getFormAttribute("description");
        String rRequired        = request.getFormAttribute("required");
        String rReadOnly        = request.getFormAttribute("read_only");
        String rTA              = request.getFormAttribute("technology_adapter");
        String rVirtualModel    = request.getFormAttribute("virtual_model_id");
        errors                  = new JsonArray();
        JsonObject errorLine;

        try{
            name = validateName(rName);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("name", e.getMessage());
            errors.add(errorLine);
        }

        try{
            required = validateBoolean(rRequired);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("required", e.getMessage());
            errors.add(errorLine);
        }

        try{
            readOnly = validateBoolean(rReadOnly);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("read_only", e.getMessage());
            errors.add(errorLine);
        }

        try{
            technologyAdapter = validateTechnologyAdapter(rTA);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("technology_adapter", e.getMessage());
            errors.add(errorLine);
        }

        try{
            virtualModelResource = validateVirtualModelResource(rVirtualModel);
        } catch (BadValidationException e){
            errorLine = new JsonObject();
            errorLine.put("virtual_model_id", e.getMessage());
            errors.add(errorLine);
        }

        description = validateDescription(rDescription);

        if(errors.isEmpty())
            isValid = true;

        return errors;
    }

    /**
     * Returns true if the object is valid, false otherwise.
     * 
     * @return The boolean value of isValid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * This method returns the name of the property.
     * 
     * @return The name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this primitive.
     * 
     * @return The type of the variable.
     */
    public PrimitiveType getType() {
        return type;
    }

    /**
     * Returns the cardinality of the property
     * 
     * @return The cardinality of the property.
     */
    public PropertyCardinality getCardinality() {
        return cardinality;
    }

    /**
     * This method returns the description of the property
     * 
     * @return The description of the item.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the concept of this object
     *
     * @return The concept of the current object.
     */
    public FlexoConcept getConcept() {
        return concept;
    }

    /**
     * This function returns the technology adapter of the current object
     *
     * @return The technology adapter.
     */
    public TechnologyAdapter<?> getTechnologyAdapter() {
        return technologyAdapter;
    }

    /**
     * Returns true if the field is required
     *
     * @return The value of the required variable.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns true if this is a read-only database; false otherwise.
     *
     * @return The value of the readOnly variable.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * This function returns the VirtualModelResource object that is associated with the VirtualModel object
     *
     * @return The VirtualModelResource
     */
    public CompilationUnitResource getVirtualModelResource() {
        return virtualModelResource;
    }
}
