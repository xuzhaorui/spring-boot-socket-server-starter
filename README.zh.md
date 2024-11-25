# spring-boot-socket-server-starter
<h1>Spring Boot Socket Server Starter 快速入门</h1>

<h2>1. 添加依赖项</h2>

<p>要使用 Starter，请添加 <code>pom.xml</code>:</p>

<p><code>xml
&lt;dependency&gt;
    &lt;groupId&gt;io.github.xuzhaorui&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-socket-server-starter&lt;/artifactId&gt;
    &lt;version&gt;0.0.7&lt;/version&gt;
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

<h2>4. 标记 URL、ClientAsServer 和状态代码</h2>

<p>将 URL 标记为 <code>@SocketUrl</code> 在 <code>HeaderVO</code> class:</p>

<p><code>java
<pre>
<code>
@Data
public class HeaderVO {
    // 是否是客户端主动向服务端发送消息
    @ClientAsServer
    private boolean clientAsServer;
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
@Slf4j
public  class KryoSerializer {
    // 静态内部类方式，JVM 会确保类的加载线程安全
    private static class Holder {
        private static final KryoSerializer INSTANCE = new KryoSerializer();
    }
    public static KryoSerializer getKryoSerializer() {
        return Holder.INSTANCE;
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
@Slf4j
@RequiredArgsConstructor
@SocketController
public class SocketControllerTest {

    private final SocketAuthenticationSuccessfulStorage socketAuthenticationSuccessfulStorage;
    private final ReadWriteMode readWriteMode;
    private final ClientAsServerResponseHandler clientAsServerResponseHandler;

    private final byte[] demo = getDemo();
    public static byte[] getDemo() {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setBody("123");
        HeaderVO headerVO = new HeaderVO();
        ResultVO resultVO = new ResultVO();
        headerVO.setDate(new Date()).setCommandId("/login").setEqpId("123456").setClientAsServer(false);
        resultVO.setCode(200).setMessageCh("成功");
        responseVO.setHeader(headerVO);
        responseVO.setResult(resultVO);

        // 确保 KryoSerializer 初始化成功
        KryoSerializer kryoSerializer = KryoSerializer.getKryoSerializer();
        if (kryoSerializer == null) {
            throw new NullPointerException("KryoSerializer 初始化失败");
        }
        return kryoSerializer.serialize(responseVO);
    }
    @SocketMapping("/login")
    public ResponseVO test(@SocketRequestData ResponseVO responseVO, SocketRequest request, SocketResponse response) {
        if (socketAuthenticationSuccessfulStorage.storeAuthenticatedClientConnectionInfo("test", new ClientConnectionInfo(request.getClientSocket(), null))) {
            responseVO.getResult().setMessageCh("认证成功");
        } else {
            responseVO.getResult().setCode(-200);
            responseVO.getResult().setMessageCh("认证失败");
        }
        responseVO.getHeader().setDate(new Date());

        // 新建线程等待主线程写入操作完成后再执行 sendMessageAndAwaitResponse
        new Thread(() -> {
            try {
                // 等待直到响应完成写入
                synchronized (response) {
                    while (!response.isHasWritten()) {
                        log.info("等待写入完成...");
                        response.wait(1000); // 每秒检查一次状态
                    }
                    log.info("写入完成，写入次数：{}", response.getWriteCount());
                }

                // 写入完成后执行二次消息发送
                sendMessageAndAwaitResponse();
            } catch (InterruptedException e) {
                log.error("线程等待写入时出错: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }).start();

        return responseVO;
    }

    public void sendMessageAndAwaitResponse() {
        try {
            ClientConnectionInfo clientConnectionInfo = socketAuthenticationSuccessfulStorage.getAuthenticatedClientConnectionInfo("test");
            Socket socket = clientConnectionInfo.getSocket();
            if (socket != null && !socket.isClosed()) {
                OutputStream outputStream = socket.getOutputStream();
                readWriteMode.write(outputStream, demo, 4);
                outputStream.flush();
                log.info("二次发出消息");
                clientAsServerResponseHandler.sendMessageAndAwaitResponse(message -> {
                    log.info("进行消费，消费消息为：{}", message);
                });
            } else {
                log.warn("Socket已关闭，无法发送消息");
            }
        } catch (Exception e) {
            log.error("二次消息发送失败: {}", e.getMessage());
        }
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

    private static final String SERVER_HOST = "127.0.0.1";  // 服务器地址
    private static final int SERVER_PORT = 8888;  // 服务器端口
    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);
    private static final int TIMEOUT = 5000;  // 超时时间
    private static final AtomicInteger messageCounter = new AtomicInteger(0); // 追踪消息次数

    public static ResponseVO getDemo() {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setBody("123");
        responseVO.setHeader(new HeaderVO().setDate(new Date()).setCommandId("/login").setEqpId("123456").setClientAsServer(false));
        responseVO.setResult(new ResultVO().setCode(200).setMessageCh("成功"));
        return responseVO;
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        // 新建资源类
        SendTheTask sendTheTaskJson = new SendTheTask("kryo");

        // 线程池执行资源类任务
        executorService.execute(sendTheTaskJson::run);
        executorService.shutdown();
    }

    private static class SendTheTask {
        private final String serializationManner;

        private SendTheTask(String serializationManner) {
            this.serializationManner = serializationManner;
        }

        public void run() {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
                // 获取输出流（复用，不需要每次发送都重新获取）
                OutputStream outputStream = socket.getOutputStream();

                // 序列化并构建消息
                byte[] serializedMessage = getSerializedMessage(serializationManner);
                byte[] fullMessage = buildMessageWithLengthHeader(serializedMessage);
                log.info("Serialized message: {}", Arrays.toString(serializedMessage));

                // 启动接收消息的线程
                Thread receiveThread = new Thread(() ->{
                        receiveMessages(socket, serializationManner);
                },serializationManner);
                receiveThread.start();

                // 循环发送消息
                while (!Thread.currentThread().isInterrupted()) {
                    sendMessage(outputStream, fullMessage);
                    log.info("Sent binary message: {} bytes", fullMessage.length);
                    TimeUnit.SECONDS.sleep(2);// 每隔2秒发送一次
                }
            } catch (IOException | InterruptedException e) {
                log.error("Client error: ", e);
            }
        }
    }

    private static byte[] getSerializedMessage(String serializationManner) {
        // 初始化要发送的消息
        ResponseVO demo = getDemo();
        return getBytes(serializationManner, demo);
    }
    
    private static byte[] getSerializedMessage(ResponseVO responseVO, String serializationManner) {
        return getBytes(serializationManner, responseVO);
    }
    
    private static byte[] getBytes(String serializationManner, ResponseVO demo) {
        byte[] serializedMessage;
        if (Objects.equals(serializationManner, "kryo")) {
            serializedMessage = KryoSerializer.getKryoSerializer().serialize(demo);
        } else if (Objects.equals(serializationManner, "json")) {
            serializedMessage = JsonUtil.boToJson(demo).getBytes(StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Invalid serialization manner: " + serializationManner);
        }
        return serializedMessage;
    }

    
    private static byte[] buildMessageWithLengthHeader(byte[] message) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + message.length);
        buffer.putInt(message.length);  // 消息长度
        buffer.put(message);  // 消息体
        return buffer.array();
    }

    private static void sendMessage(OutputStream outputStream, byte[] message) throws IOException {
        outputStream.write(message);
        outputStream.flush();
    }

    /**
     * 接收并处理来自服务器的消息
     */
    private static void receiveMessages(Socket socket, String serializationManner) {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {


            while (!Thread.currentThread().isInterrupted()) {
                // 检查是否有消息长度可读取
                int available = inputStream.available();
                if (available >= 4) {
                    log.info("消息长度:{}",available);
                    byte[] lengthBuffer = new byte[4];
                    int bytesRead = inputStream.read(lengthBuffer);

                    if (bytesRead == -1) {
                        log.warn("服务器关闭了连接");
                        break;
                    }

                    // 解析消息长度
                    int messageLength = ByteBuffer.wrap(lengthBuffer).getInt();

                    // 根据长度读取完整的消息体
                    byte[] messageBuffer = new byte[messageLength];
                    int totalBytesRead = 0;
                    while (totalBytesRead < messageLength) {
                        int read = inputStream.read(messageBuffer, totalBytesRead, messageLength - totalBytesRead);
                        if (read == -1) {
                            log.warn("服务器在读取消息时关闭了连接");
                            break;
                        }
                        totalBytesRead += read;
                    }

                    // 反序列化消息
                    ResponseVO response = deserializeMessage(serializationManner, messageBuffer);
                    log.info("次数为: {}", messageCounter.get());

                    // 如果是客户端作为服务端响应的场景
                    if ("/login".equals(response.getHeader().getCommandId()) && messageCounter.get() == 1) {
                        log.info("客户端作为服务端响应消息");
                        response.getHeader().setClientAsServer(true);  // 设置客户端响应

                        // 发送响应消息
                        byte[] serializedMessage = getSerializedMessage(response, serializationManner);
                        byte[] fullMessage = buildMessageWithLengthHeader(serializedMessage);
                        sendMessage(outputStream, fullMessage);
                        log.info("客户端发送了响应消息: {}", Arrays.toString(serializedMessage));
                    }
                    messageCounter.incrementAndGet();  // 增加计数器，准备接收下一条消息
                    log.info("次数为: {}", messageCounter.get());
                    log.info("接收和反序列化的消息: {}", response);
                }
            }
        } catch (IOException e) {
            log.error("Error receiving message: ", e);
        }
    }


    private static ResponseVO deserializeMessage(String serializationManner, byte[] messageBuffer) {
        ResponseVO response;
        if (Objects.equals(serializationManner, "kryo")) {
            response = KryoSerializer.getKryoSerializer().deserialize(messageBuffer, ResponseVO.class);
        } else if (Objects.equals(serializationManner, "json")) {
            String messageStr = new String(messageBuffer, StandardCharsets.UTF_8);
            response = JsonUtil.jsonToBo(ResponseVO.class, messageStr);
        } else {
            throw new RuntimeException("不支持的序列化方式");
        }
        return response;
    }
}
</code>
</pre>
</code>
</p>
<h2>就是这样！您已准备好使用 Spring Boot 构建基于Socket的应用程序。</h2>

