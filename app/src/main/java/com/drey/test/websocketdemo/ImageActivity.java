package com.drey.test.websocketdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * Created by drey on 24.11.2015.
 */
public class ImageActivity extends AppCompatActivity {
    public static final String KEY_IMAGE = "image";
    private ImageView _image;
    private Long _key = -1l;
    private WebSocketService _service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _image = (ImageView) findViewById(R.id.image);
        if (getIntent().hasExtra(KEY_IMAGE)) {
            _key = getIntent().getLongExtra(KEY_IMAGE, -1);
        }
    }

    ServiceConnection _conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _service = ((WebSocketService.MyBinder) service).getService();
            _image.setImageBitmap(_service.getImage(_key));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if ( _service == null && _key > 0) {
            Intent i = new Intent(this, WebSocketService.class);
            bindService(i, _conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_service != null) {
            _service.RemoveImage(_key);
            unbindService(_conn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            _image.setDrawingCacheEnabled(true);
            Bitmap b = _image.getDrawingCache();
            MediaStore.Images.Media.insertImage(getContentResolver(), b, null, null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
