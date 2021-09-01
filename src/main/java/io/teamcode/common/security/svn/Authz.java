package io.teamcode.common.security.svn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 
 * 참조한 소스다. Alias는 앞으로도 계속 지원하지 않을 계획이다.
 * group은 지원할 생각은 있으나 현재는 패스.
 * 
 * @author chiang
 *
 */
public class Authz implements Serializable {

	private static final long serialVersionUID = -6391781055811301224L;

	private static final Logger logger = LoggerFactory.getLogger(Authz.class);
		
	private List<AuthzAccessRule> accessRules;

	private List<AuthzGroup> groups;

	private boolean hasUnsavedChanges;

	private List<AuthzPath> paths;

	private List<AuthzRepository> repositories;

	/**
	 * FIXME 원래는 Path 기반으로 해야 정확하나 아직 세분화한 정책을 사용하지 않으므로 repositoryName으로만 구분한다.
	 * 
	 * repositoryName을 key로 하는 Map이다. 만약 String이 <code>PARENT</code> 라면 Repository에 매핑이 되지 않은 User이다. 이는
	 * Group으로 설정되어 있고 User가 직접 매핑되지 않은 경우이다.
	 * 
	 */
	//private Map<String, List<AuthzUser>> repositoryUserMap;
	
	//원래 위의 것 전에 쓰던 것 일단 남기기
	private List<AuthzUser> users;

	public Authz() {
		super();

		initialize();
	}

	public void addGroupMember(final AuthzGroup group,
			final AuthzGroupMember member)
			throws AuthzGroupMemberAlreadyExistsException,
			AuthzAlreadyMemberOfGroupException {
		Preconditions.checkNotNull(group, "Group is null");
		Preconditions.checkNotNull(member, "Member is null");

		if (group instanceof AuthzGroup) {
			((AuthzGroup) group).addMember(member);
		}

		if (member instanceof AuthzGroupMember) {
			((AuthzGroupMember) member).addGroup(group);
		}

		setHasUnsavedChanges();
	}

	public void addGroupMember(final Collection<AuthzGroup> groups,
			final AuthzGroupMember member)
			throws AuthzGroupMemberAlreadyExistsException,
			AuthzAlreadyMemberOfGroupException {

		Preconditions.checkNotNull(groups, "Groups is null");
		Preconditions.checkNotNull(member, "Member is null");

		for (final AuthzGroup group : groups) {
			addGroupMember(group, member);
		}

		setHasUnsavedChanges();

	}

	/**
	 * 
	 *
	 * @param group
	 * @param members
	 * @throws AuthzGroupMemberAlreadyExistsException
	 * @throws AuthzAlreadyMemberOfGroupException
	 */
	public void addGroupMembers(final AuthzGroup group,
			final Collection<AuthzGroupMember> members)
			throws AuthzGroupMemberAlreadyExistsException,
			AuthzAlreadyMemberOfGroupException {
		Preconditions.checkNotNull(group, "Group is null");
		Preconditions.checkNotNull(members, "Members is null");

		for (final AuthzGroupMember member : members) {
			addGroupMember(group, member);
		}
		
		setHasUnsavedChanges();
	}

	/**
	 * Sets "has unsaved changes" flag to false.
	 */
	protected void clearHasUnsavedChanges() {
		hasUnsavedChanges = false;
	}

	public AuthzAccessRule cloneAccessRule(
			final AuthzAccessRule authzAccessRule,
			final AuthzPermissionable permissionable)
			throws AuthzAccessRuleAlreadyExistsException,
			AuthzAccessRuleAlreadyAppliedException {
		Preconditions.checkNotNull(authzAccessRule, "Access rule is null");
		Preconditions.checkNotNull(permissionable, "Permissionable is null");

		final AuthzAccessRule newAuthzAccessRule = createAccessRule(
				authzAccessRule.getPath(), permissionable,
				authzAccessRule.getAuthzPrivilege());

		setHasUnsavedChanges();

		return newAuthzAccessRule;
	}

