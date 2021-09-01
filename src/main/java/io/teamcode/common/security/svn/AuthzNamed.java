package io.teamcode.common.security.svn;

import java.io.Serializable;

public interface AuthzNamed extends Comparable<AuthzNamed>, Serializable {

    String getName();

}
