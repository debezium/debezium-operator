/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

public class InMemoryStore extends AbstractStore {

    public InMemoryStore(String type) {
        super(type);
    }
}
