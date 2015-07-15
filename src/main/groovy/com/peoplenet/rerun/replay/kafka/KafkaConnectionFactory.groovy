package com.peoplenet.rerun.replay.kafka

import com.peoplenet.rerun.replay.exception.PublishException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import kafka.javaapi.*
import kafka.javaapi.consumer.SimpleConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.ExecutionException

/**
 * Creates connections to kafka for publishing and consuming.
 */
@Slf4j
@CompileStatic
@Component
class KafkaConnectionFactory {
    List<BrokerAddress> bootstrapBrokers
    KafkaProducer<String, byte[]> producer
    String clientId = "kafka-connection"

    @Autowired
    KafkaConnectionFactory(KafkaConfiguration config) {
        bootstrapBrokers = config.bootstrapBrokers.collect {
           return BrokerAddress.from(it)
        }

        if(!config.clientId) {
            try {
                clientId = InetAddress.localHost.hostName
            } catch (Exception e) {
                log.warn("Unable to resolve hostname for client id.")
            }
        } else {
            clientId = config.clientId
        }

        initializePublisher()
    }

    private void initializePublisher() {
        Properties props = new Properties()
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 'org.apache.kafka.common.serialization.StringSerializer')
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 'org.apache.kafka.common.serialization.ByteArraySerializer')
        props.put(ProducerConfig.CLIENT_ID_CONFIG, 'rerun')
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapBrokers.collect {"${it}"}.join(','))
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, 'snappy')

        producer = new KafkaProducer<>(props)
    }

    /**
     * Publish a single message to the topic, this is a synchronous call.
      */
    PublishMetadata publishMessage(String topic, int partitionId, byte[] message) {

        try {
            RecordMetadata meta = producer.send(new ProducerRecord<>(topic, partitionId, null, message)).get()

            return new PublishMetadata(
                    offset: meta.offset(),
                    partition: meta.partition(),
                    topic: meta.topic()
            )
        } catch (InterruptedException | ExecutionException e) {
            throw new PublishException("failed to publish to [topic: '${topic}', partition: '${partitionId}']", e)
        }

    }

    /**
     * Return partition metadata which includes the leader for a topic and partition.  Returns null if no metadata
     * can be found.
     *
     */
    PartitionInformation findLeader(String topic, int partition) {
        findLeader(bootstrapBrokers, topic, partition)
    }

    /**
     * Find the leader amongst the given bootstrap brokers. Returns null if no leader is found
     *
     */
    PartitionInformation findLeader(List<BrokerAddress> brokers, String topic, int partition)
        throws PartitionNotFoundException {

        if (!brokers) {
            throw new IllegalArgumentException('No brokers given to locate partition leader with!')
        }

        PartitionMetadata pMeta = brokers.findResult { BrokerAddress broker ->

            withSimpleConsumer(broker) { SimpleConsumer consumer ->
                TopicMetadataRequest req = new TopicMetadataRequest([topic])
                TopicMetadataResponse resp = consumer.send(req)
                List<TopicMetadata> metaData = resp?.topicsMetadata()

                metaData?.findResult { topicMeta ->
                    topicMeta.partitionsMetadata()?.find { partMeta ->
                        partMeta?.partitionId() == partition
                    }
                }
            }
        }

        if (pMeta) {
            new PartitionInformation(
                    leader: new BrokerAddress(pMeta.leader().host(), pMeta.leader().port()),
                    replicas: pMeta.replicas()?.collect { replica ->
                        new BrokerAddress(replica.host(), replica.port())
                    },
                    partitionId: pMeta.partitionId(),
                    topic: topic
            )
        } else {
            throw new PartitionNotFoundException(topic, partition)
        }
    }

    /**
     * Attempt to grab updated PartitionInformation for the given old partition information.  By default
     * 3 attempts is made to update the information. If, after all attempts are exhausted, the leader of the partition
     * cannot be found then null is returned.
     */
    PartitionInformation refreshPartitionInformation(PartitionInformation oldInfo) {
        //retry 3 times to find the leader, make this configurable?
        (0..2).findResult { int idx ->
            PartitionInformation info = findLeader(oldInfo.replicas, oldInfo.topic, oldInfo.partitionId)

            if ((info?.leader) &&
                (oldInfo.leader != info.leader || idx > 0)) {

                return info
            }

            sleep(1000)

        } as PartitionInformation
    }

    //Does not compile without the public modifier
    @SuppressWarnings('UnnecessaryPublicModifier')
    public <T> T withSimpleConsumer(BrokerAddress broker, Closure<T> closure) {
        T result = null
        SimpleConsumer consumer = null
        try {

            consumer = createSimpleConsumer(broker.host, broker.port, 100000, 64 * 1024, clientId)

            result = closure.call(consumer)

        } finally {
            if (consumer != null) {
                consumer.close()
            }
        }

        return result
    }

    //Does not compile without the public modifier
    @SuppressWarnings('UnnecessaryPublicModifier')
    public <T> T  withSimpleConsumer(Closure<T> closure) {
        bootstrapBrokers.findResult { BrokerAddress broker ->
            withSimpleConsumer(broker, closure)
        }
    }

    /**
     * Override for testing purposes.
     */
    protected SimpleConsumer createSimpleConsumer(
            String host, int port, int soTimeout, int bufferSize, String clientId) {

        return new SimpleConsumer(host, port, soTimeout, bufferSize, clientId)
    }
}
