package org.opensingular.dbuserprovider.persistence;

import org.opensingular.dbuserprovider.DBUserStorageException;
import org.opensingular.dbuserprovider.model.QueryConfigurations;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;


@JBossLog
public class UserRepository {


    private DataSourceProvider  dataSourceProvider;
    private QueryConfigurations queryConfigurations;

    public UserRepository(DataSourceProvider dataSourceProvider, QueryConfigurations queryConfigurations) {
        this.dataSourceProvider = dataSourceProvider;
        this.queryConfigurations = queryConfigurations;
    }

    private <T> T doQuery(String query, Function<ResultSet, T> resultTransformer, Object... params) {
        Optional<DataSource> dataSourceOpt = dataSourceProvider.getDataSource();
        if (dataSourceOpt.isPresent()) {
            DataSource dataSource = dataSourceOpt.get();
            try (Connection c = dataSource.getConnection()) {
                PreparedStatement statement = c.prepareStatement(query);
                if (params != null) {
                    for (int i = 1; i <= params.length; i++) {
                        statement.setObject(i, params[i - 1]);
                    }
                }
                try (ResultSet rs = statement.executeQuery()) {
                    return resultTransformer.apply(rs);
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
        return null;
    }

    private List<Map<String, String>> readMap(ResultSet rs) {
        try {
            List<Map<String, String>> data        = new ArrayList<>();
            Set<String>               columnsFound = new HashSet<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnLabel = rs.getMetaData().getColumnLabel(i);
                columnsFound.add(columnLabel);
            }
            while (rs.next()) {
                Map<String, String> result = new HashMap<>();
                for (String col : columnsFound) {
                    result.put(col, rs.getString(col));
                }
                data.add(result);
            }
            return data;
        } catch (Exception e) {
            throw new DBUserStorageException(e.getMessage(), e);
        }
    }


    private Integer readInt(ResultSet rs) {
        try {
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            throw new DBUserStorageException(e.getMessage(), e);
        }
    }

    private Boolean readBoolean(ResultSet rs) {
        try {
            rs.next();
            return rs.getBoolean(1);
        } catch (Exception e) {
            throw new DBUserStorageException(e.getMessage(), e);
        }
    }

    private String readString(ResultSet rs) {
        try {
            rs.next();
            return rs.getString(1);
        } catch (Exception e) {
            throw new DBUserStorageException(e.getMessage(), e);
        }
    }

    public List<Map<String,String>> getAllUsers() {
        return doQuery(queryConfigurations.getListAll(), this::readMap);
    }

    public int getUsersCount() {
        return Optional.ofNullable(doQuery(queryConfigurations.getCount(), this::readInt)).orElse(0);
    }


    public Map<String,String> findUserById(String id) {
        return Optional.ofNullable(doQuery(queryConfigurations.getFindById(), this::readMap, id))
                .orElse(Collections.emptyList())
                .stream().findFirst().orElse(null);
    }

    public Map<String,String> findUserByUsername(String username) {
        return Optional.ofNullable(doQuery(queryConfigurations.getFindByUsername(), this::readMap, username))
                .orElse(Collections.emptyList())
                .stream().findFirst().orElse(null);
    }

    public List<Map<String,String>> findUsers(String query) {
        return doQuery(queryConfigurations.getFindBySearchTerm(), this::readMap, query);
    }

    public boolean validateCredentials(String username, String password) {
        String hash = Optional.ofNullable(doQuery(queryConfigurations.getFindPasswordHash(), this::readString, username)).orElse("");
        if (queryConfigurations.isBlowfish()) {
            return BCrypt.checkpw(password, hash);
        } else {
            MessageDigest digest   = DigestUtils.getDigest(queryConfigurations.getHashFunction());
            byte[]        pwdBytes = StringUtils.getBytesUtf8(password);
            return Objects.equals(Hex.encodeHexString(digest.digest(pwdBytes)), password);
        }
    }

    public boolean updateCredentials(String username, String password) {
        throw new NotImplementedException("Password update not supported");
    }

}
