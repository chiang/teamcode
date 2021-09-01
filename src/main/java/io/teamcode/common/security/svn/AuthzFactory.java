package io.teamcode.common.security.svn;

public final class AuthzFactory {

	/** The Constant INSTANCE. */
	private static final AuthzFactory INSTANCE = new AuthzFactory();

	/**
	 * Gets the single instance of AuthzDocumentFactory.
	 * 
	 * @return single instance of AuthzDocumentFactory
	 */
	public static AuthzFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Instantiates a new authz document factory.
	 */
	private AuthzFactory() {
		super();
	}

	public Authz create() {
		return new Authz();
	}
}
