package org.openflexo.http.server.core.validators;

import io.vertx.ext.web.FileUpload;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.rt.logging.FMLConsole;
import org.openflexo.http.server.core.exceptions.BadValidationException;
import org.openflexo.http.server.core.helpers.Helpers;
import org.openflexo.http.server.util.IdUtils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A Generic Validator class
 *
 * @author Ihab Benamer
 */
public abstract class GenericValidator {
    private final String[] primitiveTypes   = {"string", "date", "boolean", "integer", "byte", "long", "short", "float", "double"};
    private final String[] visibilities     = {"public", "protected", "default", "private"};
    private final String[] behaviourTypes   = {"action", "cloning", "creation", "deletion", "event", "synchronization", "navigation"};
    private final String[] cardinalities    = {"one", "zeromany", "onemany", "zeroone"};
    private final String[] logLevels        = {"info", "severe", "warning", "fine", "finer", "finest", "debug"};
    private final String[] formats          = {"application/zip"};
    private final long MAX_UPLOAD_SIZE      = 50 * 1000 * 1000;

    /**
     * If the field is not null and not empty, then if the field is equal to "true" or "1", return true, else if the field
     * is equal to "false" or "0", return false, else throw an exception
     *
     * @param field The field to validate
     * @return A boolean value
     */
    public boolean validateBoolean(String field) throws BadValidationException {
        if(field != null && !field.isEmpty()){
            if(field.equalsIgnoreCase("true") || field.equals("1")){
                return true;
            } else if(field.equalsIgnoreCase("false") || field.equals("0")){
                return false;
            } else {
                throw new BadValidationException("Invalid value");
            }
        } else {
            return false;
        }
    }

