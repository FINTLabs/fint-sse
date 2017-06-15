# FINT SSE client

[![Build Status](https://travis-ci.org/FINTlibs/fint-sse.svg?branch=master)](https://travis-ci.org/FINTlibs/fint-sse)
[![Coverage Status](https://coveralls.io/repos/github/FINTlibs/fint-sse/badge.svg?branch=master)](https://coveralls.io/github/FINTlibs/fint-sse?branch=master)

Built on [jersey](https://jersey.github.io/documentation/latest/sse.html).

## Installation

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-sse:0.0.2')
```

## Usage

Create new instance. If the SseUrl contains a placeholder at the end a UUID is generated and added before registering the client.
By default concurrent connections is also enabled, where 2 sse connections are registered on connect. This can be disabled by `disableConcurrentConnections()`.
```java
new FintSse("http://localhost:8080/sse/%s");
```

It is possible to specifcy the SSE thread interval in milliseconds, this is 10 minutes by default
```java
new FintSse("http://localhost:8080/sse/%s", TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES));
```

Create an event listener
```java
public class MyEventListener extends AbstractEventListener {
    @Override
    public void onEvent(Event event) {
        ...
    }
}
```

Connect to the SSE server
```java
fintSse.connect(myEventListener);
```

Or connect event listener to a specific action
```java
fintSse.connect(myEventListener, "GET_ALL_EMPLOYEES");
```

It is also possible to send in headers that will be sent with the SSE request
```java
Map<String, String> headeres = ImmutableMap.of("x-org-id", "mock.no");
fintSse.connect(listener, headers);
```

Verify connection, this will return true if the connection is open.  
If it is closed `FintSse` will try to reconnect and return false.
```java
boolean connected = fintSse.verifyConnection();
```

Close connection
```java
fintSse.close();
```

By default the client will run two simultaneous SSE connections, this can be disabled
```java
fintSse.disableConcurrentConnections();
```