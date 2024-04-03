package org.openflexo.http.server.core.helpers;

import io.vertx.core.buffer.Buffer;
import jline.internal.Log;
import org.apache.commons.io.FileUtils;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.*;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.action.PropertyEntry;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.logging.FMLConsole;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.http.server.core.RestTerminal;
import org.openflexo.toolbox.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *  Generic Helpers.
 *
 * @author Ihab Benamer
 */
public class Helpers {

    private static final String WORKSPACE = "src/main/resources/";
    private static DefaultFlexoEditor defaultFlexoEditor;

    /**
     * If the default editor is null, create a new one and return it.
     *
     * @param virtualModelLibrary the VirtualModelLibrary instance that will be used to load the VirtualModel
     * @return A new instance of DefaultFlexoEditor
     */
    public static DefaultFlexoEditor getDefaultFlexoEditor(VirtualModelLibrary virtualModelLibrary){
        if (defaultFlexoEditor == null)
            defaultFlexoEditor = new DefaultFlexoEditor(null, virtualModelLibrary.getServiceManager());

        return defaultFlexoEditor;
    }

    /**
     * It creates a URI for a virtual model in a project
     *
     * @param project the project in which the virtual model will be created
     * @param name the name of the virtual model
     * @return A string
     */
    public static String createVirtualModelUri(FlexoProject<?> project, String name) {
        return project.getProjectURI() + "/" + name + ".fml";
    }

    /**
     * If the visibility is null, set it to an empty string. If the visibility is public, protected, or private, return the
     * corresponding Visibility enum value. Otherwise, return the default Visibility enum value.
     *
     * @param visibility The visibility of the method.
     * @return A visibility object
     */
    public static Visibility getVisibility(String visibility) {
        if (visibility == null)
            visibility = "";

        switch (visibility.toLowerCase()) {
            case "public":
                return Visibility.Public;
            case "protected":
                return Visibility.Protected;
            case "private":
                return Visibility.Private;
            default:
                return Visibility.Default;
        }
    }

    /**
     * It takes a string and returns a PropertyEntry.PropertyType
     *
     * @param type the type of the property
     * @return A PropertyEntry.PropertyType
     */
    public static PropertyEntry.PropertyType getPropertyType(String type){
        switch (type){
            case "PRIMITIVE":
                return PropertyEntry.PropertyType.PRIMITIVE;
            case "ABSTRACT_PROPERTY":
                return PropertyEntry.PropertyType.ABSTRACT_PROPERTY;
            case "EXPRESSION_PROPERTY":
                return PropertyEntry.PropertyType.EXPRESSION_PROPERTY;
            case "GET_PROPERTY":
                return PropertyEntry.PropertyType.GET_PROPERTY;
            case "GET_SET_PROPERTY":
                return PropertyEntry.PropertyType.GET_SET_PROPERTY;
            case "FLEXO_CONCEPT_INSTANCE":
                return PropertyEntry.PropertyType.FLEXO_CONCEPT_INSTANCE;
            case "MODEL_SLOT":
                return PropertyEntry.PropertyType.MODEL_SLOT;
            case "TECHNOLOGY_ROLE":
                return PropertyEntry.PropertyType.TECHNOLOGY_ROLE;
            default:
                return null;
        }
    }

    /**
     * It takes a string and returns a PropertyCardinality enum
     *
     * @param cardinality The cardinality of the property.
     * @return The cardinality of the property.
     */
    public static PropertyCardinality getCardinality(String cardinality) {
        switch (cardinality){
            case "One":
                return PropertyCardinality.One;
            case "ZeroMany":
                return PropertyCardinality.ZeroMany;
            case "OneMany":
                return PropertyCardinality.OneMany;
            default:
                return PropertyCardinality.ZeroOne;
        }
    }
    
    /**
     * It returns the primitive type of given string
     *
     * @param type The type of the field.
     * @return The primitive type of the given string.
     */
    public static PrimitiveType getPrimitiveType(String  type){
        switch (type){
            case "String":
                return PrimitiveType.String;
            case "Date":
                return PrimitiveType.Date;
            case "Boolean":
                return PrimitiveType.Boolean;
            case "Integer":
            case "Byte":
            case "Long":
            case "Short":
                return PrimitiveType.Integer;
            case "Float":
                return PrimitiveType.Float;
            case "Double":
                return PrimitiveType.Double;
            default:
                return null;
        }
    }