	public Collection<AuthzAccessRule> cloneAccessRules(
			final Collection<AuthzAccessRule> authzAccessRules,
			final AuthzPermissionable permissionable)
			throws AuthzAccessRuleAlreadyExistsException,
			AuthzAccessRuleAlreadyAppliedException {
		Preconditions.checkNotNull(authzAccessRules, "Access rules is null");
		Preconditions.checkNotNull(permissionable, "Permissionable is null");

		final Collection<AuthzAccessRule> newAuthzAccessRules = new ArrayList<AuthzAccessRule>(
				authzAccessRules.size());

		for (final AuthzAccessRule authzAccessRule : permissionable
				.getAccessRules()) {
			newAuthzAccessRules.add(cloneAccessRule(authzAccessRule,
					permissionable));
		}

		setHasUnsavedChanges();

		return newAuthzAccessRules;
	}

	public AuthzGroup cloneGroup(final AuthzGroup groupToClone,
			final String cloneGroupName)
			throws AuthzGroupAlreadyExistsException,
			AuthzInvalidGroupNameException,
			AuthzGroupMemberAlreadyExistsException,
			AuthzAlreadyMemberOfGroupException,
			AuthzAccessRuleAlreadyExistsException,
			AuthzAccessRuleAlreadyAppliedException {
		Preconditions.checkNotNull(groupToClone, "Group to clone is null");
		// Validation of cloneGroupName is done within createGroup

		final AuthzGroup cloneGroup = createGroup(cloneGroupName);

		addGroupMember(groupToClone.getGroups(), cloneGroup);
		addGroupMembers(cloneGroup, groupToClone.getMembers());
		cloneAccessRules(groupToClone.getAccessRules(), cloneGroup);

		setHasUnsavedChanges();

		return cloneGroup;
	}

	public AuthzUser cloneUser(final AuthzUser userToClone,
			final String cloneUserName, final String cloneAlias)
			throws AuthzInvalidUserNameException,
			AuthzUserAlreadyExistsException,
			AuthzUserAliasAlreadyExistsException,
			AuthzInvalidUserAliasException,
			AuthzGroupMemberAlreadyExistsException,
			AuthzAlreadyMemberOfGroupException,
			AuthzAccessRuleAlreadyExistsException,
			AuthzAccessRuleAlreadyAppliedException {
		Preconditions.checkNotNull(userToClone, "User to clone is null");
		// Validation of cloneUserName and cloneAlias is done within createGroup

		final AuthzUser cloneUser = createUser(cloneUserName, cloneAlias);

		addGroupMember(userToClone.getGroups(), cloneUser);
		cloneAccessRules(userToClone.getAccessRules(), cloneUser);

		setHasUnsavedChanges();
		return cloneUser;
	}

	public AuthzAccessRule createAccessRule(final AuthzPath path,
			final AuthzPermissionable permissionable,
			final AuthzPrivilege authzPrivilige)
			throws AuthzAccessRuleAlreadyExistsException,
			AuthzAccessRuleAlreadyAppliedException {
		assert accessRules != null;

		logger.debug(
				"createAccessRule() entered. path=\"{}\", permissionable=\"{}\"",
				path, permissionable);

		Preconditions.checkNotNull(path, "Path is null");
		Preconditions.checkNotNull(permissionable, "Permissionable is null");
		Preconditions.checkNotNull(authzPrivilige, "Access level is null");

		if (doesAccessRuleExist(path, permissionable)) {
			logger.info("createAccessRule() access rule already exists");

			throw new AuthzAccessRuleAlreadyExistsException("access rule already exists");
		}

		final AuthzAccessRule accessRule = new AuthzAccessRule(path,
				permissionable, authzPrivilige);

		if (permissionable instanceof AuthzGroupMember) {
			((AuthzGroupMember) permissionable).addAccessRule(accessRule);
		}

		if (path instanceof AuthzPath) {
			((AuthzPath) path).addAccessRule(accessRule);
		}

		accessRules.add(accessRule);
		Collections.sort(accessRules);
		setHasUnsavedChanges();

		logger.debug(
				"createAccessRule() access rule created successfully, returning {}",
				accessRule);

		return accessRule;
	}

