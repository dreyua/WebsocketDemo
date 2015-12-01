package com.drey.test.websocketdemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by drey on 19.11.2015.
 */
public class WebSocketService extends Service {

    public final IBinder _binder = new MyBinder();

    private static MyWebsocketServer _ws;

    private Handler _handler;

    public WebSocketService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (_ws == null) {
            InetSocketAddress inetSockAddress = new InetSocketAddress(5555);
            _ws = new MyWebsocketServer(inetSockAddress, getApplicationContext());
            _ws.start();
            Log.d("TEST", "ws started");
        }
        return _binder;
    }


    protected void onHandleIntent(Intent intent) {
    }



    public class MyBinder extends Binder {
        WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    public void setCallback(Handler cb) {
        _handler = cb;
        if (_ws != null) {
            _ws.setCallback(cb);
        }
    }

    public void decodeImage(final Uri imageURI) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream imageStream = null;
                try {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(getContentResolver().openInputStream(imageURI), null, opt);
                    opt.inSampleSize = Util.calculateInSampleSize(opt, MainActivity.PREVIEW_WIDTH, MainActivity.PREVIEW_HEIGHT);
                    opt.inJustDecodeBounds = false;
                    // Log.d("size", ""+opt.inSampleSize);
                    Bitmap bt = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageURI), null, opt);
                    if (WebSocketService.this._handler != null) {
                        Message m = Message.obtain(WebSocketService.this._handler, MainActivity.ACTION_PREVIEW_LOADED);
                        m.obj = bt;
                        Bundle b = new Bundle();
                        b.putString(MainActivity.TAG_URI, imageURI.toString());
                        m.setData(b);
                        WebSocketService.this._handler.sendMessage(m);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void sendImage(final Uri imageUri, final MyWebsocketClient client) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client.getConnection().isOpen()) {
                        InputStream s = getContentResolver().openInputStream(imageUri);
                        byte[] bytes = new byte[s.available()];
                        s.read(bytes);
                        ByteBuffer bb = ByteBuffer.wrap(bytes);
                        client.getConnection().send(bb);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public Bitmap getImage(Long key){
        return _ws!=null?_ws.getImage(key):null;
    }

    public void RemoveImage(Long key) {
        if (_ws != null){
            _ws.removeImage(key);
        }
    }

}
