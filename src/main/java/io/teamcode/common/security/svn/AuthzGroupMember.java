package io.teamcode.common.security.svn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AuthzGroupMember extends AuthzAbstractNamed implements AuthzPermissionable {

	private static final long serialVersionUID = 4551615643098230175L;
	
	private static final Logger logger = LoggerFactory.getLogger(AuthzGroupMember.class);

    /** Collection of access rules that apply to this member. */
    private final List<AuthzAccessRule> accessRules = new ArrayList<AuthzAccessRule>();

    /** Collection of groups of which is a member. */
    private final List<AuthzGroup> groups = new ArrayList<AuthzGroup>();

    public AuthzGroupMember(final String name) {
           super(name);
    }

    @Override
    public final List<AuthzAccessRule> getAccessRules() {
    	assert accessRules != null;

    	return new ImmutableList.Builder<AuthzAccessRule>().addAll(accessRules).build();
    }

    /**
     * Adds access rule to collection access rules.
     * 
     * @param accessRule Access rule to add to collection
     * @return True if access rule added
     * @throws AuthzAccessRuleAlreadyAppliedException If the access rule is already applied to the member
     */
    protected final boolean addAccessRule(final AuthzAccessRule accessRule)
                    throws AuthzAccessRuleAlreadyAppliedException {
            assert accessRules != null;

            Preconditions.checkNotNull(accessRule, "Access Rule is null");

            if (accessRules.contains(accessRule)) {

                    throw new AuthzAccessRuleAlreadyAppliedException();
            }

            if (accessRules.add(accessRule)) {
                    Collections.sort(accessRules);

                    return true;
            }
            else {
                    return false;
            }
    }

    /**
     * Adds group to collection of groups.
     * 
     * @param group Group to add to collection
     * @return True if group added
     * @throws AuthzAlreadyMemberOfGroupException If this object is already a member of the group
     */
	protected final boolean addGroup(final AuthzGroup group)
			throws AuthzAlreadyMemberOfGroupException {
		assert groups != null;

		Preconditions.checkNotNull(group, "Group is null");

		if (groups.contains(group)) {
			logger.error("addGroup() already a member of group");

			throw new AuthzAlreadyMemberOfGroupException(
					"already a member of group");
		}

		if (groups.add(group)) {
			Collections.sort(groups);

			return true;
		} else {
			return false;
		}
	}

    public final List<AuthzGroup> getGroups() {
            assert groups != null;

            return new ImmutableList.Builder<AuthzGroup>().addAll(groups).build();
    }

    /**
     * Removes an access rule from the collection of access rules.
     * 
     * @param accessRule Access rule to remove
     * @return True if access rule removed
     */
	final boolean removeAccessRule(final AuthzAccessRule accessRule) {
		assert accessRule != null;

		logger.debug("removeAccessRule() entered. accessRule={}", accessRule);

		final boolean removed = accessRules.remove(accessRule);

		logger.debug("removeAccessRule() exited, returning {}", removed);

		return removed;
	}

    /**
     * Remove a group from the collection of groups.
     * 
     * @param group Group to remove from the collection
     * @return True if group is removed
     * @throws AuthzNotMemberOfGroupException If this object is not a member of the provided group
     */
    protected final boolean removeGroup(final AuthzGroup group) throws AuthzNotMemberOfGroupException {
            assert groups != null;

            logger.debug("removeGroup() entered. group={}", group);

            Preconditions.checkNotNull(group, "Group is null");

            if (!groups.contains(group)) {
                    logger.error("removeGroup() this object is not a member of the group");

                    throw new AuthzNotMemberOfGroupException("this object is not a member of the group");
            }

            return groups.remove(group);
    }

}
