package com.drey.test.websocketdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by drey on 20.11.2015.
 */
public class MyWebsocketClient extends WebSocketClient {

    Handler _handler;

    public MyWebsocketClient(URI serverURI, Handler cb) {
        super(serverURI);
        _handler = cb;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        if(_handler != null){
            _handler.sendMessage(Message.obtain(_handler, MainActivity.ACTION_WS_OPEN));
            appendLog("HTTP " + serverHandshake.getHttpStatus() + " "+ serverHandshake.getHttpStatusMessage(), _handler);
        }
    }

    @Override
    public void onMessage(String s) {
        if (_handler != null) {
            appendLog( "[response]: " + s, _handler);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        if(_handler != null) {
            _handler.sendMessage(Message.obtain(_handler, MainActivity.ACTION_WS_CLOSE));
        }
    }

    @Override
    public void onError(Exception e) {
        if(_handler != null){
            appendLog(e.getMessage(), _handler);
        }
    }

    public static void appendLog(String s, Handler h) {
        Message m = Message.obtain(h, MainActivity.ACTION_APPEND_LOG);
        Bundle b = new Bundle();
        b.putString("text", s);
        m.setData(b);
        h.sendMessage(m);
    }



}
