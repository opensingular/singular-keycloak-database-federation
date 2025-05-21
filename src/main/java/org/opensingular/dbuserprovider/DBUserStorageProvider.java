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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@JBossLog
public class DBUserStorageProvider implements UserStorageProvider,
                                              UserLookupProvider, UserQueryProvider, CredentialInputUpdater, CredentialInputValidator, UserRegistrationProvider {
    
    private final KeycloakSession session;
    private final ComponentModel  model;
    private final UserRepository  repository;
    private final boolean allowDatabaseToOverwriteKeycloak;
    private final boolean syncEnabled;
    private final boolean unlinkEnabled;

    DBUserStorageProvider(KeycloakSession session, ComponentModel model, DataSourceProvider dataSourceProvider, QueryConfigurations queryConfigurations) {
        this.session    = session;
        this.model      = model;
        this.repository = new UserRepository(dataSourceProvider, queryConfigurations);
        this.allowDatabaseToOverwriteKeycloak = queryConfigurations.getAllowDatabaseToOverwriteKeycloak();
        this.syncEnabled = queryConfigurations.isSyncEnabled();
        this.unlinkEnabled = queryConfigurations.isUnlinkEnabled();
    }

    private UserModel syncUser(RealmModel realm, UserModel userAdapter) {
        String username = userAdapter.getUsername();
        log.infov("Attempting to sync user: {0}", username);

        if (!this.syncEnabled) {
            log.debugv("User synchronization is disabled. Skipping sync for user: {0}", username);
            return userAdapter;
        }

        UserModel localUser = session.userLocalStorage().getUserByUsername(realm, username);

        if (localUser == null) {
            log.infov("User not found locally, creating new local user: {0}", username);
            try {
                localUser = session.userLocalStorage().addUser(realm, username);
                localUser.setFederationLink(userAdapter.getId()); // Link to the federated user
                localUser.setFirstName(userAdapter.getFirstName());
                localUser.setLastName(userAdapter.getLastName());
                localUser.setEmail(userAdapter.getEmail());
                localUser.setEmailVerified(userAdapter.isEmailVerified());
                // Copy attributes
                for (Map.Entry<String, List<String>> attribute : userAdapter.getAttributes().entrySet()) {
                    localUser.setAttribute(attribute.getKey(), attribute.getValue());
                }
                // Add any required actions if necessary, e.g.,
                // localUser.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
                log.infov("Successfully created local user: {0}", username);
            } catch (Exception e) {
                log.errorv(e, "Error creating local user: {0}", username);
                return userAdapter; // return original adapter if sync fails
            }
        } else {
            log.infov("Found existing local user: {0}. Updating attributes.", username);
            if (!localUser.isEmailVerified() && userAdapter.isEmailVerified()){
                 localUser.setEmailVerified(userAdapter.isEmailVerified());
            }
            if (userAdapter.getEmail() != null && !userAdapter.getEmail().equals(localUser.getEmail())) {
                localUser.setEmail(userAdapter.getEmail());
            }
            if (userAdapter.getFirstName() != null && !userAdapter.getFirstName().equals(localUser.getFirstName())) {
                localUser.setFirstName(userAdapter.getFirstName());
            }
            if (userAdapter.getLastName() != null && !userAdapter.getLastName().equals(localUser.getLastName())) {
                localUser.setLastName(userAdapter.getLastName());
            }
            // Update attributes - be careful with read-only attributes or conflicts
            try {
                for (Map.Entry<String, List<String>> attribute : userAdapter.getAttributes().entrySet()) {
                    // Potentially add checks here to avoid overwriting protected attributes
                    localUser.setAttribute(attribute.getKey(), attribute.getValue());
                }
                log.infov("Successfully synced attributes for user: {0}", username);
            } catch (ModelException e) { // More specific exception for read-only issues if available
                log.warnv(e, "Could not update some attributes for user {0} (possibly read-only or conflict).", username);
            }
        }
        return localUser;
    }
    
    private List<UserModel> toUserModel(RealmModel realm, List<Map<String, String>> users) {
        return users.stream()
                    .map(m -> new UserAdapter(session, realm, model, m, allowDatabaseToOverwriteKeycloak)).collect(Collectors.toList());
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
          dbUser = this.getUserById(user.getId(), realm);

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
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        return Collections.emptySet();
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
    public UserModel getUserById(String id, RealmModel realm) {
        
        log.infov("lookup user by id: realm={0} userId={1}", realm.getId(), id);
        
        String externalId = StorageId.externalId(id);
        Map<String, String> user = repository.findUserById(externalId);

        if (user == null) {
            log.debugv("findUserById returned null, skipping creation of UserAdapter, expect login error");
            return null;
        } else {
            UserModel userAdapter = new UserAdapter(session, realm, model, user, allowDatabaseToOverwriteKeycloak);
            if (this.syncEnabled) {
                return syncUser(realm, userAdapter);
            }
            return userAdapter;
        }
    }
    
    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        
        log.infov("lookup user by username: realm={0} username={1}", realm.getId(), username);
        
        UserModel userAdapter = repository.findUserByUsername(username).map(u -> new UserAdapter(session, realm, model, u, allowDatabaseToOverwriteKeycloak)).orElse(null);
        if (userAdapter != null) {
            if (this.syncEnabled) {
                return syncUser(realm, userAdapter);
            }
            return userAdapter;
        }
        return null;
    }
    
    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        
        log.infov("lookup user by email: realm={0} email={1}", realm.getId(), email); // Log corrected to email
        
        // Assuming getUserByUsername can also find by email if the DB query is configured that way,
        // or if username and email are the same. If not, this might need adjustment
        // to call a specific repository.findUserByEmail if available.
        UserModel userAdapter = repository.findUserByUsername(email) // This might need to be repository.findUserByEmail(email)
                                .map(u -> new UserAdapter(session, realm, model, u, allowDatabaseToOverwriteKeycloak))
                                .orElse(null);
        if (userAdapter != null) {
            if (this.syncEnabled) {
                return syncUser(realm, userAdapter);
            }
            return userAdapter;
        }
        return null;
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
    public List<UserModel> getUsers(RealmModel realm) {
        log.infov("list users: realm={0}", realm.getId());
        return internalSearchForUser(null, realm, null);
    }
    
    @Override
    public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        
        log.infov("list users: realm={0} firstResult={1} maxResults={2}", realm.getId(), firstResult, maxResults);
        return internalSearchForUser(null, realm, new PagingUtil.Pageable(firstResult, maxResults));
    }
    
    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm) {
        log.infov("search for users: realm={0} search={1}", realm.getId(), search);
        return internalSearchForUser(search, realm, null);
    }
    
    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        log.infov("search for users: realm={0} search={1} firstResult={2} maxResults={3}", realm.getId(), search, firstResult, maxResults);
        return internalSearchForUser(search, realm, new PagingUtil.Pageable(firstResult, maxResults));
    }
    
    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        log.infov("search for users with params: realm={0} params={1}", realm.getId(), params);
        return internalSearchForUser(params.values().stream().findFirst().orElse(null), realm, null);
    }
    
    private List<UserModel> internalSearchForUser(String search, RealmModel realm, PagingUtil.Pageable pageable) {
        return toUserModel(realm, repository.findUsers(search, pageable));
    }
    
    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
        log.infov("search for users with params: realm={0} params={1} firstResult={2} maxResults={3}", realm.getId(), params, firstResult, maxResults);
        return internalSearchForUser(params.values().stream().findFirst().orElse(null), realm, new PagingUtil.Pageable(firstResult, maxResults));
    }
    
    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        log.infov("search for group members with params: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId(), firstResult, maxResults);
        return Collections.emptyList();
    }
    
    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        log.infov("search for group members: realm={0} groupId={1} firstResult={2} maxResults={3}", realm.getId(), group.getId());
        return Collections.emptyList();
    }
    
    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        log.infov("search for group members: realm={0} attrName={1} attrValue={2}", realm.getId(), attrName, attrValue);
        return Collections.emptyList();
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

    /**
     * Removes the federation link from the user.
     *
     * @param realm The realm.
     * @param user  The user to unlink.
     * @return True if unlinking was successful, false otherwise.
     */
    public boolean unlinkUser(RealmModel realm, UserModel user) {
        log.infov("Attempting to unlink user: realm={0} username={1}", realm.getId(), user.getUsername());

        if (!this.unlinkEnabled) {
            log.warnv("User unlinking is disabled by configuration. Skipping for user: {0}", user.getUsername());
            return false;
        }

        if (user.getFederationLink() == null) {
            log.infov("User {0} is not federated or already unlinked.", user.getUsername());
            return true; // Considered success as the link is not present
        }

        try {
            user.setFederationLink(null);
            log.infov("Successfully unlinked user: {0}", user.getUsername());
            return true;
        } catch (Exception e) {
            log.errorv(e, "Error unlinking user: {0}", user.getUsername());
            return false;
        }
    }
}
