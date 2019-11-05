package com.kp.taskmanager.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kp.common.constant.FieldConstant;
import com.kp.common.data.message.IMessageParser;
import com.kp.common.data.message.ObjectHelper;
import com.kp.common.data.vo.VArray;
import com.kp.common.data.vo.VObject;
import com.kp.common.processor.ProcessorManager;
import com.kp.common.utilities.ExceptionUtils;
import com.kp.common.worker.HandleCallback;
import com.kp.common.worker.Handler;
import com.kp.common.worker.KWorkerPool;
import com.kp.common.worker.impl.DisruptorWorkerPool;
import com.kp.configuration.ConfigStorage;
import com.kp.configuration.WorkerPoolCfg;
import com.kp.network.connection.DefaultConnectionManager;
import com.kp.network.connection.IConnection;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.ConnectionListener;
import com.kp.network.event.impl.DefaultConnectionListenerManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.taskmanager.network.codec.JsonMessageParser;
import com.kp.taskmanager.network.codec.VOMessage;
import com.kp.taskmanager.network.constant.Commands;
import com.kp.taskmanager.network.server.JsonNettyTcpServer;
import com.kp.taskmanager.thread.ManageableThread;
import com.kp.thread.ManageableObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.kp.common.constant.FieldConstant.*;
import static com.kp.network.utilities.IPConverter.InetAddressByIPv4;

public class ThreadManager implements ManageableObject, IThreadManager {

    protected String configPath = "./configuration/server.json";
    protected String serverId;
    protected String appId;
    protected JsonNettyTcpServer socketServer;
    protected VObject config;
    protected Class configLoaderClass;
    protected Map<String, VObject> threadInfos = new ConcurrentHashMap<>();
    protected ConfigStorage configStorage;
    protected Map<String, ManageableThread> threads = new ConcurrentHashMap<>();
    protected AtomicLong transId = new AtomicLong(1);
    protected VOMessage notifyMessage = new VOMessage(Commands.NOTIFY);
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private KWorkerPool<VOMessage, VOMessage> mailer;
    private Object lock = new Object();
    private ProcessorManager processorManager = new ProcessorManager("com.kp.taskmanager.manager.processor");

