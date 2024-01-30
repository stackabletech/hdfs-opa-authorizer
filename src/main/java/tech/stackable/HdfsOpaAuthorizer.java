package tech.stackable;

import org.apache.hadoop.hdfs.server.namenode.INodeAttributeProvider;
import org.apache.hadoop.hdfs.server.namenode.INodeAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsOpaAuthorizer extends INodeAttributeProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HdfsOpaAuthorizer.class);

    @Override
    public void start() {
        LOG.info("Starting HdfsOpaAuthorizer");
    }

    @Override
    public void stop() {
        LOG.info("Stopping HdfsOpaAuthorizer");
    }

    @Override
    public INodeAttributes getAttributes(String[] strings, INodeAttributes iNodeAttributes) {
        // No special attributes needed
        return iNodeAttributes;
    }

    @Override
    public AccessControlEnforcer getExternalAccessControlEnforcer(AccessControlEnforcer defaultEnforcer) {
        return new HdfsOpaAccessControlEnforcer();
    }
}
