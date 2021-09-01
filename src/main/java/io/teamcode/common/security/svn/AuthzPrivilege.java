package io.teamcode.common.security.svn;

public enum AuthzPrivilege {
	DENY_ACCESS(""),

	READ_ONLY("r"),

	READ_WRITE("rw");

	public static AuthzPrivilege getByCode(final String code) {
		if (code.equals(DENY_ACCESS.privilege)) {
			return DENY_ACCESS;
		} else if (code.equals(READ_ONLY.privilege)) {
			return READ_ONLY;
		} else if (code.equals(READ_WRITE.privilege)) {
			return READ_WRITE;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private final String privilege;

	private AuthzPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public String getPrivilege() {
		return this.privilege;
	}

	@Override
	public String toString() {
		return privilege;
	}

}
