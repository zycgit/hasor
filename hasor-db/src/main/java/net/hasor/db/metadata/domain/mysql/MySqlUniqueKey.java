/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.db.metadata.domain.mysql;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL 唯一键
 * @version : 2021-03-30
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlUniqueKey extends MySqlConstraint {
    private List<String>        columns     = new ArrayList<>();
    private Map<String, String> storageType = new HashMap<>();

    public List<String> getColumns() {
        return this.columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public Map<String, String> getStorageType() {
        return this.storageType;
    }

    public void setStorageType(Map<String, String> storageType) {
        this.storageType = storageType;
    }
}
