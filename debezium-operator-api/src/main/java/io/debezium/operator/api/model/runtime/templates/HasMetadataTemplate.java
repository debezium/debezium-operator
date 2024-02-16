/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.templates;

/**
 * Interface for kubernetes objects witch metadata
 */
public interface HasMetadataTemplate {

    /**
     * Gets template metadata
     *
     * @return Metadata template
     */
    MetadataTemplate getMetadata();

    /**
     * Sets template metadata
     *
     * @param metadata Metadata template
     */
    void setMetadata(MetadataTemplate metadata);
}
