package com.drey.test.websocketdemo;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by drey on 19.11.2015.
 */
public class MyWebsocketServer extends WebSocketServer {
    static final String TAG = "WS";
    private Handler _handler;
    private Context _cnt;
    private HashMap<Long, Bitmap> _images = new HashMap<Long, Bitmap>();

    public MyWebsocketServer(InetSocketAddress address, Context cnt) {
        super(address);
        _cnt = cnt;
        Log.d(TAG, "create");
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        Log.d(TAG, "onOpen");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        Log.d(TAG, "onClose");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        webSocket.send(s.equals("one") ? "two" : s);
        if (_handler != null) {
            MyWebsocketClient.appendLog("[in]: " + s, _handler);
        }
        Log.d(TAG, "onMessage: " + s);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        StringBuilder sb = new StringBuilder("Image received (")
                .append(Formatter.formatFileSize(_cnt, message.remaining()))
                .append(")");
        Log.d(TAG, sb.toString());
        Long key = Calendar.getInstance().getTimeInMillis();
        _images.put(key, BitmapFactory.decodeByteArray(message.array(), 0, message.array().length));
        if (_cnt != null) {
            Intent i = new Intent(_cnt, ImageActivity.class);
            i.putExtra(ImageActivity.KEY_IMAGE, key);
            PendingIntent pi = PendingIntent.getActivity(_cnt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(_cnt)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setTicker(sb.toString())
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle(_cnt.getResources().getString(R.string.app_name))
                    .setContentText(sb.toString());

            int id = 001;
            NotificationManager mgr = (NotificationManager) _cnt.getSystemService(Activity.NOTIFICATION_SERVICE);
            mgr.cancel(id);
            mgr.notify(id, builder.build());
        }

    }


    @Override
    public void onError(WebSocket webSocket, Exception e) {
        Log.d(TAG, "onError: " + e.getMessage());
    }

    public void setCallback(Handler cb) {
        _handler = cb;
    }

    public Bitmap getImage(Long key){return _images.get(key);}

    public void removeImage(Long key) {
        _images.remove(key);
    }
}
