package org.opensingular.dbuserprovider;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.opensingular.dbuserprovider.model.QueryConfigurations;
import org.opensingular.dbuserprovider.model.UserAdapter;
import org.opensingular.dbuserprovider.persistence.DataSourceProvider;
import org.opensingular.dbuserprovider.persistence.UserRepository;
import org.opensingular.dbuserprovider.util.PagingUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@JBossLog
public class DBUserStorageProvider implements UserStorageProvider,
                                              UserLookupProvider, UserQueryProvider, CredentialInputUpdater, CredentialInputValidator, UserRegistrationProvider {
    
    private final KeycloakSession session;
    private final ComponentModel  model;
    private final UserRepository  repository;
    private final boolean allowDatabaseToOverwriteKeycloak;

    DBUserStorageProvider(KeycloakSession session, ComponentModel model, DataSourceProvider dataSourceProvider, QueryConfigurations queryConfigurations) {
        this.session    = session;
        this.model      = model;
        this.repository = new UserRepository(dataSourceProvider, queryConfigurations);
        this.allowDatabaseToOverwriteKeycloak = queryConfigurations.getAllowDatabaseToOverwriteKeycloak();
    }
    
    
    private Stream<UserModel> toUserModel(RealmModel realm, List<Map<String, String>> users) {
        return users.stream()
                    .map(m -> new UserAdapter(session, realm, model, m, allowDatabaseToOverwriteKeycloak));
    }
    
    
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }
    
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }
    
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        
        log.infov("isValid user credential: userId={0}", user.getId());
        
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        
        UserCredentialModel cred = (UserCredentialModel) input;

        UserModel dbUser = user;
        // If the cache just got loaded in the last 500 millisec (i.e. probably part of the actual flow), there is no point in reloading the user.)
        if (allowDatabaseToOverwriteKeycloak && user instanceof CachedUserModel && (System.currentTimeMillis() - ((CachedUserModel) user).getCacheTimestamp()) > 500) {
          dbUser = this.getUserById(realm, user.getId());

          if (dbUser == null) {
            ((CachedUserModel) user).invalidate();
            return false;
          }

          // For now, we'll just invalidate the cache if username or email has changed. Eventually we could check all (or a parametered list of) attributes fetched from the DB.
          if (!java.util.Objects.equals(user.getUsername(), dbUser.getUsername()) || !java.util.Objects.equals(user.getEmail(), dbUser.getEmail())) {
            ((CachedUserModel) user).invalidate();
          }
        }
        return repository.validateCredentials(dbUser.getUsername(), cred.getChallengeResponse());
    }
    
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        
        log.infov("updating credential: realm={0} user={1}", realm.getId(), user.getUsername());
        
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        
        UserCredentialModel cred = (UserCredentialModel) input;
        return repository.updateCredentials(user.getUsername(), cred.getChallengeResponse());
    }
    
    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
    }
    
    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user)
    {
        return Stream.empty();
    }
    
    @Override
    public void preRemove(RealmModel realm) {
        
        log.infov("pre-remove realm");
    }
    
    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        
        log.infov("pre-remove group");
    }
    
    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        
        log.infov("pre-remove role");
    }
    
    @Override
    public void close() {
        log.debugv("closing");
    }
    
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        
        log.infov("lookup user by id: realm={0} userId={1}", realm.getId(), id);
        
        String externalId = StorageId.externalId(id);
        Map<String, String> user = repository.findUserById(externalId);

        if (user == null) {
            log.debugv("findUserById returned null, skipping creation of UserAdapter, expect login error");
            return null;
        } else {
            return new UserAdapter(session, realm, model, user, allowDatabaseToOverwriteKeycloak);
        }
    }
    
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        
        log.infov("lookup user by username: realm={0} username={1}", realm.getId(), username);
        
        return repository.findUserByUsername(username).map(u -> new UserAdapter(session, realm, model, u, allowDatabaseToOverwriteKeycloak)).orElse(null);
    }
    
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        
        log.infov("lookup user by username: realm={0} email={1}", realm.getId(), email);
        
        return getUserByUsername(realm, email);
    }
    
    @Override
    public int getUsersCount(RealmModel realm) {
        return repository.getUsersCount(null);
    }
    
    @Override
    public int getUsersCount(RealmModel realm, Set<String> groupIds) {
        return repository.getUsersCount(null);
    }
    
    @Override
    public int getUsersCount(RealmModel realm, String search) {
        return repository.getUsersCount(search);
    }
    
    @Override
    public int getUsersCount(RealmModel realm, String search, Set<String> groupIds) {
        return repository.getUsersCount(search);
    }
    
    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params) {
        return repository.getUsersCount(null);
    }
    
    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params, Set<String> groupIds) {
        return repository.getUsersCount(null);
    }
    
    @Override
    public int getUsersCount(RealmModel realm, boolean includeServiceAccount) {
        return repository.getUsersCount(null);
    }
    
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
        Integer maxResults)
    {
        log.infov("list users: realm={0} firstResult={1} maxResults={2}", realm.getId(), firstResult, maxResults);
        return internalSearchForUser(search, realm, new PagingUtil.Pageable(firstResult, maxResults));
    }
    
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult,
        Integer maxResults)
    {
        log.infov("search for users with params: realm={0} params={1}", realm.getId(), params);
        return internalSearchForUser(params.values().stream().findFirst().orElse(null), realm, null);
    }
    
    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue)
    {
        log.infov("search for group members: realm={0} attrName={1} attrValue={2}", realm.getId(), attrName, attrValue);
        return Stream.empty();
    }
    
    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult,
        Integer maxResults)
    {
        log.infov("search for group members with params: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId(), firstResult, maxResults);
        return Stream.empty();
    }
    
    private Stream<UserModel> internalSearchForUser(String search, RealmModel realm, PagingUtil.Pageable pageable) {
        return toUserModel(realm, repository.findUsers(search, pageable));
    }
    
    @Override
    public UserModel addUser(RealmModel realm, String username) {
        // from documentation: "If your provider has a configuration switch to turn off adding a user, returning null from this method will skip the provider and call the next one."
        return null;
    }
    
    
    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        boolean userRemoved = repository.removeUser();
        
        if (userRemoved) {
            log.infov("deleted keycloak user: realm={0} userId={1} username={2}", realm.getId(), user.getId(), user.getUsername());
        }
        
        return userRemoved;
    }
}