    /**
     * It returns the class of the type passed as a parameter
     *
     * @param type the type of the property
     * @return The type of the object.
     */
    public static Type getAbstractType(String type) {
        switch (type){
            case "FlexoConcept":
                return FlexoConcept.class;
            case "VirtualModel":
                return VirtualModel.class;
            default:
                return (Type) getPrimitiveType(type);
        }
    }

    /**
     * It takes a string and returns the corresponding behaviour class
     *
     * @param type the type of the behaviour
     * @return The class of the behaviour type.
     */
    public static Class<? extends FlexoBehaviour> getBehaviourType(String type){
        switch (type){
            case "action":
                return ActionScheme.class;
            case "cloning":
                return CloningScheme.class;
            case "creation":
                return CreationScheme.class;
            case "deletion":
                return DeletionScheme.class;
            case "event":
                return EventListener.class;
            case "synchronization":
                return SynchronizationScheme.class;
            case "navigation":
                return NavigationScheme.class;
            default:
                return null;
        }
    }

    /**
     * It takes a String type and a String value and returns the value casted to the type
     *
     * @param type The type of the value to be casted.
     * @param value The value to be casted
     * @return The value of the primitive type.
     */
    public static Object castPrimitiveValue(String type, String value){
        switch (type){
            case "String":
                return value;
            case "Date":
                try {
                    return new SimpleDateFormat("dd-MM-yyyy").parse(value);
                } catch (ParseException e) {
                    return null;
                }
            case "Boolean":
                return Boolean.valueOf(value);
            case "Integer":
                return Integer.valueOf(value);
            case "Byte":
                return Byte.valueOf(value);
            case "Long":
                return Long.valueOf(value);
            case "Short":
                return Short.valueOf(value);
            case "Float":
                return Float.valueOf(value);
            case "Double":
                return Double.valueOf(value);
            default:
                return null;
        }
    }

    public static FMLConsole.LogLevel getLogLevel(String level){
        switch (level){
            case "info":
                return FMLConsole.LogLevel.INFO;
            case "severe":
                return FMLConsole.LogLevel.SEVERE;
            case "warning":
                return FMLConsole.LogLevel.WARNING;
            case "fine":
                return FMLConsole.LogLevel.FINE;
            case "finer":
                return FMLConsole.LogLevel.FINER;
            case "finest":
                return FMLConsole.LogLevel.FINEST;
            case "debug":
                return FMLConsole.LogLevel.DEBUG;
            default:
                return null;
        }
    }

    public static TechnologyAdapter<?> getTechnologyAdapterClass(String ta, FlexoServiceManager serviceManager) {
        switch (ta){
            case "fml":
                return serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
            case "fmlrt":
                return serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
            // TODO: add other technology adapters
            default:
                return null;
        }
    }

    /**
     * It takes a path and a project, and returns the folder that corresponds to the path
     *
     * @param path the path to the folder you want to get
     * @param project the project to which the folder belongs
     * @return The folder that matches the path.
     */
    public static RepositoryFolder getFolderFromPath(String path, FlexoProject<?> project){
        RepositoryFolder folder     = project.getRootFolder();
        RepositoryFolder subFolder;

        if (path != null && !path.isEmpty()){
            List<String> folders = new LinkedList(Arrays.asList(path.split("/")));

            while (folders.size() > 0){
                if(!folders.get(0).isEmpty()){
                    subFolder = folder.getFolderNamed(folders.get(0));
                    if (subFolder == null){
                        break;
                    }
                    folder = subFolder;
                }
                folders.remove(0);
            }
        }
        return folder;
    }

    /**
     * It takes a zip file, unzips it, and returns the files inside
     *
     * @param targetDir The directory where the zip file will be unzipped.
     * @param uploadedFile The file that was uploaded by the user
     * @return A list of files in the target directory.
     */
    public static File[] unzipFile(String targetDir, Buffer uploadedFile) throws IOException {
        byte[] buffredBytes = uploadedFile.getBytes();
        File uploadedRc     = new File(WORKSPACE + "uploaded_rc.zip");

        FileUtils.writeByteArrayToFile(uploadedRc, buffredBytes);
        ZipUtils.unzipFile("src/main/resources/uploaded_rc.zip", targetDir);

        //Delete unnecessary files
        uploadedRc.delete();

        try{
            Files.walk(Paths.get(WORKSPACE + "uploaded_rc/__MACOSX/")).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch(NoSuchFileException e){
            Log.warn("No __MACOSX folder to delete");
        }

        try{
            Files.walk(Paths.get(WORKSPACE + "uploaded_rc/.DS_Store")).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch(NoSuchFileException e){
            Log.warn("No DS_Store file to delete");
        }

        return new File(targetDir).listFiles();
    }
}
