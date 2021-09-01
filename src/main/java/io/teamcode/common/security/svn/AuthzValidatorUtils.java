package io.teamcode.common.security.svn;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public final class AuthzValidatorUtils {
	
	private static final Logger logger = LoggerFactory
			.getLogger(AuthzValidatorUtils.class);

	/** Regular expression pattern for matching valid path values. */
	private static final Pattern VALID_PATH_PATTERN = Pattern
			.compile("^(/)|(/.*[^/])$");

	/**
	 * Checks group name for validity.
	 * 
	 * @param name
	 *            Group name to check
	 * @return True if group name is valid, otherwise false
	 */
	public static boolean isValidGroupName(final String name) {
		final boolean isValidGroupName = StringUtils.isNotBlank(name);

		return isValidGroupName;
	}

	/**
	 * Checks path for validity. Paths must start with a slash (/), but must not
	 * end with a slash (/), except for when path consists of a single slash
	 * (/).
	 * 
	 * @param path
	 *            Path to validate
	 * @return True if path is valid, otherwise false
	 */
	public static boolean isValidPath(final String path) {
		final boolean isValidPath = VALID_PATH_PATTERN.matcher(
				StringUtils.trimToEmpty(path)).matches();

		return isValidPath;
	}

	/**
	 * Checks repository name for validity.
	 * 
	 * @param name
	 *            Repository name to check
	 * @return True if repository name is valid, otherwise false
	 */
	public static boolean isValidRepositoryName(final String name) {
		final boolean isValidRepositoryName = StringUtils.isNotBlank(name);
		return isValidRepositoryName;
	}

	/**
	 * Checks user alias for validity.
	 * 
	 * @param alias
	 *            User alias to check
	 * @return True if user alias is valid, otherwise false
	 */
	public static boolean isValidUserAlias(final String alias) {
		final boolean isValidUserAlias = StringUtils.isNotBlank(alias);

		return isValidUserAlias;
	}

	/**
	 * Checks user name for validity.
	 * 
	 * @param name
	 *            User name to check
	 * @return True if user name is valid, otherwise false
	 */
	public static boolean isValidUserName(final String name) {
		final boolean isValidUserName = StringUtils.isNotBlank(name);

		return isValidUserName;
	}

	/**
	 * Instantiates a new authz validator utils.
	 */
	private AuthzValidatorUtils() {
		throw new AssertionError();
	}
}
