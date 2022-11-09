package org.opensingular.dbuserprovider.model;

import org.opensingular.dbuserprovider.persistence.RDBMS;

public class QueryConfigurations {

    private final String count;
    private final String listAll;
    private final String findById;
    private final String findByUsername;
    private final String findByEmail;
    private final String findBySearchTerm;
    private final int findBySearchTermParamsCount;
    private final String findPasswordHash;
    private final String hashFunction;
    private final RDBMS  RDBMS;
    private final boolean allowKeycloakDelete;
    private final boolean allowDatabaseToOverwriteKeycloak;

    public QueryConfigurations(String count, String listAll, String findById, String findByUsername, String findByEmail, String findBySearchTerm, String findPasswordHash, String hashFunction, RDBMS RDBMS, boolean allowKeycloakDelete, boolean allowDatabaseToOverwriteKeycloak) {
        this.count = count;
        this.listAll = listAll;
        this.findById = findById;
        this.findByUsername = findByUsername;
        this.findByEmail = findByEmail;
        this.findBySearchTerm = findBySearchTerm;
        this.findBySearchTermParamsCount = (int)findBySearchTerm.chars().filter(ch -> ch == '?').count();
        this.findPasswordHash = findPasswordHash;
        this.hashFunction = hashFunction;
        this.RDBMS = RDBMS;
        this.allowKeycloakDelete = allowKeycloakDelete;
        this.allowDatabaseToOverwriteKeycloak = allowDatabaseToOverwriteKeycloak;
    }

    public RDBMS getRDBMS() {
        return RDBMS;
    }

    public String getCount() {
        return count;
    }

    public String getListAll() {
        return listAll;
    }

    public String getFindById() {
        return findById;
    }

    public String getFindByUsername() {
        return findByUsername;
    }
    
    public String getFindByEmail() {
        return findByEmail;
    }

    public String getFindBySearchTerm() {
        return findBySearchTerm;
    }
    
    public int getFindBySearchTermParamsCount() {
        return findBySearchTermParamsCount;
    }

    public String getFindPasswordHash() {
        return findPasswordHash;
    }

    public String getHashFunction() {
        return hashFunction;
    }

    public boolean isArgon2() {
        return hashFunction.contains("Argon2");
    }

    public boolean isBlowfish() {
        return hashFunction.toLowerCase().contains("blowfish");
    }

    public boolean getAllowKeycloakDelete() {
        return allowKeycloakDelete;
    }

    public boolean getAllowDatabaseToOverwriteKeycloak() {
        return allowDatabaseToOverwriteKeycloak;
    }
}
