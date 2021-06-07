package com.coursework.qrcodescanner;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(MainActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                        showAlertDialog(result);
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public void showAlertDialog(@NonNull final Result result) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Распознано");
        alert.setMessage(result.getText());
        alert.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

//                Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_LONG).show();
                writeFileSD(result);

            }
        });
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Не добавлено", Toast.LENGTH_LONG).show();
            }
        });
        alert.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    final String LOG_TAG = "myLogs";
    @SuppressLint("SimpleDateFormat")
    final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    final String DIR_SD = "Отчёты";

    void writeFileSD(Result result) {
        final String currentDate = df.format(new Date());
        final String FILENAME = "Отчёт_" + currentDate + ".txt";

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }

        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        sdPath.mkdir();

        File sdFile = new File(sdPath, FILENAME);

        try {

            FileWriter writer = new FileWriter(sdFile, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(result.getText()+" "+currentDate+"\n");
            bufferWriter.close();

            Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
            Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.files_settings:
                Intent intent = new Intent(this, FilesActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
