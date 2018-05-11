package com.ramzi.messanger.ui;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;
import com.ramzi.messanger.R;
import com.ramzi.messanger.utils.ImageUtil;
import com.ramzi.messanger.utils.SharedPref;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImageViewer extends AppCompatActivity {

    private PhotoView imageViewer;
    private FloatingActionButton downloadButton;
    private Bitmap bitmap;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        imageViewer = findViewById(R.id.imageView);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        getSupportActionBar().setTitle(bundle.getString("title"));
        Single.create(emitter -> {
            SharedPref sharedPref = new SharedPref(this);
            String ss = sharedPref.readImage();
            Bitmap bitmap = ImageUtil.convertFromBase64(ss);
            emitter.onSuccess(bitmap);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    imageViewer.setImageBitmap((Bitmap) o);
                });
        imageViewer.setImageBitmap(bitmap);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            this.finish();
        return true;
    }
}
