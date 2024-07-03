/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;

public class FileStore extends AbstractStore {
    public static final String CONFIG_PREFIX = "file";

    @JsonPropertyDescription("Name of the offset file (relative to data root)")
    @JsonProperty(required = false)
    private String fileName;

    public FileStore(String fileName, String type) {
        super(CONFIG_PREFIX, type);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected ConfigMapping typeConfiguration() {
        return ConfigMapping.empty()
                .put("filename", fileName);
    }
}
