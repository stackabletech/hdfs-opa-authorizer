package tech.stackable;

import org.apache.hadoop.hdfs.server.namenode.INode;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributeProvider;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributes;

import java.security.AccessControlException;

/**
 * Hello world!
 *
 */
public class HdfsOpaAuthorizer implements INodeAttributeProvider.AccessControlEnforcer
{

    public void checkPermission(String s, String s1, org.apache.hadoop.security.UserGroupInformation userGroupInformation, INodeAttributes[] iNodeAttributes, INode[] iNodes, byte[][] bytes, int i, String s2, int i1, boolean b, org.apache.hadoop.fs.permission.FsAction fsAction, org.apache.hadoop.fs.permission.FsAction fsAction1, org.apache.hadoop.fs.permission.FsAction fsAction2, org.apache.hadoop.fs.permission.FsAction fsAction3, boolean b1) throws AccessControlException {

    }

    public void checkPermissionWithContext(INodeAttributeProvider.AuthorizationContext authzContext) throws AccessControlException {

    }
}

