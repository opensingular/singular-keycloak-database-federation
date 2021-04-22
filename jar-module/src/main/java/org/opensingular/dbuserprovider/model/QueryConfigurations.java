package org.opensingular.dbuserprovider.model;

public class QueryConfigurations {

    private String count;
    private String listAll;
    private String findById;
    private String findByUsername;
    private String findBySearchTerm;
    private String findPasswordHash;
    private String hashFunction;

    public QueryConfigurations(String count, String listAll, String findById, String findByUsername, String findBySearchTerm, String findPasswordHash, String hashFunction) {
        this.count = count;
        this.listAll = listAll;
        this.findById = findById;
        this.findByUsername = findByUsername;
        this.findBySearchTerm = findBySearchTerm;
        this.findPasswordHash = findPasswordHash;
        this.hashFunction = hashFunction;
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

    public String getFindBySearchTerm() {
        return findBySearchTerm;
    }

    public String getFindPasswordHash() {
        return findPasswordHash;
    }

    public String getHashFunction() {
        return hashFunction;
    }

    public boolean isBlowfish() {
        return hashFunction.toLowerCase().contains("blowfish");
    }
}