    public static void main(String[] args) {
        try {
            String log4jConfigFile = System.getProperty("user.dir") + File.separator + "configuration/log4j2.xml";
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jConfigFile));
            Configurator.initialize(ClassLoader.getSystemClassLoader(), source);
            ThreadManager lsn = new ThreadManager();
            lsn.init();
            lsn.start();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        lsn.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }));

        } catch (Throwable var3) {

            Logger log = LoggerFactory.getLogger("MAIN");
            log.error("FATAL error occured, system is interrupted", var3);
            System.exit(-1);
        }

    }

    private void loadConfig() throws Exception {
        File file = new File(configPath);
        try {

            String content = FileUtils.readFileToString(file);
            config = VObject.fromJSON(content);

        } catch (Exception e) {
            throw e;

        }

    }

    @Override
    public void logToFile(Object log) {
        getLogger().info("", log);
    }

    @Override
    public void logToMonitor(Object log) {
        if (log instanceof VOMessage) {
            try {
                ((VOMessage) log).put(CREATED_TIME, System.currentTimeMillis());
                mailer.publish((VOMessage) log);
            } catch (Exception e) {
                getLogger().error("publish message to mailer error ", e);
            }

        }


    }

    @Override
    public void logToFile(String region, Object log) {
        getLogger(region).info("", log);
    }

    @Override
    public String genTransId(String threadId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getServerId()).append(":").append(threadId).append(":").append(transId.incrementAndGet());
        return stringBuilder.toString();
    }

    @Override
    public Collection<ManageableThread> listThreads() {
        return threads.values();
    }

    @Override
    public ManageableThread getThreadById(String id) {
        if (id == null) {
            return null;
        } else {
            return threads.get(id);
        }
    }

    public String getServerId() {
        return this.serverId;
    }

    protected void notify(String msg, String threadId, String threadName) {
        notifyMessage.setTransId(genTransId(threadId));
        notifyMessage.put(FieldConstant.THREAD_ID, threadId);
        notifyMessage.put(FieldConstant.THREAD_NAME, threadId);
        notifyMessage.put(FieldConstant.MESSAGE, msg);
        logToMonitor(notifyMessage);
    }

    protected void loadThreads(Collection<VObject> vArray) {
        synchronized (lock) {
            vArray.forEach(vObject -> {

                try {
                    String threadId = vObject.getString(THREAD_ID);
                    if (threadId == null) {
                        throw new NullPointerException("Not found threadId");
                    }
                    String threadName = vObject.getString(THREAD_NAME);
                    if (threadName == null) {
                        throw new NullPointerException("Not found threadName");
                    }
                    ManageableThread manageableThread;
                    if (threads.containsKey(threadId)) {
                        manageableThread = threads.get(threadId);
                        if (!vObject.toJson().contains(manageableThread.getParammeters().toJson())) {
                            ManageableThread manageableThreadClone = (ManageableThread) manageableThread.clone();
                            try {
                                manageableThreadClone.fillParameter(vObject);

                                notify("Configuration is changed ", threadId, threadName);
                                manageableThread.setParammeters(vObject);
                                manageableThread.reloadConfig();
                            } catch (Exception e) {
                                notify("New Configuration is invalid:  " + ExceptionUtils.toString(e), threadId, threadName);


                            }

                        }
                        getLogger().info("thread ({}:{}) is existed ", threadName, threadId);
                    } else {
                        Class threadClass = Class.forName(vObject.getString(CLASS_NAME));
                        manageableThread = (ManageableThread) threadClass.newInstance();
                        manageableThread.setThreadId(threadId);
                        manageableThread.setThreadManager(ThreadManager.this);
                        manageableThread.setParammeters(vObject);
                        manageableThread.setThreadName(threadName);
                        manageableThread.setAutoActive(new AtomicBoolean(vObject.getBoolean(AUTO_ACTIVE)));
                        threads.put(threadId, manageableThread);
                        getLogger().info("add new thread ({}:{})", threadName, threadId);

                    }
                } catch (Exception e) {
                    try {
                        getLogger().error("load thread {} error", vObject.toJson(), e);
                    } catch (JsonProcessingException e1) {
                        getLogger().error("error",e1);

                    }

                }


            });
        }


    }

    protected void loadThreadInfor(VArray config) {
        config.forEach(o -> {
            try {
                VObject vObject = (VObject) o;
                String threadId = vObject.getString(THREAD_ID);
                if (threadId == null) {
                    throw new NullPointerException("Not found threadId");
                }
                threadInfos.put(threadId, vObject);
            } catch (Exception e) {
                getLogger().error("load thread {} error", o, e);

            }
        });

    }

    @Override
    public synchronized void init() throws Exception {
        logToFile("Loading parameters");
        loadConfig();
        appId = config.getString(APP_ID);
        if (appId == null) {
            throw new NullPointerException("Not found appId");
        }
        serverId = config.getString(SERVER_ID);
        if (serverId == null) {
            throw new NullPointerException("Not found serverId");
        }

        VObject networkCfg = config.getVObject(NETWORK);
        if (networkCfg == null) {
            throw new NullPointerException("Not found networkCfg");
        }
        VObject propertiesCfg = networkCfg.getVObject(PROPERTY);
        if (propertiesCfg == null) {
            throw new NullPointerException("Not found propertiesCfg");
        }
        processorManager.init(this);


        int port = networkCfg.getInteger(PORT);
        String ip = networkCfg.getString(IP);
        mailer = new DisruptorWorkerPool<>();
        WorkerPoolCfg workerPoolCfg = new WorkerPoolCfg();
        workerPoolCfg.setKeepAliveTime(60000);
        workerPoolCfg.setPoolSize(propertiesCfg.getInteger(NMAILER));
        workerPoolCfg.setRingBuferSize(propertiesCfg.getInteger(NQUEUEMAIL));

        SendMsgHandler sendMsgHandler = new SendMsgHandler();
        mailer.setHandleCallback(sendMsgHandler);
        mailer.setHandler(sendMsgHandler);
        mailer.setTTL(-1);

        mailer.init(workerPoolCfg, new ObjectHelper() {
            @Override
            public Object newInstance(Object... params) {
                return new VOMessage();
            }
        });
        mailer.start();

        IConnectionManager<VOMessage, Channel> connectionManager = new DefaultConnectionManager<>();
        IConnectionListenerManager<VOMessage> connectionListenerManager = new DefaultConnectionListenerManager<>();
        IMessageParser<ByteBuf, VOMessage> messageParser = new JsonMessageParser();
        socketServer = new JsonNettyTcpServer(connectionManager, connectionListenerManager, messageParser);
        InetAddress inetAddress = InetAddressByIPv4(IP);
        socketServer.addLocalAddress(inetAddress);
        socketServer.addLocalPort(port);
        socketServer.setNboot(propertiesCfg.getInteger(NBOOT, 1));
        socketServer.setNworker(propertiesCfg.getInteger(NWORKER, 2));
        socketServer.setRcvbuf(100);
        socketServer.setTimeout(propertiesCfg.getInteger(TIMEOUT, 10000));
        socketServer.addConnectionListener("main", new ConnectionListener<VOMessage>() {
            @Override
            public void connectionOpened(IConnection connection) {

            }

            @Override
            public void connectionClosed(IConnection connection) {

            }

            @Override
            public void messageReceived(IConnection connection, VOMessage message) {
                try {
                    VOMessage voMessage = (VOMessage) processorManager.getProcessor(message.getCommand()).process(message);
                    connection.send(voMessage);


                } catch (Exception e) {
                    getLogger().error("process message error ", e);
                }

            }

            @Override
            public void internalError(IConnection connection, VOMessage message, Throwable cause) {

            }
        });

        VObject configLoaderCfg = propertiesCfg.getVObject(CONFIG_LOADER);
        configLoaderClass = Class.forName(configLoaderCfg.getString(CLASS_NAME));
        Constructor<?> ctor = configLoaderClass.getConstructor();
        VObject loaderParameters = configLoaderCfg.getVObject(PARAMETER);
        configStorage = (ConfigStorage) ctor.newInstance();
        configStorage.init(loaderParameters);
        logToFile("Loaded parameters");
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                VObject cfg = null;
                try {
                    cfg = configStorage.load();
                    loadThreadInfor(cfg.getVArray(THREADS));
                    loadThreads(threadInfos.values());
                } catch (Exception e) {
                    getLogger().error("load config error ", e);
                    ThreadManager.this.notify("load config error  " + ExceptionUtils.toString(e), null, null);

                }


            }
        }, 50, 10000, TimeUnit.MILLISECONDS);


    }

    @Override
    public synchronized void start() throws Exception {
        logToFile("starting ...");

        socketServer.init();
        for (ManageableThread manageableThread : threads.values()) {
            try {
                if (manageableThread.getAutoActive().get()) {
                    manageableThread.start();


                }

            } catch (Exception e) {
                getLogger().error("autostart thread {} has error ", manageableThread, e);
            }

        }
        logToFile("started");
    }

    @Override
    public void stop() throws Exception {
        for (ManageableThread manageableThread : threads.values()) {
            try {
                manageableThread.stop();

            } catch (Exception e) {
                getLogger().error("stop thread {} has error ", manageableThread, e);
            }

        }
        try {
            socketServer.destroy();

        } catch (Exception e) {
            getLogger().error("stop socketServer {} has error ", e);
        }
        try {
            scheduledExecutorService.shutdown();
        } catch (Exception e) {
            getLogger().error("stop scheduledExecutorService {} has error ", e);
        }

        try {
            if (mailer != null) {
                mailer.shutdown();
            }
        } catch (Exception e) {
            getLogger().error("stop mailer {} has error ", e);
        }


    }

    @Override
    public void process() throws Exception {

    }

    public class SendMsgHandler implements Handler<VOMessage, VOMessage>, HandleCallback<VOMessage, VOMessage> {

        @Override
        public void onHandleComplete(VOMessage request, VOMessage result) {
            getLogger().info("send message success {} ", request);
        }

        @Override
        public void onHandleError(VOMessage request, Throwable exception, int reason) {
            getLogger().error("send message  {} error ", request, exception);

        }

        @Override
        public VOMessage handle(VOMessage message) throws Exception {
            socketServer.broadcastMessage(message);
            return message;
        }

        @Override
        public VOMessage interop(VOMessage requestParams) throws Exception {
            return requestParams;
        }
    }
}
