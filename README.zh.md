# spring-boot-socket-server-starter
<h1>Spring Boot Socket Server Starter 快速入门</h1>

<h2>1. 添加依赖项</h2>

<p>要使用 Starter，请在你的 <code>pom.xml</code>:</p>

<p><code>xml
&lt;dependency&gt;
    &lt;groupId&gt;io.github.xuzhaorui&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-socket-server-starter&lt;/artifactId&gt;
    &lt;version&gt;0.0.6&lt;/version&gt;
&lt;/dependency&gt;
</code></p>

<p>为了进行测试，请添加以下依赖项:</p>

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

<h2>2. 配置文件</h2>

<p>以下属性可用于在 <code>application.yml</code>:</p>

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
<li><strong>socket.server.port</strong>: 服务器侦听的端口 (默认: 8888).</li>
<li><strong>socket.server.scan-socket-message-package</strong>: 用于扫描Socket消息的软件包 (默认: 全局).</li>
<li><strong>socket.server.reading-mode</strong>: 读取消息的模式 (<code>line</code> 对于基于文本 <code>length</code> 用于二进制或基于文本, 默认: <code>line</code>).</li>
<li><strong>socket.server.length</strong>: 消息的长度 (用于 <code>length</code> 模式, 默认: 4).</li>
<li><strong>socket.server.print-serializer-memory-address</strong>: 是否打印序列化器内存地址 (默认: <code>false</code>).</li>
</ul>

<h2>3. 定义消息对象</h2>

<p>定义消息对象 <code>@SocketMessage</code> 并指定序列化器 (可以是多个):</p>

<p><code>java
<pre>
<code>
@Data
@SocketMessage(serializer = {KryoSocketSerializer.class, JsonSerialization.class})
public class ResponseVO implements Serializable {
    private Object body;
    @Valid @NotNull(message = "消息头不能为null")
    private HeaderVO header;
    @Valid @NotNull(message = "消息体不能为null")
    private ResultVO result;
}
</code>
</pre>
</code>
</p>

<h2>4. 标记 URL 和状态代码</h2>

<p>将 URL 标记为 <code>@SocketUrl</code> 在 <code>HeaderVO</code> class:</p>

<p><code>java
<pre>
<code>
@Data
public class HeaderVO {
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    @NotBlank
    @SocketUrl
    private String commandId;
    @NotBlank
    private String eqpId;
    private String sessionId;
}
</code>
</pre>
</code>
</p>

<p>标记状态代码和响应消息 <code>@SocketCode</code> 与 <code>@SocketMsg</code> in <code>ResultVO</code>:</p>

<p><code>java
<pre>
<code>
@Data
public class ResultVO {
    @SocketCode
    private Integer code;
    @SocketMsg
    private String messageCh;
}
</code>
</pre>
</code>
</p>

<h2>5. 定义序列化程序</h2>

<h3>JSON 序列化:</h3>

