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

compile('no.fint:fint-sse:1.1.3')

compile('org.glassfish.jersey.core:jersey-client:2.26') 
compile('org.glassfish.jersey.core:jersey-common:2.26')
```

The `jersey-client` and `jersey-common` is required to get the correct version of the dependencies used by `fint-sse`.

## Usage

### Create new instance.
```java
new FintSse("http://localhost:8080/sse/%s");
```

If the SseUrl contains a placeholder at the end a UUID is generated and added before registering the client.
By default concurrent connections is also enabled, where 2 sse connections are registered on connect. This can be disabled by using [FintSseConfig](#configuration).

### Create an event listener
```java
public class MyEventListener extends AbstractEventListener {
    
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

`verifyConnection()` will automatically reconnect if a connection is lost.

### Close connection

```java
fintSse.close();
```

### SSE Configuration

When creating a new instance it is possible to send in `FintSseConfig` with the following configuration options:
* **sseThreadInterval (long)** -  The time between the two SSE connection threads in milliseconds, this is 10 minutes by default.
* **concurrentConnections (boolean)** - If two connection threads are enabled/disabled. By default the client will run two simultaneous SSE connections.
* **orgIds (String[])** - The orgId(s) supported by the SSE connection. If no orgIds are configured, all values are accepted. If the event received contains an orgId that is not supported a message will be logged and the event is not passed on the the event listener.
* **accessTokenReplacementUri (String)** - The part of the SSE url that will be replaced when building the access token uri (default: /provider).
* **accessTokenRequestUri (String)** - The replacement string when building the access token uri (default: /provider/sse/auth-init).

```java
FintSseConfig config = FintSseConfig.builder().sseThreadInterval(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)).build();
FintSse fintSse = new FintSse("http://localhost/sse/%s", config);
```

It is often required only to configure the organisation ids, the `withOrgIds` helper method is created for this:

```java
FintSseConfig config = FintSseConfig.withOrgIds(orgIds);
```

## OAuth

Enable support for OAuth by sending in `TokenService` when creating a new instance:
Import the `OAuthConfig` class fro the `@Configuration`.

```java
@Import(OAuthConfig.class)
@Configuration
public class Config {
    ...
}
```

Autoimport the `TokenService` and call `getAccessToken()`.  
If the property `fint.oauth.enabled` is set to `false` the `TokenService` can be null.

```java
@Autowired(required = false)
private TokenService tokenService;

public void myMethod() {
    if(tokenService != null) {
        String accessToken = tokenService.getAccessToken();
        ...
    }
}
```

When `fint.oauth.enabled` is set to `true`, the [`OAuth2RestTemplate`](https://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/client/OAuth2RestTemplate.html) will be injected for `@Autowired RestTemplate`.  
If the configuration value is not set or set to `false` a standard `RestTemplate` is used.

### OAuth Configuration

| Key | Description |
|-----|-------------|
| fint.oauth.enabled | true / false. Enables / disables the TokenService. Disabled by default. |
| fint.oauth.username | Username |
| fint.oauth.password | Password |
| fint.oauth.access-token-uri | Access token URI |
| fint.oauth.client-id | Client id |
| fint.oauth.client-secret | Client secret |
| fint.oauth.scope | Scope |

**Basic authentication**  
Basic authentication is enabled by default by spring-security.  
To disable add this property: `security.basic.enabled=false`

## Log

To enable debug log:
```
logging.level.no.fint.oauth.OAuthConfig: DEBUG
logging.level.no.fint.sse.FintSse: DEBUG
```