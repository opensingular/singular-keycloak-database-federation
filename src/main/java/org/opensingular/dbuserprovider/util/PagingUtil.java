package org.opensingular.dbuserprovider.util;

import org.opensingular.dbuserprovider.persistence.RDBMS;

import java.util.Locale;

public class PagingUtil {

    public static class Pageable {
        private final int firstResult;
        private final int maxResults;

        public Pageable(int firstResult, int maxResults) {
            this.firstResult = firstResult;
            this.maxResults = maxResults;
        }
    }

    public static String formatScriptWithPageable(String query, Pageable pageable, RDBMS rdbms) {
        int firstResult = Math.max(pageable.firstResult, 0);
        int maxResults  = Math.max(pageable.maxResults, 0);
        switch (rdbms) {
            case POSTGRESQL:
            case MYSQL:
                return String.format("%s LIMIT %d OFFSET %d", query, maxResults, firstResult);
            case ORACLE:
                return String.format("%s OFFSET %d ROWS FETCH NEXT %d ROWS ONLY", query, firstResult, maxResults);
            case SQL_SERVER:
                // OFFSET/FETCH requires an ORDER BY clause on SQL Server
                String orderBy = query.toLowerCase(Locale.ROOT).contains("order by") ? "" : " ORDER BY (SELECT NULL)";
                return String.format("%s%s OFFSET %d ROWS FETCH NEXT %d ROWS ONLY", query, orderBy, firstResult, maxResults);
            default:
                return query;
        }
    }
}
