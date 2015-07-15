package com.peoplenet.rerun.replay.service

class JobNotFoundException extends RuntimeException {
    String jobId

    JobNotFoundException(String jobId) {
        super("Job with id '${jobId}' could not be found")
        this.jobId = jobId
    }
}
