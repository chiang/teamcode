package io.teamcode.common.security.svn;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class AuthzRepository extends AuthzAbstractNamed {

	private static final long serialVersionUID = -4189629458598272420L;
	
	public static final String NULL_REPOSITORY_IDENTIFIER = "NULL";

	public AuthzRepository(final String name) {
		super(name);
	}

	/**
	 * Compares this object with the provided AuthzRepository object for
	 * equality.
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
		final AuthzRepository other = (AuthzRepository) object;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

	/**
	 * Calculates hashCode value of this repository.
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
	 * Creates a string representation of this repository.
	 * 
	 * @return String representation of this repository
	 */
	@Override
	public String toString() {
		final ToStringBuilder toStringBuilder = new ToStringBuilder(this);

		toStringBuilder.append("name", getName());

		return toStringBuilder.toString();
	}

}