<p><code>java
<pre>
<code>
public class JsonSerialization implements SocketMessageSerializer {
    @Override
    public String serialize(Object object) throws RuntimeException {
        return JsonUtil.boToJson(object);
    }
    @Override
    public ResponseVO deserialize(Object message, Class<?> clazz) throws Exception {
        try {
            if (message instanceof String){
                return (ResponseVO) JsonUtil.jsonToBo(clazz,(String) message);
            }else if(message instanceof byte[]){
                byte[]  message1 = (byte[]) message;
                String message2 = new String(message1, StandardCharsets.UTF_8);
                return (ResponseVO) JsonUtil.jsonToBo(clazz,message2);
            }else {
                throw new RuntimeException("不支持的类型");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
/**
 * Json工具
 *
 * @Author 陈文
 * @Date 2019/12/25  9:53
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {

        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 注册 JavaTimeModule 以支持 Java 8 日期和时间
        MAPPER.registerModule(new JavaTimeModule());

        // 设置全局时区为上海
        MAPPER.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));


        MAPPER.configOverride(Date.class)
                .setFormat(JsonFormat.Value.forPattern("yyyyMMdd HH:mm:ss")
                        .withTimeZone(TimeZone.getTimeZone("Asia/Shanghai")));  // 设置时区

        // 配置序列化特性，支持空值
        MAPPER.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        //配置ObjectMapper，遇到未知属性时不抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 配置ObjectMapper，遇到基本数据类型的空值时抛出异常
        MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

    }

    public static <T> T jsonToBo(Class<T> cls, String json) {
        try {
            if (!StringUtils.hasText(json)) {
                return null;
            }
            return MAPPER.readValue(json, cls);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
   
    public static <T> String boToJson(T bo) {
        String str = "";
        try {
            str = MAPPER.writeValueAsString(bo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
</code>
</pre>
</code>
</p>

<h3>Kryo 序列化:</h3>

<p><code>java
<pre>
<code>
public  class KryoSerializer {
    private static KryoSerializer me = new KryoSerializer();
    private  KryoSerializer(){}
    public static KryoSerializer getKryoSerializer() {
        return me;
    }
    /**
     * 因为 Kryo 不是线程安全的。因此，使用 ThreadLocal 来存储 Kryo 对象
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(ResponseVO.class);
        kryo.register(HeaderVO.class);
        kryo.register(ResultVO.class);
        kryo.register(Date.class);
        return kryo;
    });
    public <T >byte[] serialize(T object) throws RuntimeException {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, object);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
                    throw new  RuntimeException(e);
        }
    }
    public <R> R deserialize(byte[] data, Class<R> clazz) throws RuntimeException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
           throw new  RuntimeException(e);
        }
    }
}
public class KryoSocketSerializer implements SocketMessageSerializer{
    @Override
    public byte[] serialize(Object object) throws RuntimeException {
        return KryoSerializer.getKryoSerializer().serialize(object);
    }
    @Override
    public ResponseVO deserialize(Object message, Class<?> clazz) throws Exception {
        return (ResponseVO) KryoSerializer.getKryoSerializer().deserialize((byte[]) message, clazz);
    }
}
</code>
</pre>
</code>
</p>

<h2>6. 定义 Socket 控制器</h2>

<p>定义一个简单的控制器，其中包含 <code>@SocketMapping</code> method:</p>

<p><code>java
<pre>
<code>
@SocketController
@RequiredArgsConstructor
public class SocketControllerTest {

    private final SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage;
    
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
}
</code>
</pre>
</code>
</p>


<h2>7. Socket 客户端示例</h2>

<p>下面是一个简单的套接字客户端:</p>

<p><code>java
<pre>
<code>
public class SocketClient {

    private static final String SERVER_HOST = "localhost";  // 服务器地址
    private static final int SERVER_PORT = 8888;  // 服务器端口
    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);
    private static final int TIMEOUT = 5000;  // 超时时间
    public static ResponseVO getDemo(){
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

    private static class SendTheTask implements Runnable{
        private final String serializationManner;

        private SendTheTask(String serializationManner) {
            this.serializationManner = serializationManner;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
                socket.setSoTimeout(TIMEOUT); // 设置超时时间

                // 获取输出流（复用，不需要每次发送都重新获取）
                OutputStream outputStream = socket.getOutputStream();


                byte[] serializedMessage = getSerializedMessage(serializationManner);
                byte[] fullMessage = buildMessageWithLengthHeader(serializedMessage);
                log.info("Serialized message: " + Arrays.toString(serializedMessage));

                // 启动接收消息的线程
                Thread receiveThread = new Thread(() -> receiveMessages(socket,serializationManner),serializationManner);
                receiveThread.start();

                // 每隔2秒发送一次消息
                while (!Thread.currentThread().isInterrupted()) {
                    sendMessage(outputStream, fullMessage);
                    log.info("Sent binary message: " + fullMessage.length + " bytes");
                    Thread.sleep(2000); // 每隔2秒发送一次
                }
            } catch (IOException | InterruptedException e) {
                log.error("Client error: ", e);
            }
        }
    }


    private static byte[] getSerializedMessage(String serializationManner){
        // 初始化要发送的消息
        ResponseVO demo =getDemo();
        byte[] serializedMessage;
        if (Objects.equals(serializationManner, "kryo")) {
            serializedMessage = KryoSerializer.getKryoSerializer().serialize(demo);
        }else if (Objects.equals(serializationManner, "json")){
            serializedMessage = JsonUtil.boToJson(demo).getBytes(StandardCharsets.UTF_8);

        }else {
            throw new IllegalArgumentException("Invalid serialization manner: " + serializationManner);
        }
        return serializedMessage;

    }

    /**
     * 构建带有长度字段的消息，长度字段位于消息的前4个字节
     */
    private static byte[] buildMessageWithLengthHeader(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + message.length);
        buffer.putInt(message.length);  // 消息长度
        buffer.put(message);  // 消息体
        return buffer.array();
    }

    /**
     * 发送消息到服务器
     */
    private static void sendMessage(OutputStream outputStream, byte[] message) throws IOException {
        outputStream.write(message);
        outputStream.flush();
    }

    /**
     * 接收并处理来自服务器的消息
     */
    private static void receiveMessages(Socket socket, String serializationManner) {
        try (InputStream inputStream = socket.getInputStream()) {
            while (!Thread.currentThread().isInterrupted()) {
                // 先读取消息的长度
                byte[] lengthBuffer = new byte[4];
                int bytesRead = inputStream.read(lengthBuffer);
                if (bytesRead == -1) {
                    log.warn("Server closed the connection");
                    break;
                }
                if (bytesRead < 4) {
                    log.warn("Incomplete message length header received");
                    continue;
                }

                // 解析消息长度
                int messageLength = ByteBuffer.wrap(lengthBuffer).getInt();

                // 根据长度读取完整的消息体
                byte[] messageBuffer = new byte[messageLength];
                int totalBytesRead = 0;
                while (totalBytesRead < messageLength) {
                    int read = inputStream.read(messageBuffer, totalBytesRead, messageLength - totalBytesRead);
                    if (read == -1) {
                        log.warn("Server closed the connection while reading message");
                        break;
                    }
                    totalBytesRead += read;
                }

                if (totalBytesRead == messageLength) {
                    ResponseVO response;
                    if (Objects.equals(serializationManner, "kryo")){
                        response  = KryoSerializer.getKryoSerializer().deserialize(messageBuffer, ResponseVO.class);

                    }else if (Objects.equals(serializationManner, "json")){
                        String messageStr = new String(messageBuffer, StandardCharsets.UTF_8);
                        response = JsonUtil.jsonToBo(ResponseVO.class,messageStr);
                    }else {
                        throw new RuntimeException("不支持的序列化方式");
                    }
                    // 反序列化接收到的消息
                    log.info("Received and deserialized message: " + response);
                } else {
                    log.warn("Received incomplete message");
                }
            }
        } catch (IOException e) {
            log.error("Error receiving message: ", e);
        }
    }
}
</code>
</pre>
</code>
</p>
<h2>就是这样！您已准备好使用 Spring Boot 构建基于Socket的应用程序。</h2>

