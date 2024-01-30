package tech.stackable;

public abstract class OpaConfigException extends RuntimeException {
    private static final long serialVersionUID = 2627367174713287956L;

    public OpaConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class UriRequired extends OpaConfigException {
        private static final long serialVersionUID = 799187669826407192L;

        public UriRequired() {
            super("No Open Policy Agent URI provided (must be set in the configuration \""
                    + HdfsOpaAccessControlEnforcer.OPA_POLICY_URL_PROP + "\")", null);
        }
    }

    public static final class UriInvalid extends OpaConfigException {
        private static final long serialVersionUID = 2753800944632029653L;

        public UriInvalid(String uri, Throwable cause) {
            super("Open Policy Agent URI is invalid (see configuration property \""
                    + HdfsOpaAccessControlEnforcer.OPA_POLICY_URL_PROP + "\"): " + uri, cause);
        }
    }
}
