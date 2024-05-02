/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.webapp;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

final class StatusCache implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String,Status> entries = new LinkedHashMap<String,Status>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Entry<String,Status> eldest) {
            return size() > 10;
        }
    };

    synchronized String add(Status status) {
        String key = UUID.randomUUID().toString();
        entries.put(key, status);
        return key;
    }

    synchronized Status get(String key) {
        return entries.get(key);
    }
}
