# java6tls1.2

자바 6 환경에서 일부 연결에만 TLS 1.2 를 사용하는 데모를 제공합니다.  
Demo for using TLS 1.2 selectively on Java 6 environment

# 의존성 / dependency
(*.jar 파일들은 아래 build system 을 사용하시면 필요하지 않습니다.) 

(You won't need *.jar files if you use any of below build systems.)

## Maven

```xml

<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpkix-jdk15on</artifactId>
    <version>1.55</version>
</dependency>
```

## Gradle(groovy)

``` groovy
implementation group: 'org.bouncycastle', name: 'bcpkix-jdk15on', version: '1.55'
```

## Gradle(kts)

``` kotlin
implementation("org.bouncycastle:bcpkix-jdk15on:1.55")
```

# 실행하는 법 / How to run

시작하기 전에, Java 가 Java 6 인지 확인해주세요. Java 7 이상은 TLS 1.2 를 이미 지원하기 때문에 이 방법을 사용하실 필요가 없습니다.  
Before you begin, please check if your Java is Java 6. From Java 7, Java is shipped with built-in support for TLS 1.2 so
you won't need this repository.

```shell
$ java -version
java version "1.6.0_45"
Java(TM) SE Runtime Environment (build 1.6.0_45-b06)
Java HotSpot(TM) 64-Bit Server VM (build 20.45-b01, mixed mode)
```

## 데모 설명 / demo explanations

이 저장소는 두 종류의 데모를 포함합니다.

`TlsDemoUsingDelegation`는 모든 연결을 가로채서 일부 원하는 연결에만 TLSv1.2 를 적용하고 나머지는 Java 6 의 기존 동작을 적용합니다.

`TlsDemoSingleTarget`는 TLSv1.2 사용이 필요한 곳에만 설정되어 TLSv1.2 를 적용하고, 나머지 연결에는 전혀 관여하지 않습니다.

|                        | TLSv1.2 사용 판단 주체 | Socket Factory가 TLSv1.2 를 사용할 host 를 알아야함 | 유저가 Socket factory 를 스스로 주입해야함 | 모든 통신에 개입함 |
|------------------------|------------------|-------------------------------------------|--------------------------------|------------|
| TlsDemoUsingDelegation | Socket Factory   | ✅                                         | ❌                              | ✅          |
| TlsDemoSingleTarget    | 사용자              | ❌                                         | ✅                              | ❌          |

`TlsDemoUsingDelegation` intercepts all communication and applies TLSv1.2 to only selected hosts and lets other
communications use Java 6 default communication. (No TLSv1.2)

`TlsDemoSingleTarget` is only applied to selected communications and make them use TLSv1.2. Other communications are not
affected at all.

|                        | Who decides to use TLSv1.2? | Socket factory needs info about hosts to use TLSv1.2 | User should manually inject socket factory  | Intercept all connection? |
|------------------------|-----------------------------|------------------------------------------------------|---------------------------------------------|---------------------------|
| TlsDemoUsingDelegation | The Socket factory          | ✅                                                    | ❌                                           | ✅                         |
| TlsDemoSingleTarget    | The user                    | ❌                                                    | ✅                                           | ❌                         |

## 컴파일 / Compilation

`javac` 를 이용해 `TlsDemoSingleTarget`와 `TlsDemoUsingDelegation`를 컴파일해주세요.  
Please use `javac` to compile `TlsDemoSingleTarget` and `TlsDemoUsingDelegation`.

```shell
$ javac -cp ".:bcpkix-jdk15on-1.55.jar:bcprov-jdk15on-1.55.jar" TlsDemoSingleTarget.java TlsDemoUsingDelegation.java
```

## TlsDemoUsingDelegation 실행하기 / Running TlsDemoUsingDelegation

```shell
$ java -cp ".:bcpkix-jdk15on-1.55.jar:bcprov-jdk15on-1.55.jar" TlsDemoUsingDelegation
Fire GET request to https://www.google.com/
Handshake completed with www.google.com using TLSv1
Fire GET request to https://amazing.today/
Handshake completed with amazing.today using TLSv1.2
```

저희가 TLSv1 를 사용하도록 설정한 `amazing.today` 와의 연결에만 TLSv1.2 가 사용된 것을 확인하실 수 있습니다.  
You can check that only the connection to `amazing.today` used TLSv1.2, as we configured to do so.

## TlsDemoSingleTarget 실행하기 / Running TlsDemoSingleTarget

```shell
$ java -cp ".:bcpkix-jdk15on-1.55.jar:bcprov-jdk15on-1.55.jar" TlsDemoSingleTarget
Fire GET request to https://www.google.com/
Fire GET request to https://amazing.today/
Handshake completed with amazing.today using TLSv1.2
```

역시 저희가 TLSv1 를 사용하도록 설정한 `amazing.today` 와의 연결에만 TLSv1.2 가 사용되었습니다. 이 경우에는 google 과의 연결에는 저희 코드가 전혀 관여하지 않아 SSL 버전이 로그로
찍히지 않았지만, 내부 동작을 확인하시면 TLSv1 로 연결된 것을 확인하실 수 있습니다.  
In this case too, we can see that only the connection to `amazing.today` used TLSv1.2. The connection to google did not
leave log because our code does not affect the connection in any way - hence no logs. However, if you see the internals
you could verify that it used TLSv1.

# 구동 원리 / How it works

## TlsDemoUsingDelegation

### 한국어

`HttpsURLConnection#setDefaultSSLSocketFactory` 를 이용하여 일부 host 에만 TLS 1.2 를 사용합니다.  
`HttpsURLConnection#setDefaultSSLSocketFactory` 를 사용하면 모든 연결이 해당 Socket factory 를 경유하는데, 이 SocketFactory 가 스스로 어떤 연결에
TLS 1.2 를 적용할지 판단합니다.  
사용자는 `TLSSocketFactory` 에서 `TLS_1_2_HOSTS`에 TLS 1.2 를 사용하기 원하는 host 를 입력합니다. 기본설정은 아래와 같습니다.

``` java
/**
 * Hosts to use TLS 1.2. When using {@link TlsDemoUsingDelegation} approach, should be deleted.
 *
 * TLS 1.2 를 사용할 호스트입니다. {@link TlsDemoUsingDelegation} 방식으로 이 클래스를 사용하시면 삭제하셔도 무방합니다.
 */
private static final String[] TLS_1_2_HOSTS = new String[] { "amazing.today", "vtov.studio" };
```

그러면 이 `TLS_1_2_HOSTS` 필드를 이용하는 함수인 `shouldUseTLS1_2` 가 TLS 1.2 를 사용할지를 판단하여 필요한 연결에만 TLS 1.2 를 사용합니다.

``` java
/**
 * When using {@link TlsDemoUsingDelegation} approach, should be deleted.
 *
 * {@link TlsDemoUsingDelegation} 방식으로 사용하시면 삭제되어야합니다.
 */
private static boolean shouldUseTLS1_2(final String host) {
    for (final String tlsHost: TLS_1_2_HOSTS) {
        if (host.contains(tlsHost)) return true;
    }
    return false;
}
```

위 `shouldUseTLS1_2` 검사를 실패한 연결들은 모두 Java 6의 기본 SocketFactory 인 `SSLSocketFactory.getDefault()`를 사용하여 연결됩니다.

### English

Connect to only selected hosts using TLS 1.2 with `HttpsURLConnection#setDefaultSSLSocketFactory`.  
We use `HttpsURLConnection#setDefaultSSLSocketFactory` which will in result cause all connections to go through
specified Socket factory. The Socket factory itself determines which connection to use TLS 1.2 and which not to.  
The user must specify the hosts to use TLS 1.2 in `TLSSocketFactory`. The default setting is provided below.

``` java
/**
 * Hosts to use TLS 1.2. When using {@link TlsDemoUsingDelegation} approach, should be deleted.
 *
 * TLS 1.2 를 사용할 호스트입니다. {@link TlsDemoUsingDelegation} 방식으로 이 클래스를 사용하시면 삭제하셔도 무방합니다.
 */
private static final String[] TLS_1_2_HOSTS = new String[] { "amazing.today", "vtov.studio" };
```

Then the function `shouldUseTLS1_2` will use the above provided field `TLS_1_2_HOSTS` to determine which connection
should TLS 1.2 be used.

``` java
/**
 * When using {@link TlsDemoUsingDelegation} approach, should be deleted.
 *
 * {@link TlsDemoUsingDelegation} 방식으로 사용하시면 삭제되어야합니다.
 */
private static boolean shouldUseTLS1_2(final String host) {
    for (final String tlsHost: TLS_1_2_HOSTS) {
        if (host.contains(tlsHost)) return true;
    }
    return false;
}
```

All connections which failed above `shouldUseTLS1_2` test will fall back to use Java 6's default socket
factory, `SSLSocketFactory.getDefault()`.

## TlsDemoSingleTarget

### 한국어

`HttpsURLConnection#setSSLSocketFactory` 를 사용하여 TLS 1.2 를 사용합니다.  
TLS 1.2 를 사용할 연결에만 `HttpsURLConnection#setSSLSocketFactory` 를 통해 Socket factory 를 지정해줍니다. 이 Socket factory 는 모든 연결에 TLS
1.2 를 지원합니다.

하지만 `connect`와 `connectTls1_2`의 구분에서 보듯이 모든 연결마다 factory 를 주입해주어야 합니다.

### 영어

Uses TLS 1.2 using `HttpsURLConnection#setSSLSocketFactory`.  
In this method, only selected connections use our Socket factory (as opposed to all connections using our socket
factory).

And thus socket factory do not need to know which host to use TLS 1.2 and which host not to. So the Socket factory won't
even need the delegation pattern.

However, as you can see in `connect` and, `connectTls1_2`, all connections that wish to use TLS 1.2 must explicitly have
the socket factory injected.

# OSS

This demo used https://github.com/a--i--r/TLSSocketFactory