	public AuthzGroup createGroup(final String name)
			throws AuthzGroupAlreadyExistsException,
			AuthzInvalidGroupNameException {
		assert groups != null;

		// Validate group name
		validateGroupName(name);

		final AuthzGroup group = new AuthzGroup(StringUtils.trim(name));

		groups.add(group);

		Collections.sort(groups);

		setHasUnsavedChanges();
		return group;
	}

	public AuthzPath createPath(final AuthzRepository repository,
			final String pathString) throws AuthzInvalidPathException,
			AuthzPathAlreadyExistsException {
		assert paths != null;

		// Repository is not validated as it may be null
		final String pathStringTrimmed = StringUtils.trimToNull(pathString);

		// Validate path
		if (!AuthzValidatorUtils.isValidPath(pathStringTrimmed)) {
			logger.error("createPath() invalid path");

			throw new AuthzInvalidPathException();
		}

		if (doesPathExist(repository, pathStringTrimmed)) {
			logger.info("createPath() path already exists");

			throw new AuthzPathAlreadyExistsException();
		}

		final AuthzPath path = new AuthzPath(repository, pathStringTrimmed);
		paths.add(path);
		Collections.sort(paths);
		setHasUnsavedChanges();

		return path;
	}

	public AuthzRepository createRepository(final String name)
			throws AuthzInvalidRepositoryNameException,
			AuthzRepositoryAlreadyExistsException {
		assert repositories != null;

		// Validate repository name
		validateRepositoryName(name);

		final AuthzRepository repository = new AuthzRepository(name.trim());

		repositories.add(repository);

		Collections.sort(repositories);

		setHasUnsavedChanges();

		return repository;
	}

	public AuthzUser createUser(final String name, final String alias)
			throws AuthzInvalidUserNameException,
			AuthzUserAlreadyExistsException,
			AuthzUserAliasAlreadyExistsException,
			AuthzInvalidUserAliasException {
		assert users != null;

		// Validate user name and alias
		validateUserName(name);

		final String aliasTrimmed = StringUtils.trimToNull(alias);

        if (aliasTrimmed != null) {
                validateUserAlias(alias);
        }

        final AuthzUser user = new AuthzUser(StringUtils.trim(name), aliasTrimmed);

        users.add(user);

        Collections.sort(users);
		
		setHasUnsavedChanges();

		return user;
	}

	public void deleteAccessRule(final AuthzAccessRule accessRule) {
		assert accessRules != null;
		Preconditions.checkNotNull(accessRule, "Access rule is null");

		final AuthzPermissionable permissionable = accessRule.getPermissionable();

		if (permissionable instanceof AuthzGroupMember) {
			((AuthzGroupMember) permissionable).removeAccessRule(accessRule);
		}

		final AuthzPath path = accessRule.getPath();

		if (path instanceof AuthzPath) {
			((AuthzPath) path).removeAccessRule(accessRule);
		}

		accessRules.remove(accessRule);

		setHasUnsavedChanges();
	}

	public void deleteAccessRules(final AuthzPermissionable permissionable) {
		Preconditions.checkNotNull(permissionable, "Permissionable is null");

		for (final AuthzAccessRule accessRule : permissionable.getAccessRules()) {
			deleteAccessRule(accessRule);
		}

		setHasUnsavedChanges();
	}

	public void deleteGroup(final AuthzGroup group)
			throws AuthzNotMemberOfGroupException, AuthzNotGroupMemberException {
		assert groups != null;

		Preconditions.checkNotNull(group, "Group is null");

		removeGroupMember(group.getGroups(), group);
		removeGroupMembers(group, group.getMembers());
		deleteAccessRules(group);

		groups.remove(group);

		setHasUnsavedChanges();
	}

