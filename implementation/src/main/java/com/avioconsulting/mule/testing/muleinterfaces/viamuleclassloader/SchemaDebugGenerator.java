package com.avioconsulting.mule.testing.muleinterfaces.viamuleclassloader;

import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// see GroovyMuleTestFrameworkClassLoader for how we substitute this in for Mule's DefaultExtensionSchemaGenerator implementation
public class SchemaDebugGenerator extends DefaultExtensionSchemaGenerator {
    // the only way to generate dynamic schemas from Mule extensions BEFORE the app loads (and possibly fails)
    // is to dump them to files write as they are generated
    @Override
    public String generate(ExtensionModel extensionModel,
                           DslResolvingContext dslContext) {
        String schema = super.generate(extensionModel, dslContext);
        if (!System.getProperty("avio.groovy.test.generate.xml.schemas").equals("true")) {
            return schema;
        }
        File schemaDir = new File(System.getProperty("mule.home"),
                                  "schemas_from_testing_framework");
        assert schemaDir.exists() || schemaDir.mkdir();
        File schemaFile = new File(schemaDir,
                                   extensionModel.getXmlDslModel().getXsdFileName());
        FileWriter writer = null;
        try {
            writer = new FileWriter(schemaFile,
                                    false); // don't append
            writer.write(schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return schema;
    }
}
