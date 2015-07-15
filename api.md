# Create Replay Job

Creates a new replay job for a given topic, partition and offset range.

```POST /replay```

_Message_

```
{
    "name":"descriptive name",
    "source" : {
        "topic": "topic-string",
        "partition": "partitionId",
        "startingOffset": 200,  //long value
        "endingOffset": 205 //long value    
    },
    "target": {
        "url": "http://somehost/some/path",
        "method": "POST",
        "headers": {
            "x-header": "x-value"
        },
        "queryParams": { //allow user to specify the  query parameters to add to each request. 
            "topic":  "${messageKey}",  // messageKey and topicName will be replaced by the system.
            "another": "${topicName}"
        }
    }
}
```

_Returns_

```
{
    "replayId": "UUID string"
}
```

# Get Status of Replay Job

Get the status of a replay job.

``` GET /replay/{replayId}/status ```

_Returns_

```
{
    "replayId": "UUID string",
    "status" : "running|complete|error", //one of running, complete, error
    "errorDescription": "my be empty", //optionally filled when in error status
    "startingOffset": 200,
    "endingOffset": 205,
    "currentOffset": 203  // can be used to show percent complete.
}
```


# List All Known Replay Jobs

Returns a list of all known replay jobs sorted in time descending (oldest first). No paging at this time.

``` GET /replay ```

_Returns_

```
{
    "replayJobs": [
        {
            "replayId": "UUID string",
            "status" : "running|complete|error",
            "name": "descriptive name",
            "submitTime": "ISO 8601 format, GMT"
        } 
    ]
}
```

# Get a Replay Job

Return the configuraiton for a replay job.

``` GET /replay/{replayId} ```

_Returns_

```
{
    "name":"descriptive name",
    "topic": "topic-string",
    "partition": "partitionId",
    "startingOffset": 200,  //long value
    "endingOffset": 205, //long value
    "batch": false,
    "httpTarget": {
        "url": "http://somehost/some/path",
        "method": "POST"
        "queryParams": { //allow user to specify the  query parameters to add to each request. 
            "topic":  "${messageKey}",  // messageKey and topicName will be replaced by the system.
            "another": "${topicName}"
        }
    },
    "submitTime": "ISO 8601 format, GMT"
}
```
        