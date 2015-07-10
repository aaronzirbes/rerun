package com.peoplenet.rerun.replay.kafka

interface MessageStream {
   /**
    * Beginning ingesting the stream of data and invoke the callback with each message.
    *
    * Optionally return a StreamControl status of STOP to interrupt the stream.
    *
    * @param closure
    */
   void beginStreaming(Closure closure)
}
