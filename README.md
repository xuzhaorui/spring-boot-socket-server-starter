# spring-boot-socket-server-starter
<h1>Spring Boot Socket Server Starter Quickstart</h1>

<h2>1. Add Dependencies</h2>

<p>To use the starter, include the following dependency in your <code>pom.xml</code>:</p>

<p><code>xml
&lt;dependency&gt;
    &lt;groupId&gt;io.github.xuzhaorui&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-socket-server-starter&lt;/artifactId&gt;
    &lt;version&gt;0.0.6&lt;/version&gt;
&lt;/dependency&gt;
</code></p>

<p>For testing, add the following dependencies:</p>

<p><code>xml
&lt;dependency&gt;
    &lt;groupId&gt;org.projectlombok&lt;/groupId&gt;
    &lt;artifactId&gt;lombok&lt;/artifactId&gt;
    &lt;scope&gt;provided&lt;/scope&gt;
&lt;/dependency&gt;
&lt;dependency&gt;
    &lt;groupId&gt;com.esotericsoftware&lt;/groupId&gt;
    &lt;artifactId&gt;kryo&lt;/artifactId&gt;
    &lt;version&gt;5.6.2&lt;/version&gt;
&lt;/dependency&gt;
</code></p>

<h2>2. Configuration File</h2>

<p>The following properties are available to configure the socket server in your <code>application.yml</code>:</p>

<p><code>yaml
socket:
  server:
    port: 8888
    scan-socket-message-package: com.xx.domain
    reading-mode: length
    length: 4
    print-serializer-memory-address: true
</code></p>

<ul>
<li><strong>socket.server.port</strong>: The port the server listens on (default: 8888).</li>
<li><strong>socket.server.scan-socket-message-package</strong>: The package to scan for socket messages (default: global).</li>
<li><strong>socket.server.reading-mode</strong>: The mode to read the message (<code>line</code> for line-based or <code>length</code> for binary or text-based, default: <code>line</code>).</li>
<li><strong>socket.server.length</strong>: The length of the message (used in <code>length</code> mode, default: 4).</li>
<li><strong>socket.server.print-serializer-memory-address</strong>: Whether to print the serializer memory address (default: <code>false</code>).</li>
</ul>

<h2>3. Define Message Object</h2>

<p>Define a message object with <code>@SocketMessage</code> and specify the serializer (can be multiple):</p>

<p><code>java
@Data
@SocketMessage(serializer = {KryoSocketSerializer.class, JsonSerialization.class})
public class ResponseVO implements Serializable {
    private Object body;
    @Valid @NotNull(message = "消息头不能为null")
    private HeaderVO header;
    @Valid @NotNull(message = "消息体不能为null")
    private ResultVO result;
}
</code></p>

<h2>4. Mark URL and Status Codes</h2>

<p>Mark the URL with <code>@SocketUrl</code> in your <code>HeaderVO</code> class:</p>

<p>```java
@Data
public class HeaderVO {
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;</p>

<pre><code>@NotBlank @SocketUrl
private String commandId;

@NotBlank
private String eqpId;

private String sessionId;
</code></pre>

<p>}
```</p>

<p>Mark status codes and response messages with <code>@SocketCode</code> and <code>@SocketMsg</code> in <code>ResultVO</code>:</p>

<p><code>java
@Data
public class ResultVO {
    @NotNull
    private Integer code;
    @SocketMsg
    private String messageCh;
}
</code></p>

<h2>5. Define Serializers</h2>

<h3>JSON Serializer:</h3>

<p>```java
public class JsonSerialization implements SocketMessageSerializer {
    @Override
    public String serialize(Object object) throws RuntimeException {
        return JsonUtil.boToJson(object);
    }</p>

<pre><code>@Override
public ResponseVO deserialize(Object message, Class&lt;?&gt; clazz) throws Exception {
    return JsonUtil.jsonToBo(clazz, (String) message);
}
</code></pre>

<p>}
```</p>

<h3>Kryo Serializer:</h3>

