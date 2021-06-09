package com.coursework.qrcodescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private HashSet<String> results = new HashSet<>();
    private static final int PERMISSION_REQUEST_CODE = 200;
    private List<String> listPermissionsNeeded = new ArrayList<>();
    private final String LOG_TAG = "myLogs";
    @SuppressLint("SimpleDateFormat")
    private final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    private final String DIR_SD = "Отчёты";
    private String location = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermissions()) {
            CodeScannerView scannerView = findViewById(R.id.scanner_view);
            mCodeScanner = new CodeScanner(this, scannerView);
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
            EditText location_tw = findViewById(R.id.location_view);
            location_tw.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    onCreateLocationDialog();
                    return true;
                }
            });
            location_tw.
                    setOnKeyListener(new View.OnKeyListener() {
                                         @Override
                                         public boolean onKey(View v, int keyCode, KeyEvent event) {
                                             if (event.getAction() == KeyEvent.ACTION_DOWN &&
                                                     (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                                 if (location_tw.getText().toString().equals("")) {
                                                     Toast.makeText(getApplicationContext(),
                                                             "Не может быть пустым", Toast.LENGTH_SHORT).show();
                                                     location_tw.setText(R.string.location_text);
                                                     location = String.valueOf(R.string.location_text);
                                                 } else {
                                                     location = location_tw.getText().toString();
                                                 }
                                                 return true;
                                             }
                                             return false;
                                         }
                                     }
                    );
        } else {
            requestPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if(mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

    public void showAlertDialog(@NonNull final Result result) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Распознано");
        alert.setMessage(result.getText());
        alert.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
            if (results.add(result.getText())) {
                FileWriter writer = new FileWriter(sdFile, true);
                BufferedWriter bufferWriter = new BufferedWriter(writer);
                bufferWriter.write(result.getText() + " " + currentDate + "\n");
                bufferWriter.close();

                Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
                Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Уже существует", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.files_settings:
                Intent intent1 = new Intent(this, FilesActivity.class);
                startActivity(intent1);
                return true;
            case R.id.locations_settings:
                Intent intent2 = new Intent(this, LocationsActivity.class);
                startActivity(intent2);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] test_data = {"red", "green", "blue"};
        builder.setTitle("Локации")
                .setItems(test_data, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText location_tw = findViewById(R.id.location_view);
                        location_tw.setText(test_data[which]);
                        location_tw.setTextSize(18);
                    }
                });
        builder.create().show();
    }

    private boolean checkPermissions() {
        boolean camera = true;
        boolean write_external_storage = true;
        int permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
            camera = false;
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            write_external_storage = false;
        }
        return camera && write_external_storage;
    }

    private void requestPermissions() {

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.
                    toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Разрешения получены", Toast.LENGTH_SHORT).show();
                    CodeScannerView scannerView = findViewById(R.id.scanner_view);
                    mCodeScanner = new CodeScanner(this, scannerView);
                    mCodeScanner.setDecodeCallback(new DecodeCallback() {
                        @Override
                        public void onDecoded(@NonNull final Result result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
                } else {
                    Toast.makeText(getApplicationContext(), "Отказано в доступе", Toast.LENGTH_SHORT).show();
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        showMessageOKCancel(
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions();
                                    }
                                });
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Вам необходимо разрешить права доступа")
                .setPositiveButton("Разрешить", okListener)
                .setNegativeButton("Закрыть", null)
                .create()
                .show();
    }
}
