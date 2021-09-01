package io.teamcode.common.security.svn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

public abstract class AuthzAbstractNamed implements AuthzNamed {

	private static final long serialVersionUID = 1458605651714193404L;

	private String name;

	protected AuthzAbstractNamed(final String name) {
		super();

		Preconditions.checkNotNull(name, "Name is null");

		this.name = name;
	}

	@Override
	public final int compareTo(final AuthzNamed that) {
		return ComparisonChain.start().compare(this.name, that.getName())
				.result();
	}

	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	protected void setName(final String name) {
		this.name = name;
	}
}
