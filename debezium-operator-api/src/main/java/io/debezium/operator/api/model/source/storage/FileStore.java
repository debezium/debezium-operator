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
    @JsonPropertyDescription("Name of the offset file (relative to data root)")
    @JsonProperty(required = false)
    private String fileName;

    public FileStore(String fileName, String type) {
        super(type);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public ConfigMapping asConfiguration() {
        return super.asConfiguration()
                .put("filename", fileName);
    }
}
