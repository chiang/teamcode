package io.teamcode.common.security.svn;

import io.teamcode.common.TeamcodeConstants;

import java.io.*;
import java.util.Iterator;

public final class AuthzFileGenerator {
	private static final String TEXT_NEW_LINE = System
			.getProperty("line.separator");

	private final int DEFAULT_MAX_LINE_LENGTH = 80;

	private final Authz authz;

	public AuthzFileGenerator(final Authz authz) {
		super();

		this.authz = authz;
	}

	private String createPrefix(final int length) {
		final StringBuilder builder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			builder.append(" ");
		}

		return builder.toString();
	}

	public String generate(final boolean allowMultipleLine) {
		return generate(allowMultipleLine ? DEFAULT_MAX_LINE_LENGTH : -1);
	}

	public void generate(final File file, final boolean allowMultipleLine) {
		generate(file, allowMultipleLine ? DEFAULT_MAX_LINE_LENGTH : -1);
	}

	public void generate(final File file, final int maxLineLength) {
		PrintWriter output = null;

		try {
			output = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			// Process group definitions
			output.print(generate(maxLineLength));
		} catch (final FileNotFoundException fne) {
			throw new RuntimeException("generator.filenotfound");
		} catch (final IOException ioe) {
			throw new RuntimeException("generator.error");
		} catch (final Exception e) {
			throw new RuntimeException("generator.error");
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	public String generate(final int maxLineLength) {
		final StringBuilder output = new StringBuilder();

		try {
			output.append("# ");
			// output.append(AuthzResources.getString(AuthzMessageResourceKey.APPLICATION_FILE_HEADER));
			output.append("Teamcode managed authz file");
			output.append(TEXT_NEW_LINE).append(TEXT_NEW_LINE);

			// Process alias definitions
			final StringBuilder aliases = new StringBuilder();

			// FIXME User만 따로 출력하는 곳은 없다?
			/*
			 * for (final AuthzUser user : authz.getUsersOfRepository()) { if
			 * (user.getAlias() == null) { continue; }
			 * 
			 * aliases.append(user.getAlias()); aliases.append(" = ");
			 * aliases.append(user.getName()); aliases.append(TEXT_NEW_LINE); }
			 */

			if (aliases.length() > 0) {
				output.append("[aliases]");
				output.append(TEXT_NEW_LINE);
				output.append(aliases);

				if (authz.getGroups().size() > 0) {
					output.append(TEXT_NEW_LINE);
				}
			}

			// Process group definitions
			output.append("[groups]");
			output.append(TEXT_NEW_LINE);

			for (final AuthzGroup group : authz.getGroups()) {
				output.append(group.getName());
				output.append(" = ");

				final String prefix = createPrefix(group.getName().length() + 3);
				boolean isFirstGroupMember = true;

				if (!group.getGroupMembers().isEmpty()) {
					final Iterator<AuthzGroup> members = group
							.getGroupMembers().iterator();

					StringBuffer groupLine = new StringBuffer();

					while (members.hasNext()) {
						final AuthzGroup memberGroup = members.next();

						if (maxLineLength > 0
								&& !isFirstGroupMember
								&& groupLine.length()
										+ memberGroup.getName().length() > maxLineLength) {
							output.append(groupLine);
							output.append(TEXT_NEW_LINE);
							output.append(prefix);

							groupLine = new StringBuffer();
						}

						groupLine.append("@");
						groupLine.append(memberGroup.getName());

						// Add comma if more members exist
						if (members.hasNext()) {
							groupLine.append(", ");
						}

						isFirstGroupMember = false;
					}

					output.append(groupLine);
				}

				if (!group.getUserMembers().isEmpty()) {
					if (!group.getGroupMembers().isEmpty()) {
						output.append(", ");
					}

					final Iterator<AuthzUser> members = group.getUserMembers()
							.iterator();

					StringBuffer userLine = new StringBuffer();

					while (members.hasNext()) {
						final AuthzUser memberUser = members.next();
						final String nameAlias = memberUser.getAlias() == null ? memberUser
								.getName() : "&" + memberUser.getAlias();

						if (maxLineLength > 0
								&& !isFirstGroupMember
								&& userLine.length() + nameAlias.length() > maxLineLength) {
							output.append(userLine);
							output.append(TEXT_NEW_LINE);
							output.append(prefix);

							userLine = new StringBuffer();
						}

						userLine.append(nameAlias);

						// Add comma if more members exist
						if (members.hasNext()) {
							userLine.append(", ");
						}

						isFirstGroupMember = false;
					}

					output.append(userLine);
				}

				output.append(TEXT_NEW_LINE);
			}

			if (authz.getPaths().size() > 0) {
				output.append(TEXT_NEW_LINE);
			}

			// Process access rules
			for (final AuthzPath path : authz.getPaths()) {
				if (path.getAccessRules().size() == 0) {
					continue;
				}

				if (path.getRepository() == null) {
					// Server permissions 이건 안 쓴다.
					/*output.append("[");
					output.append(path.getPath());
					output.append("]");
					output.append(TEXT_NEW_LINE);*/
					
					// repository parent permissions
					continue;
				}
				else if (path.isParentPath()) {
					continue;
				}
				else {
					// Path permissions
					output.append("[");
					output.append(path.getRepository().getName());
					output.append(":");
					output.append(path.getPath());
					output.append("]");
					output.append(TEXT_NEW_LINE);
				}

				for (final AuthzAccessRule rule : path.getAccessRules()) {
					final AuthzPermissionable permissionable = rule
							.getPermissionable();

					if (permissionable instanceof AuthzGroup) {
						final AuthzGroup group = (AuthzGroup) permissionable;

						output.append("@");
						output.append(group.getName());
						output.append(" = ");
						output.append(rule.getAuthzPrivilege().getPrivilege());
						output.append(TEXT_NEW_LINE);
					} else if (permissionable instanceof AuthzUser) {
						final AuthzUser user = (AuthzUser) permissionable;

						if (user.getAlias() == null) {
							output.append(user.getName());
						} else {
							output.append("&");
							output.append(user.getAlias());
						}

						output.append(" = ");
						output.append(rule.getAuthzPrivilege().getPrivilege());
						output.append(TEXT_NEW_LINE);
					} else {
						throw new RuntimeException("generator.error");
					}
				}

				output.append(TEXT_NEW_LINE);
			}
			
			generateRepositoryRootAuthz(output);
		} catch (final Exception e) {
			throw new RuntimeException("generator.error");
		}

		return output.toString();
	}
	
	private void generateRepositoryRootAuthz(final StringBuilder output) {
		output.append("[/]");
		output.append(TEXT_NEW_LINE);
		//output.append("@administer = r");
		//output.append(TEXT_NEW_LINE);
		output.append(TeamcodeConstants.DEFAULT_AUTHZ_ADMIN);
		output.append(" = rw");
		output.append(TEXT_NEW_LINE);
		output.append("* =");
	}
	
	/**
	 * 이 버전은 나중에 기능이 업그레이드되면 사용하기로 함. 현재는 그냥 위의 Method 처럼 하드코딩함.
	 * 
	 * <p>[/]와 같이 모든 Repository에 적용되는 권한은 맨 마지막에 추가해야 하므로 이렇게 따로 설정함.</p>
	 * <p>원래는 Multi Repository가 아닌 Single Repository를 위해서 [/trunk] 와 같이 설정해서
	 * 사용하는 것이나 우리 제품에서는 무조건 Multi만 다루므로 [/]로만 설정한다.</p>
	 * 
	 */
	private void generateRepositoryRootAuthzForAdvanced(final StringBuilder output) {
		for (final AuthzPath path : authz.getPaths()) {
			if (path.getRepository() == null) {
				output.append("[/]");
				output.append(TEXT_NEW_LINE);
				
				for (final AuthzAccessRule rule : path.getAccessRules()) {
					final AuthzPermissionable permissionable = rule
							.getPermissionable();

					if (permissionable instanceof AuthzGroup) {
						final AuthzGroup group = (AuthzGroup) permissionable;

						output.append("@");
						output.append(group.getName());
						output.append(" = ");
						output.append(rule.getAuthzPrivilege().getPrivilege());
						output.append(TEXT_NEW_LINE);
					} else if (permissionable instanceof AuthzUser) {
						final AuthzUser user = (AuthzUser) permissionable;

						if (user.getAlias() == null) {
							output.append(user.getName());
						} else {
							output.append("&");
							output.append(user.getAlias());
						}

						output.append(" = ");
						output.append(rule.getAuthzPrivilege().getPrivilege());
						output.append(TEXT_NEW_LINE);
					} else {
						throw new RuntimeException("generator.error");
					}
				}

				output.append(TEXT_NEW_LINE);
				
				break;
			}	
		}
	}
}
