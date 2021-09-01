package io.teamcode.common.security.svn;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class AuthzUser extends AuthzGroupMember {

	private static final long serialVersionUID = -6083705444336488621L;

	private String alias;
	
	public AuthzUser(final String name, final String alias) {
		super(name);
		this.alias = alias;
	}

	/**
	 * Compares this object with the provided AuthzUser object for equality.
	 * 
	 * @param object
	 *            Object to compare
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
		final AuthzUser other = (AuthzUser) object;
		if (alias == null) {
			if (other.alias != null) {
				return false;
			}
		} else if (!alias.equals(other.alias)) {
			return false;
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

	public String getAlias() {
		return this.alias;
	}

	/**
	 * Calculates hashCode value of this user.
	 * 
	 * @return Hashcode of this object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (alias == null ? 0 : alias.hashCode());
		result = prime * result
				+ (getName() == null ? 0 : getName().hashCode());
		return result;
	}

	/**
	 * Sets the alias.
	 */
	protected void setAlias(final String alias) {
		this.alias = alias;
	}

	/**
	 * Creates a string representation of this user.
	 * 
	 * @return String representation of this user
	 */
	@Override
	public String toString() {
		final ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("name", getName()).append("alias", alias);

		return toStringBuilder.toString();
	}
}