<p>```java
@Slf4j
public class KryoSocketSerializer implements SocketMessageSerializer {
    @Override
    public byte[] serialize(Object object) throws RuntimeException {
        return KryoSerializer.getKryoSerializer().serialize(object);
    }</p>

<pre><code>@Override
public ResponseVO deserialize(Object message, Class&lt;?&gt; clazz) throws Exception {
    return KryoSerializer.getKryoSerializer().deserialize((byte[]) message, clazz);
}
</code></pre>

<p>}
```</p>

<h2>6. Define Socket Controller</h2>

<p>Define a simple controller with a <code>@SocketMapping</code> method:</p>

<p>```java
@SocketController
@RequiredArgsConstructor
public class SocketControllerTest {</p>

<pre><code>private final SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage;

@SocketMapping("/login")
public ResponseVO testLogin(@SocketRequestData ResponseVO responseVO, 
                            @SocketContextSocket Socket socket, 
                            @SocketRequestData("header.date") Date date) {
    String ipAndPort = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    if (socketAuthenticationSuccessfulStorage.storeAuthenticatedSocketMetBean(ipAndPort, 
            new SocketMetBean(socket, responseVO))) {
        responseVO.getResult().setMessageCh("认证成功");
    } else {
        responseVO.getResult().setCode(-200);
        responseVO.getResult().setMessageCh("认证失败");
    }
    responseVO.getHeader().setDate(new Date());
    return responseVO;
}
</code></pre>

<p>}
```</p>

<h2>7. Socket Client Example</h2>

<p>Here's a simple socket client:</p>

<p>```java
public class SocketClient {</p>

<pre><code>private static final String SERVER_HOST = "localhost";
private static final int SERVER_PORT = 8888;

public static ResponseVO getDemo() {
    ResponseVO responseVO = new ResponseVO();
    responseVO.setBody("123");
    responseVO.setHeader(new HeaderVO().setDate(new Date()).setCommandId("/login").setEqpId("123456"));
    responseVO.setResult(new ResultVO().setCode(200).setMessageCh("成功"));
    return responseVO;
}

public static void main(String[] args) {
    new Thread(new SendTheTask("json")).start();
    new Thread(new SendTheTask("kryo")).start();
}

private static class SendTheTask implements Runnable {
    private final String serializationManner;

    public SendTheTask(String serializationManner) {
        this.serializationManner = serializationManner;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(5000);
            OutputStream outputStream = socket.getOutputStream();

            byte[] serializedMessage = getSerializedMessage(serializationManner);
            byte[] fullMessage = buildMessageWithLengthHeader(serializedMessage);
            outputStream.write(fullMessage);
            outputStream.flush();

            receiveMessages(socket, serializationManner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

private static byte[] getSerializedMessage(String serializationManner) {
    ResponseVO demo = getDemo();
    if ("kryo".equals(serializationManner)) {
        return KryoSerializer.getKryoSerializer().serialize(demo);
    } else {
        return JsonUtil.boToJson(demo).getBytes(StandardCharsets.UTF_8);
    }
}

private static byte[] buildMessageWithLengthHeader(byte[] message) {
    ByteBuffer buffer = ByteBuffer.allocate(4 + message.length);
    buffer.putInt(message.length);
    buffer.put(message);
    return buffer.array();
}

private static void receiveMessages(Socket socket, String serializationManner) {
    try (InputStream inputStream = socket.getInputStream()) {
        byte[] lengthBuffer = new byte[4];
        inputStream.read(lengthBuffer);
        int messageLength = ByteBuffer.wrap(lengthBuffer).getInt();
        byte[] messageBuffer = new byte[messageLength];
        inputStream.read(messageBuffer);
        ResponseVO response;
        if ("kryo".equals(serializationManner)) {
            response = KryoSerializer.getKryoSerializer().deserialize(messageBuffer, ResponseVO.class);
        } else {
            response = JsonUtil.jsonToBo(ResponseVO.class, new String(messageBuffer, StandardCharsets.UTF_8));
        }
        System.out.println("Received: " + response);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
</code></pre>

<p>}
```</p>

<h2>That's it! You're ready to build a socket-based application with Spring Boot.</h2>

