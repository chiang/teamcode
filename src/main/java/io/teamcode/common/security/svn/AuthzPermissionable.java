package io.teamcode.common.security.svn;

import java.util.List;

public interface AuthzPermissionable extends AuthzNamed {

	List<AuthzAccessRule> getAccessRules();
}
