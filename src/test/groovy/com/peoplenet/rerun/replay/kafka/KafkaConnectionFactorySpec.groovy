package com.peoplenet.rerun.replay.kafka

import com.peoplenet.rerun.replay.exception.PublishException
import kafka.cluster.Broker
import kafka.javaapi.PartitionMetadata
import kafka.javaapi.TopicMetadata
import kafka.javaapi.TopicMetadataRequest
import kafka.javaapi.TopicMetadataResponse
import kafka.javaapi.consumer.SimpleConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import spock.lang.Specification

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

class KafkaConnectionFactorySpec extends Specification {

    void 'initializes correctly'() {
        given: 'a good config'
        BrokerAddress address = BrokerAddress.from('localhost:9092')
        KafkaConfiguration config = new KafkaConfiguration(
                bootstrapBrokers: [address.toString()],
                clientId: 'test')

        when: 'the factory is instantiated'
        KafkaConnectionFactory factory = new KafkaConnectionFactory(config)

        then:
        factory.bootstrapBrokers.size() == 1
        factory.bootstrapBrokers[0] == address
        factory.clientId == 'test'
        assert factory.producer

    }

   void 'clientid is set to local hostname if not specified'() {
       given: 'a config without clientid'
       KafkaConfiguration config = new KafkaConfiguration(
               bootstrapBrokers: ['localhost:9092']
       )

       when:
       KafkaConnectionFactory factory = new KafkaConnectionFactory(config)

       then:
       factory.clientId == InetAddress.localHost.hostName
   }

    void 'Publish message sends data'() {
        given: 'A mock producer'
        KafkaConnectionFactory factory = goodFactory()
        KafkaProducer<String, byte[]> mockProducer = Mock(KafkaProducer)
        factory.producer = mockProducer

        and: 'topic, partition and message'
        String topic = 'test'
        int partitionId = 1
        byte[] message = topic.bytes

        ProducerRecord capturedRecord
        when: 'the message is published'
        PublishMetadata pMeta = factory.publishMessage(topic, partitionId, message)

        then:
        1 * mockProducer.send({capturedRecord = it} as ProducerRecord) >> Mock(Future) {
            get() >> new RecordMetadata(new TopicPartition(topic, partitionId), 1l, 0l)
        }

        capturedRecord != null
        capturedRecord.partition() == partitionId
        capturedRecord.topic() == topic

        assert pMeta
        pMeta.offset == 1l
        pMeta.partition == partitionId
        pMeta.topic == topic
        0 * _
    }

    void 'Publish exception is thrown when publising fails'() {
        given: 'A mock producer'
        KafkaConnectionFactory factory = goodFactory()
        KafkaProducer<String, byte[]> mockProducer = Mock(KafkaProducer)
        factory.producer = mockProducer

        and: 'topic, partition and message'
        String topic = 'test'
        int partitionId = 1
        byte[] message = topic.bytes

        ProducerRecord capturedRecord
        when: 'the message is published'
        PublishMetadata pMeta = factory.publishMessage(topic, partitionId, message)

        then:
        1 * mockProducer.send({capturedRecord = it} as ProducerRecord) >> Mock(Future) {
            get() >> { throw new ExecutionException(new Exception('BOOM'))}
        }

        0 * _

        thrown PublishException
    }

    void 'Partition information is returned when finding leader'() {
        given:
        SimpleConsumer mockConsumer = Mock(SimpleConsumer)
        KafkaConnectionFactory factory = goodFactory(mockConsumer)
        String topic = 'test'
        int partition = 0
        TopicMetadataRequest tMetaReq

        when:
        PartitionInformation pInfo = factory.findLeader(topic, partition)

        then:
        1 * mockConsumer.send({tMetaReq = it}) >> Mock(TopicMetadataResponse) {
            topicsMetadata() >> [Mock(TopicMetadata) {
                partitionsMetadata() >> [Mock(PartitionMetadata) {
                    partitionId() >> 0
                    leader() >> Mock(Broker) {
                        host() >> 'host'
                        port() >> 9092
                    }
                    replicas() >> []
                }]
            }]
        }
        1 * mockConsumer.close()
        0 * _

        topic in tMetaReq.topics()
        pInfo != null
        pInfo.topic == topic
        pInfo.partitionId == partition
        pInfo.leader == BrokerAddress.from('host:9092')
    }