    /**
     * If the name is not null and not empty, and if the name does not contain a space, then return the name. Otherwise,
     * throw an exception
     *
     * @param name The name of the parameter to validate.
     * @return A string
     */
    public String validateName(String name) throws BadValidationException {
        if(name != null && !name.isEmpty()){
            if(name.contains(" ")){
                throw new BadValidationException("Invalid value");
            } else {
                return name;
            }
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * If the name is not null and not empty, return the name, otherwise throw a BadValidationException with the message
     * 'Field required'.
     *
     * @param name The name of the field to validate.
     * @return A string
     */
    public String validateString(String name) throws BadValidationException {
        if(name != null && !name.isEmpty()){
            return name;
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * If the field is not null and not empty, check if it's a valid primitive type, and if it is, return the primitive
     * type. If it's not, throw an exception
     *
     * @param field The field to validate
     * @return A PrimitiveType object
     */
    public PrimitiveType validatePrimitiveType(String field) throws BadValidationException {
        if(field != null && !field.isEmpty()){
            if(!Arrays.asList(primitiveTypes).contains(field.toLowerCase())){
                throw new BadValidationException("Invalid value");
            } else {
                return Helpers.getPrimitiveType(field);
            }
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * If the field is not null and not empty, check if it's a valid cardinality, and if it is, return the cardinality. If
     * it's not, throw an exception
     *
     * @param field The value to be validated
     * @return PropertyCardinality
     */
    public PropertyCardinality validateCardinality(String field) throws BadValidationException {
        if(field != null && !field.isEmpty()){
            if(!Arrays.asList(cardinalities).contains(field.toLowerCase())){
                throw new BadValidationException("Invalid value");
            } else {
                return Helpers.getCardinality(field);
            }
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * If the field is not null and not empty, check if it's a valid visibility, and if it is, return the visibility. If
     * it's not, throw an exception
     *
     * @param field The field to validate
     * @return A Visibility object
     */
    public Visibility validateVisibility(String field) throws BadValidationException {
        if(field != null && !field.isEmpty()){
            if(!Arrays.asList(visibilities).contains(field.toLowerCase())){
                throw new BadValidationException("Invalid value");
            } else {
                return Helpers.getVisibility(field);
            }
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * It checks if the value of the field is a valid behaviour type, and if it is, it returns the corresponding class
     *
     * @param field the value to validate
     * @return The class of the behaviour type
     */
    public Class<? extends FlexoBehaviour> validateBehaviourType(String field) throws BadValidationException {
        if(field != null && !field.isEmpty()){
            if(!Arrays.asList(behaviourTypes).contains(field.toLowerCase())){
                throw new BadValidationException("Invalid value");
            } else {
                return Helpers.getBehaviourType(field);
            }
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * If the path is null or empty, throw a BadValidationException with the message "Field required". Otherwise, if the
     * path doesn't exist, throw a BadValidationException with the message "invalid value". Otherwise, return the path
     *
     * @param path the path to validate
     * @return The path
     */
    public String validatePath(String path) throws BadValidationException {
        if(path == null || path.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            File f = new File("src/main/resources/" + path);

            if(!Files.exists(Paths.get(f.getAbsolutePath()))){
                f.mkdirs();
            }

            return path;
        }
    }

    /**
     * It checks if the file is too large, if the file format is supported, and if the file name is encoded in UTF-8
     *
     * @param fileUploadIterator This is the iterator that contains the file uploads.
     */
    public void validateFileUpload(Iterator<FileUpload> fileUploadIterator) throws  BadValidationException {
        FileUpload fileUpload = fileUploadIterator.next();
        try {
            URLDecoder.decode(fileUpload.fileName(), "UTF-8");

            if(fileUpload.size() > MAX_UPLOAD_SIZE){
                throw new BadValidationException("File too large (files must be smaller than " + MAX_UPLOAD_SIZE + " byte)");
            }

            if(!Arrays.asList(formats).contains(fileUpload.contentType())){
                throw new BadValidationException("Unsupported file format");
            }
        } catch (UnsupportedEncodingException e) {
            throw new BadValidationException("Unsupported file name encoding");
        }
    }

    /**
     * If the description is not null and not empty, return the trimmed description. Otherwise, return the description
     *
     * @param description The description of the item.
     * @return The description is being returned.
     */
    public String validateDescription(String description){
        if (description != null && !description.isEmpty())
            return description.trim();
        return description;
    }

    public DataBinding<Object> validateObjectValue(String field, String type) throws BadValidationException {
        // TODO: cast default value to other formats
        return null;
    }

    /**
     * If the value is not null or empty, check if it's in the list of valid log levels. If it is, return the log level. If
     * it's not, throw an exception
     *
     * @param level The value to validate
     * @return A FMLConsole.LogLevel
     */
    public FMLConsole.LogLevel validateLogLevel(String level) throws BadValidationException {
        if(level != null && !level.isEmpty()){
            if(!Arrays.asList(logLevels).contains(level.toLowerCase())){
                throw new BadValidationException("Invalid value");
            } else {
                return Helpers.getLogLevel(level);
            }
        } else {
            throw new BadValidationException("Field required");
        }
    }

    /**
     * It takes a concept id and a virtual model library, and returns the corresponding concept. If the concept id is null
     * or empty, it throws a BadValidationException. If the concept id is not null or empty, but the concept does not
     * exist, it throws a BadValidationException
     *
     * @param conceptId the id of the concept to validate
     * @param virtualModelLibrary the VirtualModelLibrary where the FlexoConcept is located
     * @return A FlexoConcept
     */
    public FlexoConcept validateConcept(String conceptId, VirtualModelLibrary virtualModelLibrary) throws BadValidationException {
        if(conceptId == null || conceptId.isEmpty()){
            throw new BadValidationException("Field required");
        } else {
            FlexoConcept tContainer = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(conceptId), true);
            if (tContainer != null) {
                return tContainer;
            } else {
                throw new BadValidationException("Invalid value");
            }
        }
    }

    /**
     * It takes a concept id and a virtual model library, and returns the corresponding concept. If the concept id is null
     * or empty, it returns null. If the concept id is not null or empty, but the corresponding concept cannot be found, it
     * throws a BadValidationException
     *
     * @param conceptId the id of the concept to validate
     * @param virtualModelLibrary the VirtualModelLibrary where the FlexoConcept is to be created
     * @return A FlexoConcept
     */
    public FlexoConcept validateParentConcept(String conceptId, VirtualModelLibrary virtualModelLibrary) throws BadValidationException {
        if(conceptId == null || conceptId.isEmpty()){
            return null;
        } else {
            FlexoConcept tContainer = virtualModelLibrary.getFlexoConcept(IdUtils.decodeId(conceptId), true);
            if (tContainer != null) {
                return tContainer;
            } else {
                throw new BadValidationException("Invalid value");
            }
        }
    }
}
