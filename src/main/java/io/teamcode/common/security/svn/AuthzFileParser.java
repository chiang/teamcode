package io.teamcode.common.security.svn;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class AuthzFileParser {

	private enum State {
		STATE_PROCESS_ALIASES, STATE_PROCESS_GROUPS, STATE_PROCESS_RULES, STATE_PROCESS_SERVER_RULES, STATE_START
	}

	/** Logger handle. */
	private static final Logger logger = LoggerFactory
			.getLogger(AuthzFileParser.class);;

	/**
	 * Group currently being processed.
	 */
	private AuthzGroup currentGroup = null;

	/**
	 * Path currently being processed.
	 */
	private AuthzPath currentPath = null;

	/**
	 * Current parser state.
	 */
	private State currentState = null;

	public Authz parse(final BufferedReader input) {
		final Authz authz = AuthzFactory.getInstance().create();
		int lineNumber = 1;

		currentState = State.STATE_START;

		try {
			String line = input.readLine();

			while (line != null) {
				parseLine(authz, lineNumber, line);

				line = input.readLine();
				lineNumber++;
			}
		} catch (final IOException e) {
			// throw RuntimeException.generateException(lineNumber,
			// "parser.error");

			e.printStackTrace();

			throw new RuntimeException("parser.error", e);
		} catch (final Exception e) {
			// throw RuntimeException.generateException(lineNumber,
			// "parser.error");

			e.printStackTrace();

			throw new RuntimeException("parser.error", e);
		}

		return authz;
	}

	public Authz parse(final File file) {
		Authz document = null;
		BufferedReader input = null;

		validateReadable(file);

		try {
			input = new BufferedReader(new FileReader(file));
			document = parse(input);
		} catch (final FileNotFoundException e) {
			// throw RuntimeException.generateException(lineNumber,
			// "parser.filenotfound");

			e.printStackTrace();

			throw new RuntimeException("parser.filenotfound");
		} catch (final Exception e) {
			// throw RuntimeException.generateException(lineNumber,
			// "parser.error");

			e.printStackTrace();

			throw new RuntimeException("parser.error");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (final IOException ioe) {
					// TODO Log something
				}
			}
		}

		return document;
	}

	public Authz parse(final InputStream inputStream) {
		Authz document = null;
		BufferedReader input = null;

		try {
			input = new BufferedReader(new InputStreamReader(inputStream));

			document = parse(input);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (final IOException ioe) {
					// TODO Log something
				}
			}
		}

		return document;
	}

	private void parseAlias(final Authz document, final int lineNumber,
			final String line) throws AuthzException {
		final int index = line.indexOf('=');

		if (index == -1) {
			// Invalid syntax
			// throw RuntimeException.generateException(lineNumber,
			// "parser.syntaxerror.invalidaliasdefinition");
			throw new RuntimeException(
					"parser.syntaxerror.invalidaliasdefinition");
		}

		final String alias = line.substring(0, index).trim();
		final String name = line.substring(index + 1).trim();

		// FIXME document.createUser(name, alias);
	}

	private void parseGroup(final Authz authz, final int lineNumber,
			final String line) throws AuthzException {
		final int index = line.indexOf('=');
		logger.error("#{} 라인에서 오류가 발생햇습니다.", lineNumber);

		if (index == -1) {
			// Invalid syntax
			// throw RuntimeException.generateException(lineNumber,
			// "parser.syntaxerror.invalidgroupdefinition");

			throw new RuntimeException(
					"parser.syntaxerror.invalidgroupdefinition");
		}

		if (currentGroup != null) {
			// Invalid syntax
			// throw RuntimeException.generateException(lineNumber,
			// "parser.syntaxerror.invalidgroupdefinition");

			throw new RuntimeException(
					"parser.syntaxerror.invalidgroupdefinition");
		}

		// New Group
		final String name = line.substring(0, index).trim();
		final String memberListString = line.substring(index + 1).trim();
		final List<AuthzGroupMember> members = parseGroupMembers(authz,
				memberListString);

		AuthzGroup group = authz.getGroupWithName(name);	

		if (group == null) {
			group = authz.createGroup(name);
			authz.addGroupMembers(group, members);
		} else {
			if (!group.getMembers().isEmpty()) {
				// Existing group already has members. This is likely a
				// duplicate group definition
				// throw RuntimeException.generateException(lineNumber,
				// "parser.syntaxerror.duplicategroup", name);

				throw new RuntimeException("parser.syntaxerror.duplicategroup");
			}

			authz.addGroupMembers(group, members);
		}

		// Keep group for next line if there are more lines to process
		final String slimLine = line.trim();

		if (slimLine.charAt(slimLine.length() - 1) == ',') {
			currentGroup = group;
		}
	}

	private void parseGroupAccessRule(final Authz document,
			final int lineNumber, final String line) throws AuthzException {
		// Group Access
		final int index = line.indexOf('=');

		if (index == -1) {
			// Invalid syntax
			throw new RuntimeException();
		}

		final String groupName = line.substring(1, index).trim();
		final String level = line.substring(index + 1).trim();

		final AuthzGroup group = document.getGroupWithName(groupName);

		if (group == null) {
		    logger.error("서브버전 사용자 그룹 '{}' 을 찾을 수 없습니다. 줄 번호 #{} 에서 오류.", groupName, lineNumber);
			// throw RuntimeException.generateException(lineNumber,
			// "parser.syntaxerror.undefinedgroup", group);

			throw new RuntimeException("parser.syntaxerror.undefinedgroup");
		}

		final AuthzPrivilege accessLevel = AuthzPrivilege.getByCode(level);

		document.createAccessRule(currentPath, group, accessLevel);
	}

	private List<AuthzGroupMember> parseGroupMembers(final Authz authz,
			final String memberListString) throws AuthzException {
		final StringTokenizer memberListStringTokens = new StringTokenizer(
				memberListString, " ,");
		final int memberCount = memberListStringTokens.countTokens();
		final List<AuthzGroupMember> members = new ArrayList<AuthzGroupMember>();

		for (int i = 0; i < memberCount; i++) {
			final String memberName = memberListStringTokens.nextToken();

			if (memberName.charAt(0) == '@') {
				final String memberGroupName = memberName.substring(1,
						memberName.length());
				final AuthzGroup existingGroup = authz
						.getGroupWithName(memberGroupName);

				if (existingGroup == null) {
					members.add(authz.createGroup(memberGroupName));
				} else {
					members.add(existingGroup);
				}
			} else if (memberName.charAt(0) == '&') {
				final String memberAliasName = memberName.substring(1,
						memberName.length());

				//FIXME Alias는 차후 버전에서.
				//members.add(document.getUserWithAlias(memberAliasName));
			} else {				
				AuthzUser user = authz.getUserWithName(memberName);

				if (user == null) {
					user = authz.createUser(memberName, null);
				}

				members.add(user);
			}
		}

		return members;
	}

	private void parseGroupWrappedLine(final Authz authz, final int lineNumber,
			final String line) throws AuthzException {
		final List<AuthzGroupMember> members = parseGroupMembers(authz, line);

		authz.addGroupMembers(currentGroup, members);

		// Keep group for next line if there are more lines to process
		final String slimLine = line.trim();

		if (slimLine.charAt(slimLine.length() - 1) != ',') {
			currentGroup = null;
		}
	}

	/**
	 * Parses a single line in the authz file.
	 * 
	 * @param document
	 *            Document to be updated with values parsed.
	 * @param lineNumber
	 *            Number of the line being processed.
	 * @param line
	 *            Content of the line.
	 * @throws RuntimeException
	 * @throws RuntimeException
	 */
	private void parseLine(final Authz document, final int lineNumber,
			String line) throws AuthzException {
		// Process non-blank lines
		if (StringUtils.isBlank(line)) {
			if (currentState == State.STATE_PROCESS_GROUPS
					&& currentGroup != null) {
				// Invalid syntax
				// throw RuntimeException.generateException(lineNumber,
				// "parser.syntaxerror.invalidgroupdefinition");

				throw new RuntimeException(
						"parser.syntaxerror.invalidgroupdefinition");
			}

			return;
		}

		// Trim the line and replace all "tab" characters with a space
		line = StringUtils.trim(line).replaceAll("\\t", " ");

		switch (line.charAt(0)) {
		case '#':
			// Ignore comments
			break;

		case '[':
			// Parse section start
			if (line.equals("[aliases]")) {
				if (currentState != State.STATE_START) {
					// throw RuntimeException.generateException(lineNumber,
					// "parser.syntaxerror.multiplealiassection");

					throw new RuntimeException(
							"parser.syntaxerror.multiplealiassection");
				}

				currentState = State.STATE_PROCESS_ALIASES;
			} else if (line.equals("[groups]")) {
				if (currentState != State.STATE_START
						&& currentState != State.STATE_PROCESS_ALIASES) {
					// throw RuntimeException.generateException(lineNumber,
					// "parser.syntaxerror.multiplegroupsection");

					throw new RuntimeException(
							"parser.syntaxerror.multiplegroupsection");
				}

				currentState = State.STATE_PROCESS_GROUPS;
			} else {
				parsePath(document, lineNumber, line);
			}

			break;

		case '@':
			if (currentState == State.STATE_PROCESS_RULES
					|| currentState == State.STATE_PROCESS_SERVER_RULES) {
				// Group Access
				parseGroupAccessRule(document, lineNumber, line);
			} else {
				// throw RuntimeException.generateException(lineNumber,
				// "parser.syntaxerror.invalidgrouprule");
			}

			break;

		case ' ':
			// Group, continued
			parseGroupWrappedLine(document, lineNumber, line);

			break;

		default:
			if (currentState == State.STATE_PROCESS_ALIASES) {
				parseAlias(document, lineNumber, line);
			} else if (currentState == State.STATE_PROCESS_GROUPS) {
				parseGroup(document, lineNumber, line);
			} else if (currentState == State.STATE_PROCESS_RULES
					|| currentState == State.STATE_PROCESS_SERVER_RULES) {
				parseUserAccessRule(document, lineNumber, line,
						currentPath.getRepository());
			}

			break;
		}
	}

	private void parsePath(final Authz authz, final int lineNumber,
			final String line) throws AuthzException {
		final int index = line.indexOf(':');
		AuthzRepository repository = null;
		String pathString = null;

		if (index == -1) {
			// Server level access (e.g. [/path])
			currentState = State.STATE_PROCESS_SERVER_RULES;

			pathString = line.substring(1, line.length() - 1).trim();
		} else if (index >= 0) {
			// Repository level access (e.g. [repository:/path])
			currentState = State.STATE_PROCESS_RULES;

			final String repositoryName = line.substring(1, index).trim();

			repository = authz.getRepositoryWithName(repositoryName);

			if (repository == null) {
				repository = authz.createRepository(repositoryName);
			}

			pathString = line.substring(index + 1, line.length() - 1).trim();
		} else {
			// throw RuntimeException.generateException(lineNumber,
			// "parser.syntaxerror.invalidpath");

			throw new RuntimeException("parser.syntaxerror.invalidpath");
		}

		if (authz.doesPathExist(repository, pathString)) {
			throw new RuntimeException(
					"parser.syntaxerror.duplicatepathrepository");
		}

		// [/]인 경우를 체크한다.
		if (this.currentState.equals(State.STATE_PROCESS_SERVER_RULES)) {
			currentPath = authz.createPath(null, pathString);
		}
		else {
			currentPath = authz.createPath(repository, pathString);
		}
	}

	private void parseUserAccessRule(final Authz authz, final int lineNumber,
			final String line, final AuthzRepository authzRepository)
			throws AuthzException {
		final int index = line.indexOf('=');

		if (index == -1) {
			// Invalid syntax
			throw new RuntimeException();
		}

		final String userName = line.substring(0, index).trim();
		final String level = line.substring(index + 1).trim();

		AuthzUser user = authz.getUserWithName(userName);

		if (user == null) {	
			user = authz.createUser(userName, null);
		}

		final AuthzPrivilege accessLevel = AuthzPrivilege.getByCode(level);

		authz.createAccessRule(currentPath, user, accessLevel);
	}

	/**
	 * Validates whether the supplied file exists and is readable.
	 * 
	 * @param file
	 *            File to be validated.
	 */
	public void validateReadable(final File file) {
		if (!file.exists()) {
			throw new RuntimeException("parser.filenotfound");
		}

		if (!file.canRead()) {
			throw new RuntimeException("parser.unreadablefile");
		}
	}
}