    void 'Partition exception after trying all brokers and no metadata returned'() {
        given: 'config with multiple brokers'
        KafkaConfiguration config = new KafkaConfiguration(bootstrapBrokers: ['localhost:9092', '127.0.0.1:9092'])

        and: 'a factory using a mock consumer'
        SimpleConsumer mockConsumer = Mock(SimpleConsumer)
        KafkaConnectionFactory factory = new KafkaConnectionFactoryMock(mockConsumer, config)

        and: 'a topic and partition id'
        String topic = 'test'
        int partition = 0

        when:
        PartitionInformation pInfo = factory.findLeader(topic, partition)

        then:
        2 * mockConsumer.send(_ as TopicMetadataRequest)
        2 * mockConsumer.close()
        0 * _

        PartitionNotFoundException ex = thrown PartitionNotFoundException

        ex.topic == topic
        ex.partition == partition

    }

    void 'Partition information is refreshed'() {
        given: 'config with multiple brokers'
        KafkaConfiguration config = new KafkaConfiguration(bootstrapBrokers: ['localhost:9092', '127.0.0.1:9092'])

        and: 'a factory using a mock consumer'
        SimpleConsumer mockConsumer = Mock(SimpleConsumer)
        KafkaConnectionFactory factory = new KafkaConnectionFactoryMock(mockConsumer, config)

        and: 'partition information'
        PartitionInformation oldPInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                replicas: [BrokerAddress.from('127.0.0.1:9092')],
                topic: 'test')

        when:
        PartitionInformation newPInfo = factory.refreshPartitionInformation(oldPInfo)

        then:
        1 * mockConsumer.send(_ as TopicMetadataRequest) >> Mock(TopicMetadataResponse) {
            topicsMetadata() >> [Mock(TopicMetadata) {
                partitionsMetadata() >> [Mock(PartitionMetadata) {
                    partitionId() >> 0
                    leader() >> Mock(Broker) {
                        host() >> 'host'
                        port() >> 9092
                    }
                    replicas() >> []
                }]
            }]
        }
        1 * mockConsumer.close()
        0 * _

        newPInfo != null
        newPInfo.leader == BrokerAddress.from('host:9092')
    }

    void 'Partition is information is refreshed to same leader'() {
        given: 'config with multiple brokers'
        KafkaConfiguration config = new KafkaConfiguration(bootstrapBrokers: ['localhost:9092', '127.0.0.1:9092'])

        and: 'a factory using a mock consumer'
        SimpleConsumer mockConsumer = Mock(SimpleConsumer)
        KafkaConnectionFactory factory = new KafkaConnectionFactoryMock(mockConsumer, config)

        and: 'partition information'
        PartitionInformation oldPInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                replicas: [BrokerAddress.from('127.0.0.1:9092')],
                topic: 'test')

        when:
        PartitionInformation newPInfo = factory.refreshPartitionInformation(oldPInfo)

        then:
        2 * mockConsumer.send(_ as TopicMetadataRequest) >> Mock(TopicMetadataResponse) {
            topicsMetadata() >> [Mock(TopicMetadata) {
                partitionsMetadata() >> [Mock(PartitionMetadata) {
                    partitionId() >> 0
                    leader() >> Mock(Broker) {
                        host() >> 'localhost'
                        port() >> 9092
                    }
                    replicas() >> []
                }]
            }]
        }
        2 * mockConsumer.close()
        0 * _

        newPInfo != null
        newPInfo.leader == BrokerAddress.from('localhost:9092')
    }

    private KafkaConnectionFactory goodFactory(SimpleConsumer mockConsumer = null) {
        KafkaConfiguration config = new KafkaConfiguration(
                bootstrapBrokers: ['localhost:9092'],
                clientId: 'test')

        return mockConsumer ? new KafkaConnectionFactoryMock(mockConsumer, config) :
                new KafkaConnectionFactory(config)
    }
}
