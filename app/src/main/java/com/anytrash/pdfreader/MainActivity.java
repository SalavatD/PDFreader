package com.anytrash.pdfreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int GET_CONTENT_RESULT_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 101;

    ListView documentListView;
    ArrayList<String> documentList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        documentListView = findViewById(R.id.document_list);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, documentList);
        documentListView.setAdapter(arrayAdapter);
        documentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                TextView textView = (TextView) itemClicked;
                String filePath = textView.getText().toString();
                Intent intent = new Intent(getApplicationContext(), ReaderActivity.class);
                intent.putExtra("filePath", filePath);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermission()) getContent();
                } else {
                    getContent();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_CONTENT_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(getApplicationContext(), ReaderActivity.class);
                String filePath = data.getData().getLastPathSegment().substring(data.getData().getLastPathSegment().indexOf(':') + 1);
                if (!documentList.contains(filePath)) {
                    documentList.add(filePath);
                    arrayAdapter.notifyDataSetChanged();
                }
                intent.putExtra("filePath", filePath);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContent();
            }
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void getContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, GET_CONTENT_RESULT_CODE);
    }
}
