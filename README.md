# FINT SSE client

[![Build Status](https://travis-ci.org/FINTlibs/fint-sse.svg?branch=master)](https://travis-ci.org/FINTlibs/fint-sse)
[![Coverage Status](https://coveralls.io/repos/github/FINTlibs/fint-sse/badge.svg?branch=master)](https://coveralls.io/github/FINTlibs/fint-sse?branch=master)

Built on [jersey](https://jersey.github.io/documentation/latest/sse.html).

## Installation

```groovy
compile('no.fint:fint-sse:0.0.1')
```

## Usage

Create new instance
```java
FintSse fintSse = new FintSse("http://localhost:8080/sse/123");
```

Create an event listener
```java
public class MyEventListener implements EventListener {
    @Override
    public void onEvent(InboundEvent inboundEvent) {
        ...
    }
}
```

Connect to the SSE server
```java
fintSse.connect(myEventListener);
```

It is also possible to send in headers that will be sent with the SSE request
```java
Map<String, String> headeres = ImmutableMap.of("x-org-id", "mock.no");
fintSse.connect(listener, headers);
```

Verify connection, this will return true if the connection is open.  
If it is closed `FintSse´will try to reconnect and return false.
```java
boolean connected = fintSse.verifyConnection();
```

Close connection
```java
fintSse.close();
```