package com.drey.test.websocketdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    public static final int ACTION_APPEND_LOG = 1;
    public static final int ACTION_WS_OPEN = 2;
    public static final int ACTION_WS_CLOSE = 3;
    public static final int ACTION_PREVIEW_LOADED = 4;
    public static final String TAG_URI = "uri";
    private static final String DATA_FRAG = "data";
    private static final int SELECT_PICTURE = 1;



    public static int PREVIEW_WIDTH =100, PREVIEW_HEIGHT = 100;

    private TextView _txt;
    private View _connect;
    private View _send;
    private TextView _host;
    private TextView _port;
    private TextView _msg;
    private View _chooseImage;
    private View _sendImage;
    private ImageView _image;

    EternalFragment data;
    WebSocketService _service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = (EternalFragment) getSupportFragmentManager().findFragmentByTag(DATA_FRAG);
        if (data == null) {
            data = new EternalFragment();
            getSupportFragmentManager().beginTransaction().add(data, DATA_FRAG).commit();
            Intent i = new Intent(this, WebSocketService.class);
            startService(i);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _txt = (TextView) findViewById(R.id.txt);
        _host = (TextView) findViewById(R.id.host);
        _port = (TextView) findViewById(R.id.port);
        _msg = (TextView) findViewById(R.id.message);
        _connect = findViewById(R.id.conect);
        _send = findViewById(R.id.send);
        _chooseImage = findViewById(R.id.choose_image);
        _sendImage = findViewById(R.id.send_image);
        _image = (ImageView) findViewById(R.id.picture);
        if (savedInstanceState != null){
            _image.setImageBitmap(data.imagePreview);
            if (data.client != null && data.client.getConnection() != null && data.client.getConnection().isOpen()) {
                enableButtons(true);
            }
        }


        _connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.client != null) {
                    data.client.close();
                }
                data.client = new MyWebsocketClient(URI.create(_host.getText().toString() + ":" + _port.getText().toString()), _handler);
                data.client.connect();
            }
        });

        _send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.client != null && data.client.getReadyState() != 0 && !_msg.getText().toString().equals("")) {
                    data.client.send(_msg.getText().toString());
                }
            }
        });

        _chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

        _sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.client != null && data.client.getReadyState() != 0 && data.imageUri != null) {
                    _service.sendImage(data.imageUri, data.client);
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(this, WebSocketService.class);
        bindService(i, _conn, Context.BIND_AUTO_CREATE);
        if (data.client != null) {
            data.client._handler = _handler;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(_conn);
    }


    Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_APPEND_LOG:
                    _txt.append(msg.getData().getString("text") + "\n");
                    break;
                case ACTION_WS_OPEN:
                    enableButtons(true);
                    break;
                case ACTION_WS_CLOSE:
                    _txt.setText("");
                    enableButtons(false);
                    break;
                case ACTION_PREVIEW_LOADED:
                    if (msg.obj instanceof Bitmap){
                        MainActivity.this.data.imageUri =  Uri.parse(msg.getData().getString(TAG_URI));
                        MainActivity.this.data.imagePreview = (Bitmap) msg.obj;
                        _image.setImageBitmap(MainActivity.this.data.imagePreview);
                    }

                    enableButtons(true);
                    break;
            }
        }
    };

    ServiceConnection _conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _service = ((WebSocketService.MyBinder) service).getService();
            _service.setCallback(_handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _service = null;
        }
    };

    private void enableButtons(boolean enable) {
        _send.setEnabled(enable);
        _chooseImage.setEnabled(enable);
        _sendImage.setEnabled(enable && _image.getDrawable() != null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PICTURE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    _service.decodeImage(selectedImage);
                }
                break;
        }
    }
}
