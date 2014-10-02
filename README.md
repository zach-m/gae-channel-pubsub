

## Pubsub implementation based on App Engine Java Channel API

This is a google app-engine application that provides a backend for a publish/subscribe service. By relying on the Google-managed Channel API infrastructure, it allows clients to either publish messages on a "message-board" (`topic`) or subscribe to get notified whenever such message is being published (or both, of course, as in a chat-root scenario). 

The code is written as a Java servlet, that basically provides 3 APIs:

1. `connect(clientId)` - required before making a subscription, returns a token
2. `subscribe(topic, token)` - subscribes for notifications on a certain topic
3. `publish(topic, msg)` - publishes a message on a given topic

At this time, only Javascript clients can register for notifications (this is a Channel API restriction). But in order to publish a message, any HTTP client will do.

_NOTE:_ the current implementation is based on an in-memory storage of the topics and subscribers (to keep things simple). The `PubsubPersister` interface can be implemented in your project to interface with your choice of persistence layer. 

------------------------------------------------------------

Requires [Apache Maven](http://maven.apache.org) 3.1 or greater, and JDK 7+ in order to run.

To build, run

    mvn package

To start the applocally, run the command.

    mvn appengine:devserver

