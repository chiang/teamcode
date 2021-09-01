package io.teamcode.common.security.svn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 아래와 같은 포맷에서 [] 안의 것을 표현하는 클래스. project1은 Repository의 이름이다.
 * 
 * <pre>
 * # Repositories configuration
 * [project1:/]
 * @project1 = rw
 * </pre>
 * 
 * 
 * @author chiang
 *
 */
public final class AuthzPath implements Comparable<AuthzPath>, Serializable {

	private static final long serialVersionUID = -2041486038333760664L;
	
	private static final Logger logger = LoggerFactory.getLogger(AuthzPath.class);

	/** The access rules. */
	private final List<AuthzAccessRule> accessRules = new ArrayList<AuthzAccessRule>();

	/** Path string. */
	private final String path;

	/** Repository object. */
	private final AuthzRepository repository;

	/**
	 * Constructor.
	 * 
	 * @param repository
	 *            Repository
	 * @param path
	 *            Path
	 */
	public AuthzPath(final AuthzRepository repository, final String path) {
		super();

		Preconditions.checkNotNull(path);

		this.repository = repository;
		this.path = path;
	}
	
	public final boolean isParentPath() {
		return "PARENT".equals(this.path);
	}

	/**
	 * Adds access rule to collection access rules.
	 * 
	 * @param accessRule
	 *            Access rule to add to collection
	 * @return True if access rule added
	 * @throws AuthzAccessRuleAlreadyAppliedException
	 *             If the access rule is already applied to the member
	 */
	final boolean addAccessRule(final AuthzAccessRule accessRule)
			throws AuthzAccessRuleAlreadyAppliedException {
		assert accessRules != null;
		Preconditions.checkNotNull(accessRule, "access rule is null.");

		if (accessRules.contains(accessRule)) {
			logger.error("addAccessRule() already a member of group");

			throw new AuthzAccessRuleAlreadyAppliedException();
		}

		if (accessRules.add(accessRule)) {
			Collections.sort(accessRules);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Compares this object with the provided AuthzPath object.
	 * 
	 * @param that
	 *            AuthzPath to compare
	 * @return Returns 0 if paths are equal, less than 0 if this path is less
	 *         than the other or greater than 0 if this path is greater
	 */
	@Override
	public int compareTo(final AuthzPath that) {
		return ComparisonChain
				.start()
				.compare(this.repository, that.getRepository(),
						Ordering.natural().nullsLast())
				.compare(this.path, that.getPath()).result();
	}

	/**
	 * Compares this object with the provided AuthzPath object for equality.
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
		final AuthzPath other = (AuthzPath) object;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (repository == null) {
			if (other.repository != null) {
				return false;
			}
		} else if (!repository.equals(other.repository)) {
			return false;
		}
		return true;
	}

	public List<AuthzAccessRule> getAccessRules() {
		assert accessRules != null;

		Collections.unmodifiableCollection(accessRules);

		return new ImmutableList.Builder<AuthzAccessRule>().addAll(accessRules)
				.build();
	}

	public String getPath() {
		return path;
	}

	public AuthzRepository getRepository() {
		return repository;
	}

	/**
	 * Calculates hashCode value of this path.
	 * 
	 * @return Hashcode of this object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (path == null ? 0 : path.hashCode());
		result = prime * result
				+ (repository == null ? 0 : repository.hashCode());
		return result;
	}

	/**
	 * Removes an access rule from the collection of access rules.
	 * 
	 * @param accessRule
	 *            Access rule to remove
	 * @return True if access rule removed
	 */
	protected final boolean removeAccessRule(final AuthzAccessRule accessRule) {
		assert accessRule != null;

		final boolean removed = accessRules.remove(accessRule);

		return removed;
	}

	/**
	 * Creates a string representation of this user.
	 * 
	 * @return String representation of this user
	 */
	@Override
	public String toString() {
		final ToStringBuilder toStringBuilder = new ToStringBuilder(this);

		toStringBuilder.append("repository", repository).append("path", path);

		return toStringBuilder.toString();
	}
}
