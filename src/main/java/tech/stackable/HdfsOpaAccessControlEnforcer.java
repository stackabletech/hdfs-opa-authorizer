package tech.stackable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.hdfs.server.blockmanagement.DatanodeDescriptor;
import org.apache.hadoop.hdfs.server.blockmanagement.DatanodeStorageInfo;
import org.apache.hadoop.hdfs.server.namenode.INode;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributeProvider;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributes;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HdfsOpaAccessControlEnforcer implements INodeAttributeProvider.AccessControlEnforcer {
    public static final String OPA_POLICY_URL_PROP = "hadoop.security.authorization.opa.policy.url";

    private static final Logger LOG = LoggerFactory.getLogger(HdfsOpaAccessControlEnforcer.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper json;
    private final URI opaPolicyUrl;

    public HdfsOpaAccessControlEnforcer() {
        LOG.info("Starting HdfsOpaAccessControlEnforcer");

        // FIXME: Remove this performance bottleneck
        Configuration configuration = new Configuration();

        String opaPolicyUrl = configuration.get(OPA_POLICY_URL_PROP);
        if (opaPolicyUrl == null) {
            throw new RuntimeException("Config \"" + OPA_POLICY_URL_PROP + "\" missing");
        }

        try {
            this.opaPolicyUrl = URI.create(opaPolicyUrl);
        } catch (Exception e) {
            throw new OpaConfigException.UriInvalid(opaPolicyUrl, e);
        }

        this.json = new ObjectMapper()
                // OPA server can send other fields, such as `decision_id`` when enabling decision logs
                // We could add all the fields we *currently* know, but it's more future-proof to ignore any unknown fields
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Previously we were getting
                // Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.apache.hadoop.hdfs.util.EnumCounters and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: tech.stackable.HdfsOpaAccessControlEnforcer$ContextWrapper["inodeAttrs"]->org.apache.hadoop.hdfs.server.namenode.INodeDirectory[0]->org.apache.hadoop.hdfs.server.namenode.INodeDirectory["features"]->org.apache.hadoop.hdfs.server.namenode.DirectoryWithQuotaFeature[0]->org.apache.hadoop.hdfs.server.namenode.DirectoryWithQuotaFeature["spaceConsumed"]->org.apache.hadoop.hdfs.server.namenode.QuotaCounts["typeSpaces"])
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // Only include the needed fields. HDFS has many classes with even more circular reference to remove
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY)
                // We need to remove some circular pointers (e.g. root -> children[0] -> parent -> root)
                // Otherwise we get com.fasterxml.jackson.databind.JsonMappingException: Infinite recursion (StackOverflowError)
                .addMixIn(DatanodeDescriptor.class, DatanodeDescriptorMixin.class);

        LOG.info("Started HdfsOpaAccessControlEnforcer");
    }

    private static class OpaQueryResult {
        // Boxed Boolean to detect not-present vs explicitly false
        public Boolean result;
    }

    @Override
    public void checkPermission(String s, String s1, UserGroupInformation userGroupInformation, INodeAttributes[] iNodeAttributes, INode[] iNodes, byte[][] bytes, int i, String s2, int i1, boolean b, FsAction fsAction, FsAction fsAction1, FsAction fsAction2, FsAction fsAction3, boolean b1) throws AccessControlException {
        // TODO
        throw new AccessControlException("The HdfsOpaAccessControlEnforcer does not implement the old checkPermission API.");
    }

    @Override
    public void checkPermissionWithContext(INodeAttributeProvider.AuthorizationContext authzContext) throws AccessControlException {
        OpaQuery query = new OpaQuery(authzContext);

        try {
            LOG.info("checkPermissionWithContext called to Url {} with {}", opaPolicyUrl, json.writeValueAsString(query));
        } catch (JsonProcessingException ignored) {
        }

        byte[] queryJson;
        try {
            queryJson = json.writeValueAsBytes(query);
        } catch (JsonProcessingException e) {
            throw new OpaQueryException.SerializeFailed(e);
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(
                    HttpRequest.newBuilder(opaPolicyUrl).header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofByteArray(queryJson)).build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new OpaQueryException.QueryFailed(e);
        }

        switch (response.statusCode()) {
            case 200:
                break;
            case 404:
                throw new OpaQueryException.PolicyNotFound(opaPolicyUrl.toString());
            default:
                throw new OpaQueryException.OpaServerError(opaPolicyUrl.toString(), response);
        }

        OpaQueryResult result;
        try {
            result = json.readValue(response.body(), OpaQueryResult.class);
        } catch (Exception e) {
            throw new OpaQueryException.DeserializeFailed(e);
        }

        if (!result.result) {
            throw new AccessControlException("OPA denied the request.");
        }
    }

    private static class OpaQuery {
        public java.lang.String fsOwner;
        public java.lang.String supergroup;
        public OpaQueryUgi callerUgi;
        public org.apache.hadoop.hdfs.server.namenode.INodeAttributes[] inodeAttrs;
        public org.apache.hadoop.hdfs.server.namenode.INode[] inodes;
        public byte[][] pathByNameArr;
        public int snapshotId;
        public java.lang.String path;
        public int ancestorIndex;
        public boolean doCheckOwner;
        public org.apache.hadoop.fs.permission.FsAction ancestorAccess;
        public org.apache.hadoop.fs.permission.FsAction parentAccess;
        public org.apache.hadoop.fs.permission.FsAction access;
        public org.apache.hadoop.fs.permission.FsAction subAccess;
        public boolean ignoreEmptyDir;
        public java.lang.String operationName;
        public org.apache.hadoop.ipc.CallerContext callerContext;

        public OpaQuery(INodeAttributeProvider.AuthorizationContext context) {
            this.fsOwner = context.getFsOwner();
            this.supergroup = context.getSupergroup();
            this.callerUgi = new OpaQueryUgi(context.getCallerUgi());
            this.inodeAttrs = context.getInodeAttrs();
            this.inodes = context.getInodes();
            this.pathByNameArr = context.getPathByNameArr();
            this.snapshotId = context.getSnapshotId();
            this.path = context.getPath();
            this.ancestorIndex = context.getAncestorIndex();
            this.doCheckOwner = context.isDoCheckOwner();
            this.ancestorAccess = context.getAncestorAccess();
            this.parentAccess = context.getParentAccess();
            this.access = context.getAccess();
            this.subAccess = context.getSubAccess();
            this.ignoreEmptyDir = context.isIgnoreEmptyDir();
            this.operationName = context.getOperationName();
            this.callerContext = context.getCallerContext();
        }
    }

    private static class OpaQueryUgi {
        public UserGroupInformation realUser;
        public String userName;
        public String shortUserName;

        public String primaryGroup;
        public List<String> groups;

        public UserGroupInformation.AuthenticationMethod authenticationMethod;
        public UserGroupInformation.AuthenticationMethod realAuthenticationMethod;

        public OpaQueryUgi(UserGroupInformation ugi) {
            this.realUser = ugi.getRealUser();
            this.userName = ugi.getUserName();
            this.shortUserName = ugi.getShortUserName();
            try {
                this.primaryGroup = ugi.getPrimaryGroupName();
            } catch (IOException e) {
                this.primaryGroup = null;
            }
            this.groups = ugi.getGroups();
            this.authenticationMethod = ugi.getAuthenticationMethod();
            this.realAuthenticationMethod = ugi.getRealAuthenticationMethod();
        }
    }

    private abstract static class DatanodeDescriptorMixin {
        @JsonIgnore
        abstract INode getParent();
        @JsonIgnore
        abstract DatanodeStorageInfo[] getStorageInfos();
    }
}
