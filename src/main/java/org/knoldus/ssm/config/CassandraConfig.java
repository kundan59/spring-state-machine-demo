package org.knoldus.ssm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Cassandra configurations. Cassandra persisting the state.
 */
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;

    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoint;

    @Value("${spring.data.cassandra.port}")
    private int port;

    @NonNull
    @Override
    protected String getKeyspaceName() {
        return keyspace;
    }

    @NonNull
    @Override
    public String getContactPoints() {
        return contactPoint;
    }

    @NonNull
    @Override
    protected int getPort() {
        return port;
    }

    @NonNull
    @Override
    public String[] getEntityBasePackages() {
        return new String[] {"org.knoldus.ssm.domain"};
    }

    @NonNull
    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @NonNull
    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        return Collections.singletonList(CreateKeyspaceSpecification.createKeyspace(keyspace)
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication(1L));
    }

    @Override
    protected String getLocalDataCenter() {
        return "datacenter1";
    }

    @NonNull
    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return List.of(DropKeyspaceSpecification.dropKeyspace(keyspace));
    }
}
