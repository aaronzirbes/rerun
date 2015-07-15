package com.peoplenet.rerun.replay.kafka

import kafka.api.FetchRequest
import kafka.common.ErrorMapping
import kafka.javaapi.FetchResponse
import kafka.javaapi.consumer.SimpleConsumer
import kafka.javaapi.message.ByteBufferMessageSet
import kafka.message.Message
import kafka.message.MessageAndOffset
import org.apache.kafka.common.errors.OffsetOutOfRangeException
import spock.lang.Specification

class RangedStreamSpec extends Specification {
    SimpleConsumer mockConsumer = Mock(SimpleConsumer)
    KafkaConnectionFactoryMock mockFactory = new KafkaConnectionFactoryMock(mockConsumer, new KafkaConfiguration(
           bootstrapBrokers: ['localhost:9092']
    ))

    void 'offset range is consumed as expected'() {
        given: 'offset range and partition info'
        OffsetRange range = new OffsetRange(1, 5)
        PartitionInformation pInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                topic: 'test',
                replicas: [BrokerAddress.from('127.0.0.1:9092')]
        )

        and: 'a ranged stream'
        RangedStream stream = new RangedStream(mockFactory, range, pInfo)

        when:
        int msgCount = 0
        stream.beginStreaming {msgCount++}

        then:
        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> false
            messageSet('test', 0) >> Mock(ByteBufferMessageSet) {
                iterator() >> [
                        new MessageAndOffset(new Message('message'.bytes), 0),
                        new MessageAndOffset(new Message('message'.bytes), 1),
                        new MessageAndOffset(new Message('message'.bytes), 2),
                        new MessageAndOffset(new Message('message'.bytes), 3),
                        new MessageAndOffset(new Message('message'.bytes), 4),
                        new MessageAndOffset(new Message('message'.bytes), 5),
                        new MessageAndOffset(new Message('message'.bytes), 6),
                ].iterator()
            }
        }

        1 * mockConsumer.close()
        0 * _

        msgCount == 5
    }

    void 'offset range is consumed as expected with multiple fetches'() {
        given: 'offset range and partition info'
        OffsetRange range = new OffsetRange(0, 4)
        PartitionInformation pInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                topic: 'test',
                replicas: [BrokerAddress.from('127.0.0.1:9092')]
        )

        and: 'a ranged stream'
        RangedStream stream = new RangedStream(mockFactory, range, pInfo)
        byte[] msg = 'message'.bytes

        when:
        int msgCount = 0
        stream.beginStreaming {KafkaMessage m ->
            msgCount++
            assert new String(m.payload) == new String(msg)
        }

        then:
        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> false
            messageSet('test', 0) >> Mock(ByteBufferMessageSet) {
                iterator() >> [
                        new MessageAndOffset(new Message(msg), 0),
                        new MessageAndOffset(new Message(msg), 1),
                        new MessageAndOffset(new Message(msg), 2),
                ].iterator()
            }
        }

        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> false
            messageSet('test', 0) >> Mock(ByteBufferMessageSet) {
                iterator() >> [
                        new MessageAndOffset(new Message(msg), 3),
                        new MessageAndOffset(new Message(msg), 4),
                        new MessageAndOffset(new Message(msg), 5),
                ].iterator()
            }
        }

        1 * mockConsumer.close()
        0 * _

        msgCount == 5
    }


    void 'Error expected when offset is out of range'() {
        given: 'offset range and partition info'
        OffsetRange range = new OffsetRange(0, 4)
        PartitionInformation pInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                topic: 'test',
                replicas: [BrokerAddress.from('127.0.0.1:9092')]
        )

        and: 'a ranged stream'
        RangedStream stream = new RangedStream(mockFactory, range, pInfo)

        when:
        stream.beginStreaming {/*noop*/}

        then:
        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> true
            errorCode('test', 0) >> ErrorMapping.OffsetOutOfRangeCode()
        }

        1 * mockConsumer.close()
        0 * _

        thrown OffsetOutOfRangeException
    }


    void 'Error expected when leader is lost'() {
        given: 'offset range and partition info'
        OffsetRange range = new OffsetRange(0, 4)
        PartitionInformation pInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                topic: 'test',
                replicas: [BrokerAddress.from('127.0.0.1:9092')]
        )
        and: 'spy on the connection factory'
        KafkaConnectionFactoryMock myMockFactory = Spy(KafkaConnectionFactoryMock,
            constructorArgs: [mockConsumer, new KafkaConfiguration(bootstrapBrokers: ['localhost:9092'])]) {

           1 * refreshPartitionInformation(_) >> null
        }

        and: 'a ranged stream'
        RangedStream stream = new RangedStream(myMockFactory, range, pInfo)


        when:
        stream.beginStreaming {/*noop*/}

        then:
        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> true
            errorCode('test', 0) >> ErrorMapping.LeaderNotAvailableCode()
        }

        1 * mockConsumer.close()

        thrown LeaderMayNotBeLeaderException
    }

    void 'Recovers from lost leader'() {
        given: 'offset range and partition info'
        OffsetRange range = new OffsetRange(0, 0)
        PartitionInformation pInfo = new PartitionInformation(
                leader: BrokerAddress.from('localhost:9092'),
                partitionId: 0,
                topic: 'test',
                replicas: [BrokerAddress.from('127.0.0.1:9092')]
        )
        and: 'spy on the connection factory'
        KafkaConnectionFactoryMock myMockFactory = Spy(KafkaConnectionFactoryMock,
                constructorArgs: [mockConsumer, new KafkaConfiguration(bootstrapBrokers: ['localhost:9092'])]) {

            1 * refreshPartitionInformation(_) >> pInfo
        }

        and: 'a ranged stream'
        RangedStream stream = new RangedStream(myMockFactory, range, pInfo)

        when:
        int msgCount = 0
        stream.beginStreaming {msgCount++}

        then:
        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> true
            errorCode('test', 0) >> ErrorMapping.LeaderNotAvailableCode()
        }

        1 * mockConsumer.fetch(_ as FetchRequest) >> Mock(FetchResponse) {
            hasError() >> false
            messageSet('test', 0) >> Mock(ByteBufferMessageSet) {
                iterator() >> [
                        new MessageAndOffset(new Message('message'.bytes), 0),
                ].iterator()
            }
        }

        2 * mockConsumer.close()

        msgCount == 1
    }
}

