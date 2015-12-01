package com.drey.test.websocketdemo;

/**
 * Created by drey on 20.11.2015.
 */

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by drey on 01.11.2015.
 */
public class EternalFragment extends Fragment {

    public MyWebsocketClient client;
    public Bitmap imagePreview;
    public Bitmap receivedImage;
    public Uri imageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

}