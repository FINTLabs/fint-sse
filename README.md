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

compile('no.fint:fint-sse:0.0.11')
```

## Usage

### Create new instance.
```java
new FintSse("http://localhost:8080/sse/%s");
```

If the SseUrl contains a placeholder at the end a UUID is generated and added before registering the client.
By default concurrent connections is also enabled, where 2 sse connections are registered on connect. This can be disabled by `disableConcurrentConnections()`.

It is possible to specifcy the SSE thread interval in milliseconds, this is 10 minutes by default
```java
new FintSse("http://localhost:8080/sse/%s", TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES));
```

### Create an event listener
```java
public class MyEventListener extends AbstractEventListener {
    
    // Optional, enum action supported by this listener
    // The constructor can also be empty, where no specific action is configured
    public MyEventListener() {
        super(TestActions.MY_TEST_ACTION);
    }
    
    @Override
    public void onEvent(Event event) {
        ...
    }
}
```

### Connect to the SSE server
```java
fintSse.connect(myEventListener);
```

It is also possible to send in headers that will be sent with the SSE request
```java
Map<String, String> headers = ImmutableMap.of("x-org-id", "mock.no");
fintSse.connect(listener, headers);
```

### Verify connection

Returns true if the connection is open.  
If it is closed `FintSse` will try to reconnect and return false.
```java
boolean connected = fintSse.verifyConnection();
```

### Close connection

```java
fintSse.close();
```

By default the client will run two simultaneous SSE connections, this can be disabled
```java
fintSse.disableConcurrentConnections();
```

## OAuth

Enable support for OAuth by sending in `TokenService` when creating a new instance:
```java
new FintSse("http://localhost:8080/sse/%s", tokenService);
```

This will automatically add a bearer token to the authorization header.  

**Basic authentication**  
Basic authentication is enabled by default by spring-security (used by fint-oauth-token-service).  
To disable add this property: `security.basic.enabled=false`

**[OAuth config](https://github.com/FINTlibs/fint-oauth-token-service#configuration)**