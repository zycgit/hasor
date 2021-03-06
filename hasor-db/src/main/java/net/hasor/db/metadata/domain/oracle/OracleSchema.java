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
package net.hasor.db.metadata.domain.oracle;
import java.util.Date;

/**
 * Oracle 的 Schema
 * @version : 2021-04-29
 * @author 赵永春 (zyc@hasor.net)
 */
public class OracleSchema {
    private String               schema;
    private OracleSchemaStatus   status;
    private Date                 lockDate;
    private Date                 expiryDate;
    private String               defaultTablespace;
    private String               temporaryTablespace;
    private Date                 created;
    private String               profile;
    private OracleSchemaAuthType authenticationType;
    private Date                 lastLogin;

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public OracleSchemaStatus getStatus() {
        return this.status;
    }

    public void setStatus(OracleSchemaStatus status) {
        this.status = status;
    }

    public Date getLockDate() {
        return this.lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDefaultTablespace() {
        return this.defaultTablespace;
    }

    public void setDefaultTablespace(String defaultTablespace) {
        this.defaultTablespace = defaultTablespace;
    }

    public String getTemporaryTablespace() {
        return this.temporaryTablespace;
    }

    public void setTemporaryTablespace(String temporaryTablespace) {
        this.temporaryTablespace = temporaryTablespace;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getProfile() {
        return this.profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public OracleSchemaAuthType getAuthenticationType() {
        return this.authenticationType;
    }

    public void setAuthenticationType(OracleSchemaAuthType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public Date getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