	public void deletePath(final AuthzPath path) {
		assert paths != null;

		Preconditions.checkNotNull(path, "Path is null");

		paths.remove(path);

		setHasUnsavedChanges();
	}

	public void deleteRepository(final AuthzRepository repository) {
		assert repositories != null;

		Preconditions.checkNotNull(repository, "Repository is null");

		repositories.remove(repository);

		setHasUnsavedChanges();
	}

	public void deleteUser(final AuthzUser user, final String repositoryName)
			throws AuthzNotMemberOfGroupException, AuthzNotGroupMemberException {
		assert users != null;
		
		logger.info("deleting user of repository '{}'...", repositoryName);

		Preconditions.checkNotNull(user, "User is null");

		removeGroupMember(user.getGroups(), user);
        deleteAccessRules(user);

        users.remove(user);

		setHasUnsavedChanges();
	}

	public boolean doesAccessRuleExist(final AuthzPath path,
			final AuthzPermissionable permissionable) {

		// Validation of path and permissionable is done within getAccessRule()

		final boolean doesAccessRuleExist = getAccessRule(path, permissionable) != null;

		return doesAccessRuleExist;
	}

	public boolean doesGroupNameExist(final String name)
			throws AuthzInvalidGroupNameException {
		// Validation of name is done within getGroupWithName()

		final boolean doesGroupNameExist = getGroupWithName(name) != null;

		return doesGroupNameExist;
	}

	public boolean doesPathExist(final AuthzRepository repository,
			final String path) throws AuthzInvalidPathException {
		// Validation of repository and path is done within getPath()

		final boolean doesPathExist = getPath(repository, path) != null;

		return doesPathExist;
	}

	public boolean doesRepositoryNameExist(final String name)
			throws AuthzInvalidRepositoryNameException {
		// Validation of name is done within getRepositoryWithName()

		final boolean doesNameExist = getRepositoryWithName(name) != null;

		return doesNameExist;
	}

	public boolean doesUserAliasExist(final String alias)
			throws AuthzInvalidUserAliasException {
		// Validation of alias is done within getUserWithAlias()

		final boolean doesUserAliasExist = getUserWithAlias(alias) != null;

		return doesUserAliasExist;
	}

	public boolean doesUserNameExist(final String name)
			throws AuthzInvalidUserNameException {
		final boolean doesUserNameExist = getUserWithName(name) != null;

		return doesUserNameExist;
	}

	public AuthzAccessRule getAccessRule(final AuthzPath path,
			final AuthzPermissionable permissionable) {
		assert accessRules != null;

		Preconditions.checkNotNull(path, "Path is null");
		Preconditions.checkNotNull(permissionable, "Permissionable is null");

		AuthzAccessRule foundAccessRule = null;

		for (final AuthzAccessRule accessRule : accessRules) {
			if (accessRule.getPath().equals(path)
					&& accessRule.getPermissionable() != null
					&& accessRule.getPermissionable().equals(permissionable)) {
				foundAccessRule = accessRule;
				break;
			}
		}

		return foundAccessRule;
	}

	public List<AuthzAccessRule> getAccessRules(final String repositoryName) {
		assert accessRules != null;

		return new ImmutableList.Builder<AuthzAccessRule>().addAll(accessRules)
				.build();
	}

	public List<AuthzGroup> getGroups() {
		assert groups != null;

		return new ImmutableList.Builder<AuthzGroup>().addAll(groups).build();
	}

	public AuthzGroup getGroupWithName(final String name)
			throws AuthzInvalidGroupNameException {
		assert groups != null;

		final String nameTrimmed = StringUtils.trimToNull(name);

		if (!AuthzValidatorUtils.isValidGroupName(nameTrimmed)) {
			logger.error("getGroupWithName() invalid group name");

			throw new AuthzInvalidGroupNameException();
		}

		AuthzGroup foundGroup = null;

		for (final AuthzGroup group : groups) {
			if (group.getName().equals(nameTrimmed)) {
				foundGroup = group;
				break;
			}
		}

		return foundGroup;
	}

