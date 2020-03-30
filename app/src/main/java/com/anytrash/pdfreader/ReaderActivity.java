package com.anytrash.pdfreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class ReaderActivity extends AppCompatActivity {
    private String filePath;
    private int currentZoomLevel = 5;
    private int currentPage = 0;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page pageIndex;
    private ImageView pdfView;
    private ImageButton buttonBack;
    private ImageButton buttonNext;
    private ImageButton buttonZoomIn;
    private ImageButton buttonZoomOut;
    private ImageButton buttonFullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        filePath = getIntent().getStringExtra("filePath");
        setTitle(filePath);
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("pageIndex", 0);
        }
        pdfView = findViewById(R.id.pdfView);
        buttonBack = findViewById(R.id.button_back);
        buttonNext = findViewById(R.id.button_next);
        buttonZoomIn = findViewById(R.id.button_zoom_in);
        buttonZoomOut = findViewById(R.id.button_zoom_out);
        buttonFullscreen = findViewById(R.id.button_fullscreen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        File file = new File(Environment.getExternalStorageDirectory() + "/" + filePath);
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            displayPage(currentPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pageIndex != null) {
            outState.putInt("pageIndex", pageIndex.getIndex());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pageIndex != null) pageIndex.close();
        if (pdfRenderer != null) pdfRenderer.close();
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        if (pageIndex != null) {
            pageIndex.close();
        }
        pageIndex = pdfRenderer.openPage(index);
        int newWidth = getResources().getDisplayMetrics().widthPixels * pageIndex.getWidth() / 72 * currentZoomLevel / 40;
        int newHeight = getResources().getDisplayMetrics().heightPixels * pageIndex.getHeight() / 72 * currentZoomLevel / 64;
        Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        float dpiAdjustedZoomLevel = (float) currentZoomLevel * DisplayMetrics.DENSITY_MEDIUM / getResources().getDisplayMetrics().densityDpi;
        matrix.setScale(dpiAdjustedZoomLevel, dpiAdjustedZoomLevel);
        pageIndex.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pdfView.setImageBitmap(bitmap);
        int pageCount = pdfRenderer.getPageCount();
        buttonBack.setEnabled(index > 0);
        buttonNext.setEnabled(index < pageCount - 1);
        buttonZoomOut.setEnabled(currentZoomLevel > 5);
        buttonZoomIn.setEnabled(currentZoomLevel < 10);
    }

    public void getBackPage(View view) {
        displayPage(pageIndex.getIndex() - 1);
    }

    public void getNextPage(View view) {
        displayPage(pageIndex.getIndex() + 1);
    }

    public void getZoomIn(View view) {
        currentZoomLevel++;
        displayPage(pageIndex.getIndex());
    }

    public void getZoomOut(View view) {
        currentZoomLevel--;
        displayPage(pageIndex.getIndex());
    }

    public void changeFullscreenMode(View view) {
        if (getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            buttonFullscreen.setImageResource(R.drawable.ic_fullscreen_exit_black_24dp);
        } else {
            getSupportActionBar().show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            buttonFullscreen.setImageResource(R.drawable.ic_fullscreen_black_24dp);
        }
    }
}
