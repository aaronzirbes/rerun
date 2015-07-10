package com.peoplenet.rerun.replay.kafka

/**
 * Indicates that the chosen partition leader may not be a leader any longer.
 */
class LeaderMayNotBeLeaderException extends RuntimeException {
    BrokerAddress oldLeader
}
