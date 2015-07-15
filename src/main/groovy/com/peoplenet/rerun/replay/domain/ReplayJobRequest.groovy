package com.peoplenet.rerun.replay.domain

import com.peoplenet.rerun.replay.api.ReplayRequest

class ReplayJobRequest {
    String name
    String mediaType
    KafkaSource source
    HttpTarget target

    ReplayRequest toApi() {
        return new ReplayRequest(
                mediaType: mediaType,
                name: name,
                source: source.toApi(),
                target: target.toApi()
        )
    }

    static ReplayJobRequest fromApi(ReplayRequest api) {
        return new ReplayJobRequest(
                mediaType: api.mediaType,
                name: api.name,
                source: KafkaSource.fromApi(api.source),
                target: HttpTarget.fromApi(api.target)
        )
    }
}
