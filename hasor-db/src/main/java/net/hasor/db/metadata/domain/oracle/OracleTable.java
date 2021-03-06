/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.metadata.domain.oracle;
import net.hasor.db.metadata.TableDef;

/**
 * Oracle 的 Table see: ALL_TABLES
 * @version : 2021-04-29
 * @author 赵永春 (zyc@hasor.net)
 */
public class OracleTable implements TableDef {
    private String          schema;
    private String          table;
    private String          tableSpace;
    private Boolean         readOnly;
    private OracleTableType tableType;
    private String          materializedLog;
    private String          comment;

    @Override
    public String getCatalog() {
        return null;
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getTable() {
        return this.table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTableSpace() {
        return this.tableSpace;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    public Boolean getReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public OracleTableType getTableType() {
        return this.tableType;
    }

    public void setTableType(OracleTableType tableType) {
        this.tableType = tableType;
    }

    public String getMaterializedLog() {
        return this.materializedLog;
    }

    public void setMaterializedLog(String materializedLog) {
        this.materializedLog = materializedLog;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
