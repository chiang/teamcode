package io.teamcode.common.security;

public enum HtPasswdVariant {
	ALG_APSHA("ALG_APSHA"),
    ALG_APMD5("ALG_APMD5"),
    ALG_CRYPT("ALG_CRYPT");

    private String digestType;

    HtPasswdVariant(String digestType) {
        this.digestType = digestType;
    }

    public String getDigestType() {
        return digestType;
    }

    public static HtPasswdVariant find(final String digestType) {
    if (digestType != null) {
      for (HtPasswdVariant currHtpasswd : HtPasswdVariant.values()) {
        if (digestType.equalsIgnoreCase(currHtpasswd.getDigestType())) {
          return currHtpasswd;
        }
      }
    }
    return null;
  }
}