	public AuthzPath getPath(final AuthzRepository repository, final String path)
			throws AuthzInvalidPathException {
		assert paths != null;

		// Repository is not validated as it many be null

		final String pathTrimmed = StringUtils.trimToNull(path);

		if (!AuthzValidatorUtils.isValidPath(pathTrimmed)) {
			logger.error("getPath() invalid path");

			throw new AuthzInvalidPathException();
		}

		AuthzPath foundPath = null;

		if (repository == null) {
			for (final AuthzPath pathObject : paths) {
				if (pathTrimmed.equals(pathObject.getPath())
						&& pathObject.getRepository() == null) {
					foundPath = pathObject;
					break;
				}
			}
		} else {
			for (final AuthzPath pathObject : paths) {
				if (pathTrimmed.equals(pathObject.getPath())
						&& repository.equals(pathObject.getRepository())) {
					foundPath = pathObject;
					break;
				}
			}
		}

		return foundPath;
	}

	public List<AuthzPath> getPaths() {
		assert paths != null;
		
		return new ImmutableList.Builder<AuthzPath>().addAll(paths).build();
	}

	public List<AuthzRepository> getRepositories() {
		assert repositories != null;

		return new ImmutableList.Builder<AuthzRepository>()
				.addAll(repositories).build();
	}

	public AuthzRepository getRepositoryWithName(final String name)
			throws AuthzInvalidRepositoryNameException {
		assert repositories != null;

		final String nameTrimmed = StringUtils.trimToNull(name);

		if (!AuthzValidatorUtils.isValidRepositoryName(nameTrimmed)) {
			logger.error("getRepositoryWithName() invalid repository name");

			throw new AuthzInvalidRepositoryNameException();
		}

		AuthzRepository foundRepository = null;

		for (final AuthzRepository repository : repositories) {
			if (repository.getName().equals(nameTrimmed)) {
				foundRepository = repository;
				break;
			}
		}

		return foundRepository;
	}

	public List<AuthzUser> getUsers() {
		assert this.users != null;
		
		return new ImmutableList.Builder<AuthzUser>().addAll(users).build();
	}

	public AuthzUser getUserWithAlias(final String alias)
			throws AuthzInvalidUserAliasException {
		assert this.users != null;

		final String aliasTrimmed = StringUtils.trimToNull(alias);

		if (!AuthzValidatorUtils.isValidUserAlias(aliasTrimmed)) {
			logger.error("getUserWithAlias() invalid user alias");

			throw new AuthzInvalidUserAliasException();
		}

		AuthzUser foundUser = null;

		for (final AuthzUser user : users) {
			if (user.getAlias().equals(aliasTrimmed)) {
				foundUser = user;
				break;
			}
		}

		return foundUser;
	}

	public AuthzUser getUserWithName(final String name)
			throws AuthzInvalidUserNameException {
		assert this.users != null;		

		final String nameTrimmed = StringUtils.trimToNull(name);

		if (!AuthzValidatorUtils.isValidUserName(nameTrimmed)) {
			throw new AuthzInvalidUserNameException();
		}

		AuthzUser foundUser = null;

		for (final AuthzUser user : users) {
			if (user.getName().equals(nameTrimmed)) {
				foundUser = user;
				break;
			}
		}

		return foundUser;
	}

	public boolean hasUnsavedChanges() {
		return hasUnsavedChanges;
	}

	public void initialize() {
		accessRules = new ArrayList<AuthzAccessRule>();
		groups = new ArrayList<AuthzGroup>();
		paths = new ArrayList<AuthzPath>();
		repositories = new ArrayList<AuthzRepository>();
		users = new ArrayList<AuthzUser>();

		clearHasUnsavedChanges();
	}

