package com.example.kaligui;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class SshManager {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Session session;

    public interface ConnectListener {
        void onConnected(String message);
        void onError(String error);
    }

    public interface CommandListener {
        void onOutputUpdate(String output);
        void onCommandFinished();
        void onError(String error);
    }

    public void connect(String username, String password, String ipAddress, ConnectListener listener) {
        executor.execute(() -> {
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(username, ipAddress, 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                handler.post(() -> listener.onConnected("Connected"));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> listener.onError("Connection Failed: " + e.getMessage()));
            }
        });
    }

    public void runCommand(String command, CommandListener listener) {
        if (session == null || !session.isConnected()) {
            listener.onError("Not connected");
            return;
        }
        executor.execute(() -> {
            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                InputStream in = channel.getInputStream();
                channel.connect();

                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        final String outputChunk = new String(tmp, 0, i);
                        handler.post(() -> listener.onOutputUpdate(outputChunk));
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        break;
                    }
                    Thread.sleep(100);
                }
                channel.disconnect();
                handler.post(listener::onCommandFinished);
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> listener.onError("Error: " + e.getMessage()));
            }
        });
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public void disconnect() {
        executor.execute(() -> {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        });
        executor.shutdown();
    }
}