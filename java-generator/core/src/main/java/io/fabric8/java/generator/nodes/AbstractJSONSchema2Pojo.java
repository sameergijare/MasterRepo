/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.java.generator.nodes;

import static io.fabric8.java.generator.nodes.Keywords.JAVA_KEYWORDS;

import com.github.javaparser.ast.CompilationUnit;
import io.fabric8.java.generator.exceptions.JavaGeneratorException;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaProps;
import java.util.function.Function;

public abstract class AbstractJSONSchema2Pojo {

    static final String BOOLEAN_CRD_TYPE = "boolean";
    static final String INTEGER_CRD_TYPE = "integer";
    static final String INT32_CRD_TYPE = "int32";
    static final String INT64_CRD_TYPE = "int64";
    static final String NUMBER_CRD_TYPE = "number";
    static final String FLOAT_CRD_TYPE = "float";
    static final String DOUBLE_CRD_TYPE = "double";
    static final String STRING_CRD_TYPE = "string";
    static final String OBJECT_CRD_TYPE = "object";
    static final String ARRAY_CRD_TYPE = "array";

    public abstract String getType();

    public abstract GeneratorResult generateJava(CompilationUnit cu);

    /** Takes a random string and manipulate it to be a valid Java identifier */
    public static String sanitizeString(String str) {
        str = str.trim();
        String sanitized = "";
        if (JAVA_KEYWORDS.contains(str)) {
            sanitized = "_" + str;
        } else {
            sanitized = str;
        }

        if (sanitized.startsWith("-")) {
            sanitized = sanitized.replaceFirst("-", "minus");
        }

        int index = sanitized.indexOf('-');
        while (index != -1) {
            int next = Math.min(index + 2, sanitized.length());
            sanitized =
                    sanitized.substring(0, index)
                            + sanitized.substring(index + 1, next).toUpperCase()
                            + sanitized.substring(next);
            index = sanitized.indexOf('-');
        }

        return sanitized;
    }

    public static AbstractJSONSchema2Pojo fromJsonSchema(
            String key, JSONSchemaProps prop, String prefix, String suffix) {
        Function<JavaNameAndType, AbstractJSONSchema2Pojo> fromJsonSchema =
                javaNameAndType -> fromJsonSchema(key, javaNameAndType, prop, prefix, suffix);
        String type = prop.getType();
        if (Boolean.TRUE.equals(prop.getXKubernetesIntOrString())) {
            return fromJsonSchema.apply(JPrimitiveNameAndType.INT_OR_STRING);
        } else if (type == null
                && Boolean.TRUE.equals(prop.getXKubernetesPreserveUnknownFields())) {
            return fromJsonSchema.apply(new JObjectNameAndType(key));
        } else if (prop.getEnum() != null && prop.getEnum().size() > 0) {
            return fromJsonSchema.apply(new JEnumNameAndType(key));
        } else {
            if (type == null) {
                throw new IllegalArgumentException("Type for key:" + key + " is null");
            }

            switch (type) {
                case BOOLEAN_CRD_TYPE:
                    return fromJsonSchema.apply(JPrimitiveNameAndType.BOOL);
                case INTEGER_CRD_TYPE:
                    String intFormat = prop.getFormat();
                    if (intFormat == null) intFormat = INT64_CRD_TYPE;

                    switch (intFormat) {
                        case INT32_CRD_TYPE:
                            return fromJsonSchema.apply(JPrimitiveNameAndType.INTEGER);
                        case INT64_CRD_TYPE:
                        default:
                            return fromJsonSchema.apply(JPrimitiveNameAndType.LONG);
                    }
                case NUMBER_CRD_TYPE:
                    String numberFormat = prop.getFormat();
                    if (numberFormat == null) numberFormat = DOUBLE_CRD_TYPE;

                    switch (numberFormat) {
                        case FLOAT_CRD_TYPE:
                            return fromJsonSchema.apply(JPrimitiveNameAndType.FLOAT);
                        case DOUBLE_CRD_TYPE:
                        default:
                            return fromJsonSchema.apply(JPrimitiveNameAndType.DOUBLE);
                    }
                case STRING_CRD_TYPE:
                    return fromJsonSchema.apply(JPrimitiveNameAndType.STRING);
                case OBJECT_CRD_TYPE:
                    if (prop.getAdditionalProperties() != null
                            && prop.getAdditionalProperties().getSchema() != null) {
                        return fromJsonSchema.apply(new JMapNameAndType(key));
                    } else {
                        return fromJsonSchema.apply(new JObjectNameAndType(key));
                    }
                case ARRAY_CRD_TYPE:
                    return fromJsonSchema.apply(new JArrayNameAndType(key));
                default:
                    throw new JavaGeneratorException("Unmanaged type " + prop.getType());
            }
        }
    }

    private static AbstractJSONSchema2Pojo fromJsonSchema(
            String key, JavaNameAndType nt, JSONSchemaProps prop, String prefix, String suffix) {
        switch (nt.getType()) {
            case PRIMITIVE:
                return new JPrimitive(nt.getName());
            case ARRAY:
                return new JArray(fromJsonSchema(key, prop.getItems().getSchema(), prefix, suffix));
            case MAP:
                return new JMap(
                        fromJsonSchema(
                                key, prop.getAdditionalProperties().getSchema(), prefix, suffix));
            case OBJECT:
                boolean preserveUnknownFields =
                        Boolean.TRUE.equals(prop.getXKubernetesPreserveUnknownFields());
                return new JObject(
                        key,
                        prop.getProperties(),
                        prop.getRequired(),
                        new JObjectOptions(preserveUnknownFields, prefix, suffix));
            case ENUM:
                return new JEnum(key, prop.getEnum());
            default:
                throw new JavaGeneratorException("Unreachable " + nt.getType());
        }
    }
}
