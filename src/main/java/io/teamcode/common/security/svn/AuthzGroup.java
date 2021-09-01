package io.teamcode.common.security.svn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class AuthzGroup extends AuthzGroupMember {

	private static final long serialVersionUID = -4191841942424960445L;

	private static final Logger logger = LoggerFactory
			.getLogger(AuthzGroup.class);

	private final List<AuthzGroup> groups = new ArrayList<AuthzGroup>();

	private final List<AuthzUser> users = new ArrayList<AuthzUser>();

	public AuthzGroup(final String name) {
		super(name);
	}
	
	public boolean exist(AuthzUser authzUser) {
		return users.contains(authzUser);
	}

	/**
	 * Adds a group member.
	 * 
	 * @param group
	 *            AuthzGroup member to add
	 * @return True if member added
	 * @throws AuthzGroupMemberAlreadyExistsException
	 *             If group member already exists
	 */
	protected boolean addMember(final AuthzGroup group)
			throws AuthzGroupMemberAlreadyExistsException {
		assert groups != null;

		logger.debug("addMember(AuthzGroup) entered. group={}", group);

		Preconditions.checkNotNull(group, "Group is null");

		if (groups.contains(group)) {
			logger.error("addMember(AuthzGroup) group member already exists");

			throw new AuthzGroupMemberAlreadyExistsException();
		}

		if (groups.add(group)) {
			Collections.sort(groups);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a member.
	 * 
	 * @param member
	 *            AuthzGroupMember member to add
	 * @return True if member added
	 * @throws AuthzGroupMemberAlreadyExistsException
	 *             If user member already exists
	 */
	public boolean addMember(final AuthzGroupMember member)
			throws AuthzGroupMemberAlreadyExistsException {

		if (member instanceof AuthzGroup) {
			return addMember((AuthzGroup) member);
		} else if (member instanceof AuthzUser) {
			return addMember((AuthzUser) member);
		} else {
			return false;
		}
	}

	/**
	 * Adds a user member.
	 * 
	 * @param user
	 *            AuthzUser member to add
	 * @return True if member added
	 * @throws AuthzGroupMemberAlreadyExistsException
	 *             If user member already exists
	 */
	public boolean addMember(final AuthzUser user)
			throws AuthzGroupMemberAlreadyExistsException {
		assert users != null;

		logger.debug("addMember(AuthzUser) entered. user={}", user);

		Preconditions.checkNotNull(user, "User is null");

		if (users.contains(user)) {
			logger.error("addMember(AuthzUser) user member already exists");

			throw new AuthzGroupMemberAlreadyExistsException("user member already exists");
		}

		if (users.add(user)) {
			Collections.sort(users);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Compares this object with the provided AuthzGroup object for equality.
	 * 
	 * @param object
	 *            Object to compare
	 * @return True if this object matches the provided object, otherwise false
	 */
	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		final AuthzGroup other = (AuthzGroup) object;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

	public List<AuthzGroup> getGroupMembers() {
		assert groups != null;

		return new ImmutableList.Builder<AuthzGroup>().addAll(groups).build();
	}

	public List<AuthzGroupMember> getMembers() {
		assert groups != null;
		assert users != null;

		final Collection<AuthzGroupMember> members = new ArrayList<AuthzGroupMember>(
				groups.size() + users.size());

		/*members.addAll(groups);
		members.addAll(users);*/

		return new ImmutableList.Builder<AuthzGroupMember>().addAll(members)
				.build();
	}

	public List<AuthzUser> getUserMembers() {
		assert users != null;

		return new ImmutableList.Builder<AuthzUser>().addAll(users).build();
	}

	/**
	 * Calculates hashCode value of this group.
	 * 
	 * @return Hashcode of this object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (getName() == null ? 0 : getName().hashCode());
		return result;
	}

	/**
	 * Remove group member.
	 * 
	 * @param group
	 *            Member group to remove
	 * @return True if member removed
	 * @throws AuthzNotGroupMemberException
	 *             If provided member object is not a member of this group.
	 */
	public boolean removeMember(final AuthzGroup group)
			throws AuthzNotGroupMemberException {
		assert groups != null;

		logger.debug("removeMember(AuthzGroup) entered. group={}", group);

		Preconditions.checkNotNull(group, "Group is null");

		if (!groups.contains(group)) {
			logger.error("removeMember(AuthzGroup) group is not a member of this group");

			throw new AuthzNotGroupMemberException();
		}

		return groups.remove(group);
	}

	/**
	 * Removes a member.
	 * 
	 * @param member
	 *            AuthzGroupMember member to remove
	 * @return True if member removed
	 * @throws AuthzNotGroupMemberException
	 *             If user member already exists
	 */
	public boolean removeMember(final AuthzGroupMember member)
			throws AuthzNotGroupMemberException {
		/*if (member instanceof AuthzGroup) {
			return removeMember((AuthzGroup) member);
		} else if (member instanceof AuthzUser) {
			return removeMember((AuthzUser) member);
		} else {
			return false;
		}*/
		
		return true;
	}

	/**
	 * Remove user member.
	 * 
	 * @param user
	 *            Member user to remove
	 * @return True if member removed
	 * @throws AuthzNotGroupMemberException
	 *             If provided member object is not a member of this group.
	 */
	public boolean removeMember(final AuthzUser user)
			throws AuthzNotGroupMemberException {
		assert users != null;

		logger.debug("removeMember(AuthzUser) entered. user={}", user);

		Preconditions.checkNotNull(user, "User is null");

		if (!users.contains(user)) {
			throw new AuthzNotGroupMemberException();
		}

		return users.remove(user);
	}

	/**
	 * Creates a string representation of this group.
	 * 
	 * @return String representation of this group
	 */
	@Override
	public String toString() {
		final ToStringBuilder toStringBuilder = new ToStringBuilder(this);

		toStringBuilder.append("name", getName());

		return toStringBuilder.toString();
	}
}