	public void removeGroupMember(final AuthzGroup group,
			final AuthzGroupMember member)
			throws AuthzNotMemberOfGroupException, AuthzNotGroupMemberException {

		Preconditions.checkNotNull(group, "Group is null");
		Preconditions.checkNotNull(member, "Member is null");

		if (group instanceof AuthzGroup) {
			((AuthzGroup) group).removeMember(member);
		}

		if (group instanceof AuthzGroupMember) {
			((AuthzGroupMember) member).removeGroup(group);
		}

		setHasUnsavedChanges();
	}

	public void removeGroupMember(final Collection<AuthzGroup> groups,
			final AuthzGroupMember member)
			throws AuthzNotMemberOfGroupException, AuthzNotGroupMemberException {
		Preconditions.checkNotNull(groups, "Groups is null");
		Preconditions.checkNotNull(member, "Member is null");

		for (final AuthzGroup group : groups) {
			removeGroupMember(group, member);
		}

		setHasUnsavedChanges();
	}

	public void removeGroupMembers(final AuthzGroup group,
			final Collection<AuthzGroupMember> members)
			throws AuthzNotMemberOfGroupException, AuthzNotGroupMemberException {
		Preconditions.checkNotNull(group, "Group is null");
		Preconditions.checkNotNull(members, "Members is null");

		for (final AuthzGroupMember member : members) {
			removeGroupMember(group, member);
		}

		setHasUnsavedChanges();
	}

	public void renameGroup(final AuthzGroup group, final String newGroupName)
			throws AuthzInvalidGroupNameException,
			AuthzGroupAlreadyExistsException {
		// Validate group name
		validateGroupName(newGroupName);

		if (group instanceof AuthzAbstractNamed) {
			((AuthzAbstractNamed) group).setName(newGroupName.trim());
		}

		setHasUnsavedChanges();
	}

	public void renameRepository(final AuthzRepository repository,
			final String newRepositoryName)
			throws AuthzUserAlreadyExistsException,
			AuthzInvalidUserNameException {
		// Validate new user name
		//FIXME 왜 UserName을 체크하지? (보니까  아래 renameUser를 그냥 카피한 것 같음)
		//validateUserName(newRepositoryName);

		if (repository instanceof AuthzAbstractNamed) {
			((AuthzAbstractNamed) repository).setName(newRepositoryName.trim());
		}

		setHasUnsavedChanges();
	}

	public void renameUser(final AuthzUser user, final String newUserName)
			throws AuthzUserAlreadyExistsException,
			AuthzInvalidUserNameException {
		// Validate new user name
		validateUserName(newUserName);

		if (user instanceof AuthzAbstractNamed) {
			((AuthzAbstractNamed) user).setName(newUserName.trim());
		}

		setHasUnsavedChanges();
	}

	public void renameUserAlias(final AuthzUser user, final String newAlias)
			throws AuthzUserAliasAlreadyExistsException,
			AuthzInvalidUserAliasException {
		final String aliasTrimmed = StringUtils.trimToNull(newAlias);

		// Validate new user alias if not null. Aliases may be null.
		if (aliasTrimmed != null) {
			validateUserAlias(newAlias);
		}

		if (user instanceof AuthzUser) {
			((AuthzUser) user).setAlias(aliasTrimmed);
		}

		setHasUnsavedChanges();
	}

	/**
	 * Sets "has unsaved changes" flag to true.
	 */
	protected void setHasUnsavedChanges() {
		hasUnsavedChanges = true;
	}

	/**
	 * Creates a string representation of this document.
	 * 
	 * @return String representation of this document
	 */
	@Override
	public String toString() {
		assert accessRules != null;
		assert groups != null;
		assert paths != null;
		assert repositories != null;
		assert users != null;

		final ToStringBuilder toStringBuilder = new ToStringBuilder(this);

		toStringBuilder.append("accessRules", accessRules.size());
		toStringBuilder.append("groups", groups.size());
		toStringBuilder.append("paths", paths.size());
		toStringBuilder.append("repositories", repositories.size());
		toStringBuilder.append("users", users.size());
		toStringBuilder.append("hasUnsavedChanges", hasUnsavedChanges);

		return toStringBuilder.toString();
	}

