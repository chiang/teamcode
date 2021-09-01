package io.teamcode.common.security.svn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.io.Serializable;

public class AuthzAccessRule implements Comparable<AuthzAccessRule>, Serializable {
	
	private static final long serialVersionUID = 4600849211692988632L;

	/** The access level. */
    private final AuthzPrivilege authzPrivilege;

    /** The path. */
    private final AuthzPath path;

    /** The group. */
    private final AuthzPermissionable permissionable;

    /**
     * Instantiates a new authz access rule.
     * 
     * @param path the path
     * @param permissionable the permissionable object
     * @param accessLevel the access level
     */
    protected AuthzAccessRule(final AuthzPath path, final AuthzPermissionable permissionable,
                    final AuthzPrivilege authzPrivilege) {
            super();

            Preconditions.checkNotNull(path, "Path is null");
            Preconditions.checkNotNull(permissionable, "Permissionable is null");
            Preconditions.checkNotNull(authzPrivilege, "Access level is null");

            this.path = path;
            this.permissionable = permissionable;
            this.authzPrivilege = authzPrivilege;
    }

    /**
     * Compares this object with the provided AuthzAccessRule object.
     * 
     * @param authzAccessRule AuthzAccessRule to compare
     * @return Returns 0 if rules are equal, less than 0 if this rule is less than the other or greater than 0 if this
     *         rule is greater
     */
    @Override
    public int compareTo(final AuthzAccessRule authzAccessRule) {
            final ComparisonChain comparisonChain = ComparisonChain.start();

            comparisonChain.compare(this.path, authzAccessRule.getPath());
            comparisonChain.compare(this.permissionable, authzAccessRule.getPermissionable(), Ordering.natural()
                            .nullsLast());
            comparisonChain.compare(this.authzPrivilege, authzAccessRule.getAuthzPrivilege());

            return comparisonChain.result();
    }

    public AuthzPrivilege getAuthzPrivilege() {
            return authzPrivilege;
    }

    public AuthzPath getPath() {
            return path;
    }

    public AuthzPermissionable getPermissionable() {
            return permissionable;
    }
}
