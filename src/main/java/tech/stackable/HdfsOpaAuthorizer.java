package tech.stackable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.hdfs.server.namenode.INode;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributeProvider;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributes;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class HdfsOpaAuthorizer extends INodeAttributeProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsOpaAuthorizer.class);
    public void start() {

    }

    @Override
    public AccessControlEnforcer getExternalAccessControlEnforcer(AccessControlEnforcer defaultEnforcer) {
        return new OpaAccessControlEnforcer();
    }

    public void stop() {

    }

    public INodeAttributes getAttributes(String[] strings, INodeAttributes iNodeAttributes) {
        return iNodeAttributes;
    }

    public class OpaAccessControlEnforcer implements AccessControlEnforcer {
        private final Logger LOG = LoggerFactory.getLogger(OpaAccessControlEnforcer.class);
        private ObjectMapper mapper;
        private PrintWriter writer;

        public OpaAccessControlEnforcer() {
            LOG.warn("Initializing enforcer..");
            this.mapper = new ObjectMapper();
            try {
                LOG.warn("building writer..");
                this.writer = new PrintWriter("/tmp/authz.log", "UTF-8");
                this.writer.println("Initialized successfully!\n");
                LOG.warn("done building writer!");
                this.writer.println("done building writer");
                PrintWriter test = new PrintWriter("/tmp/test.out");
            } catch (FileNotFoundException e) {
                this.writer = null;
            } catch (UnsupportedEncodingException e) {
                this.writer = null;
            }
            LOG.warn("initialization successful!");
            this.writer.flush();
        }

        public void checkPermission(String s, String s1, UserGroupInformation userGroupInformation, INodeAttributes[] iNodeAttributes, INode[] iNodes, byte[][] bytes, int i, String s2, int i1, boolean b, FsAction fsAction, FsAction fsAction1, FsAction fsAction2, FsAction fsAction3, boolean b1) throws AccessControlException {
            this.writer.println("Got old style request\n");
            if (this.writer != null) {
                this.writer.println(s + " " + s1 + " " + iNodes);
            }
            this.writer.println("Done processing!\n");
            this.writer.flush();
        }

        public void checkPermissionWithContext(AuthorizationContext authzContext) throws AccessControlException {
            ContextWrapper ctx = new ContextWrapper(authzContext);
            this.writer.println("Got request\n");
            if (this.writer != null) {
                try {
                    mapper.writeValue(writer, ctx);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            this.writer.println("Done processing!\n");
            this.writer.flush();
        }

        public void stop() {
            this.writer.close();
        }
    }
    
    private class ContextWrapper {
        public java.lang.String fsOwner;
        public java.lang.String supergroup;
        //public org.apache.hadoop.security.UserGroupInformation callerUgi;
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

        public ContextWrapper(AuthorizationContext context) {
            this.fsOwner = context.getFsOwner();
            this.supergroup = context.getSupergroup();
            //this.callerUgi = context.getCallerUgi();
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
}

