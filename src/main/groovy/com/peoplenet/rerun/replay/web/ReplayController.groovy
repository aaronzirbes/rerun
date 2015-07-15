package com.peoplenet.rerun.replay.web

import com.peoplenet.rerun.replay.api.ReplayStatus
import com.peoplenet.rerun.replay.domain.ReplayJobRequest
import com.peoplenet.rerun.replay.service.ReplayService
import com.peoplenet.rerun.replay.api.ReplayJobSummary
import com.peoplenet.rerun.replay.domain.ReplayJob
import com.peoplenet.rerun.replay.api.ReplayList
import com.peoplenet.rerun.replay.api.ReplayRequest
import com.peoplenet.rerun.replay.api.ReplayResponse
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@CompileStatic
@RestController
@RequestMapping('/replay')
@Slf4j
class ReplayController {
    private ReplayService replayService


    @Autowired
    ReplayController(ReplayService replayService) {
       this.replayService = replayService
    }

    @RequestMapping(method = [RequestMethod.POST],
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    ReplayResponse requestReplay(@RequestBody @Valid ReplayRequest replayRequest) {
        String id  = replayService.submitReplayRequest(
                ReplayJobRequest.fromApi(replayRequest))

        new ReplayResponse(replayId: id)
    }

    @RequestMapping(method = [RequestMethod.GET],
                    produces = MediaType.APPLICATION_JSON_VALUE)
    ReplayList fetchReplayJobs() {
        List<ReplayJob> jobs = replayService.fetchReplayJobs()
        ReplayList replays = new ReplayList(replayJobs: jobs.collect { new ReplayJobSummary(it)})

        return replays
    }

    @RequestMapping(method = [RequestMethod.GET],
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    value = "{id}")
    ReplayRequest fetchReplayRequest(@PathVariable String id) {
        ReplayJobRequest jobRequest = replayService.fetchReplayJob(id).replayRequest
        return jobRequest.toApi()
    }

    @RequestMapping(method = [RequestMethod.GET],
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    value = "{id}/status")
    ReplayStatus fetchReplayStatus(@PathVariable String id) {
        ReplayJob job  = replayService.fetchReplayJob(id)

        ReplayStatus status = new ReplayStatus(
                errorMessage: job.errorMsg,
                errorDescription: job.errorDescription,
                percentComplete: job.percentComplete,
                replayId: job.id,
                status: job.replayState?.name(),
                submitTime: job.submitTime
        )

        return status
    }

}
