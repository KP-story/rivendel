package com.kp.taskmanager.thread;

import com.kp.async.ASyncManageableObject;
import com.kp.common.data.vo.VObject;
import com.kp.common.log.Loggable;
import com.kp.common.utilities.ExceptionUtils;
import com.kp.network.connection.IConnection;
import com.kp.taskmanager.manager.IThreadManager;
import com.kp.taskmanager.network.codec.VOMessage;
import com.kp.taskmanager.network.constant.Commands;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.kp.common.constant.FieldConstant.*;

public abstract class ManageableThread extends ParameterLoader implements Cloneable, ASyncManageableObject<ManageableThread>, Loggable, Runnable {
    public final static int RESTART = 1;
    public final static int FORCE_STOP = 2;
    public static int FINISHED = 1;
    public static int STARTING = 2;
    public static int PROCESSING = 3;
    public static int ERORR = 4;
    public static int ACTIVE = 5;
    public static int INACTIVE = 6;
    public static int LOAD_CONFIG = 4;
    protected IThreadManager threadManager;
    protected AtomicInteger status = new AtomicInteger(INACTIVE);
    protected String threadId;
    protected VObject parammeters = new VObject();
    protected VOMessage updatestateMessage = new VOMessage(Commands.UPDATE_STATE);
    protected VOMessage notifyMessage = new VOMessage(Commands.NOTIFY);
    protected String threadName;
    protected CompletableFuture<ManageableThread> startFuture;
    protected CompletableFuture<ManageableThread> stopFuture;
    protected AtomicInteger command = new AtomicInteger();
    private long sleepTime = 1000;
    private Thread mthrMain;
    private AtomicBoolean autoActive = new AtomicBoolean(false);
    private Lock lock = new ReentrantLock();

    public int getStatus() {
        return status.get();
    }

    public AtomicBoolean getAutoActive() {
        return autoActive;
    }

    public void setAutoActive(AtomicBoolean autoActive) {
        this.autoActive = autoActive;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public abstract void reloadConfig();

    @Override
    public String toString() {
        return "ManageableThread{" +
                "status=" + status +
                ", threadId='" + threadId + '\'' +
                ", threadName='" + threadName + '\'' +
                '}';
    }


    public void setThreadManager(IThreadManager manager) {
        this.threadManager = manager;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void logToFile(Object log) {
        threadManager.logToFile(threadId, log);
    }

    public void logMonitor(Object log) {
        threadManager.logToMonitor(log);
    }

    public void logMonitor(String log) {

        notify(log);
    }

    public VObject getParammeters() {
        return parammeters;
    }

    public void setParammeters(VObject parammeters) {
        this.parammeters = parammeters;
    }

    @Override
    public Future<ManageableThread> start() throws Exception {
        notify("call start");
        if (stopFuture != null && !stopFuture.isCancelled() && !stopFuture.isDone() && !stopFuture.isCompletedExceptionally()) {

            stopFuture.get(sleepTime + 4000, TimeUnit.MILLISECONDS);
        }
        if (startFuture != null) {
            try {
                startFuture.get(sleepTime, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                getLogger().error("wait previous start action faild", e);
            }
            startFuture.cancel(true);
        }

        startFuture = new CompletableFuture<>();
        command.set(STARTING);
        if (getStatus() == INACTIVE) {
            this.mthrMain = new Thread(this);
            this.mthrMain.setName(this.getThreadName() + ":" + this.getThreadId());
            this.mthrMain.start();
        } else {
            startFuture.complete(this);
        }

        return startFuture;


    }

    public abstract void messageReceived(VOMessage voMessage, IConnection iConnection);


    @Override
    public Future<ManageableThread> stop() throws Exception {
        if (startFuture != null && !startFuture.isCancelled() && !startFuture.isDone() && !startFuture.isCompletedExceptionally()) {

            startFuture.get(sleepTime + 4000, TimeUnit.MILLISECONDS);
        }
        if (stopFuture != null) {
            try {
                stopFuture.get(sleepTime, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                getLogger().error("wait previous stop action faild", e);
            }

            stopFuture.cancel(true);
        }
        stopFuture = new CompletableFuture<>();
        if (getStatus() == INACTIVE) {
            stopFuture.complete(this);
        }
        notify("call stop");
        command.set(FINISHED);
        this.getLogger().info("thread{} call Stop ", this);
        return stopFuture;
    }

    public void changeState(int retryTimeout, int action) throws Exception {
        try {
            lock.tryLock(retryTimeout, TimeUnit.MILLISECONDS);

            switch (action) {
                case RESTART: {
                    stop();
                    start();
                    break;
                }

                case FORCE_STOP: {
                    forceStop();
                    break;

                }

                default: {
                    stop();
                    if (status.get() == INACTIVE) {
                        start();
                    }
                }


            }


        } finally {
            lock.unlock();
        }


    }

    public void forceStop() throws Exception {
        stop();

        if (this.mthrMain != null) {

            try {
                this.mthrMain.interrupt();
                this.mthrMain.join(30000L);
            } catch (Exception e) {
                notify("Cannot destroy thread \n" + ExceptionUtils.toString(e));

                this.getLogger().error("Error when stop thread", e);
                return;
            }
        }
        this.getLogger().info("thread{} call froceStop ", this);

    }


    protected void notify(String msg) {
        notifyMessage.setTransId(threadManager.genTransId(threadId));
        notifyMessage.put(THREAD_ID, getThreadId());
        notifyMessage.put(THREAD_NAME, getThreadName());
        notifyMessage.put(MESSAGE, msg);
        logMonitor(notifyMessage);
        this.getLogger().info("notifyMessage", notifyMessage);

    }

    protected void notifyUpdateState(int state) {
        updatestateMessage.setTransId(threadManager.genTransId(threadId));
        updatestateMessage.put(THREAD_ID, getThreadId());
        updatestateMessage.put(THREAD_NAME, getThreadName());
        updatestateMessage.put(STATE, state);
        logMonitor(updatestateMessage);
        this.getLogger().info("notifyUpdateState", updatestateMessage);

    }

    private void loadConfig() throws Exception {
        notify("call load config");
        fillParameter(parammeters);
    }

    @Override
    public VObject getParameterDefinition() {
        VObject vObject = new VObject();
        return vObject;
    }

    @Override
    public void validateParameter() throws Exception {
        this.fillParameter(parammeters);
    }

    @Override
    public void fillParameter(VObject vObject) throws Exception {
        sleepTime = vObject.getLong(DELAY_TIME);
    }

    @Override
    public void run() {
        status.set(ACTIVE);
        notifyUpdateState(ACTIVE);
        if (startFuture != null) {
            startFuture.complete(this);
        }
        while (command.get() != FINISHED) {
            try {
                status.set(LOAD_CONFIG);
                notifyUpdateState(LOAD_CONFIG);
                loadConfig();
                status.set(STARTING);
                notifyUpdateState(STARTING);
                init();
                status.set(PROCESSING);
                notifyUpdateState(PROCESSING);
                process();
                status.set(FINISHED);
                notifyUpdateState(FINISHED);
            } catch (Exception e) {
                getLogger().error("error in thread id {} name {} ", threadId, threadName, e);
                notifyUpdateState(ERORR);

                notify(ExceptionUtils.toString(e));
                logMonitor(e);
            } finally {
                try {
                    Thread.sleep(sleepTime);

                } catch (Exception e) {
                    getLogger().error("error in thread id {} name {} ", threadId, threadName, e);

                }
            }


        }
        status.set(INACTIVE);
        notifyUpdateState(INACTIVE);
        if (stopFuture != null) {
            stopFuture.complete(this);
        }
    }


}