	/**
	 * Validates that the provided group name is valid and is not already in
	 * use.
	 * 
	 * @param name
	 *            Name to validate
	 * @throws AuthzInvalidGroupNameException
	 *             If provided group name is invalid
	 * @throws AuthzGroupAlreadyExistsException
	 *             If group with the provided group name already exists
	 */
	protected void validateGroupName(final String name)
			throws AuthzInvalidGroupNameException,
			AuthzGroupAlreadyExistsException {
		final String nameTrimmed = StringUtils.trimToNull(name);

		if (!AuthzValidatorUtils.isValidGroupName(nameTrimmed)) {
			logger.error("validateGroupName() invalid group name");

			throw new AuthzInvalidGroupNameException();
		}

		if (doesGroupNameExist(nameTrimmed)) {
			logger.info("validateGroupName() group already exists");

			throw new AuthzGroupAlreadyExistsException();
		}
	}

	/**
	 * Validates that the provided repository name is valid and is not already
	 * in use.
	 * 
	 * @param name
	 *            Name to validate
	 * @throws AuthzInvalidRepositoryNameException
	 *             If provided repository name is invalid
	 * @throws AuthzRepositoryAlreadyExistsException
	 *             If repository with the provided name already exists
	 */
	protected void validateRepositoryName(final String name)
			throws AuthzRepositoryAlreadyExistsException,
			AuthzInvalidRepositoryNameException {
		final String nameTrimmed = StringUtils.trimToNull(name);

		// Validate repository name
		if (!AuthzValidatorUtils.isValidRepositoryName(nameTrimmed)) {
			logger.error("validateRepositoryName() invalid repository name");

			throw new AuthzInvalidRepositoryNameException();
		}

		// Check for existing repositories with same name
		if (doesRepositoryNameExist(nameTrimmed)) {
			logger.info("validateRepositoryName() repository already exists");

			throw new AuthzRepositoryAlreadyExistsException();
		}
	}

	/**
	 * Validates that the provided user name is valid and is not already in use.
	 * 
	 * @param alias
	 *            Name to validate
	 * @throws AuthzUserAliasAlreadyExistsException
	 *             If user with the provided alias already exists
	 * @throws AuthzInvalidUserAliasException
	 *             the authz invalid user alias exception
	 */
	protected void validateUserAlias(final String alias)
			throws AuthzUserAliasAlreadyExistsException,
			AuthzInvalidUserAliasException {

		final String aliasTrimmed = StringUtils.trimToNull(alias);

		if (!AuthzValidatorUtils.isValidUserAlias(aliasTrimmed)) {
			logger.error("validateUserAlias() invalid user alias");

			throw new AuthzInvalidUserAliasException();
		}

		if (doesUserAliasExist(aliasTrimmed)) {
			logger.info("validateUserAlias() user alias already exists");

			throw new AuthzUserAliasAlreadyExistsException();
		}
	}

	/**
	 * Validates that the provided user name is valid and is not already in use.
	 * 
	 * @param name
	 *            Name to validate
	 * @throws AuthzUserAlreadyExistsException
	 *             If user with the provided name already exists
	 * @throws AuthzInvalidUserNameException
	 *             If provided user name is invalid@throws
	 *             AuthzInvalidUserNameException
	 */
	protected void validateUserName(final String name)
			throws AuthzUserAlreadyExistsException,
			AuthzInvalidUserNameException {
		final String nameTrimmed = StringUtils.trimToNull(name);

		if (!AuthzValidatorUtils.isValidUserName(nameTrimmed)) {
			logger.error("validateUserName() invalid user name");

			throw new AuthzInvalidUserNameException();
		}

		// Check for existing users with same user name or alias
		if (doesUserNameExist(nameTrimmed)) {
			logger.info("validateUserName() user '{}' already exists", name);

			throw new AuthzUserAlreadyExistsException();
		}
	}
}
