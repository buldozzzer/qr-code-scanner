package com.coursework.qrcodescanner;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class LocationsActivity extends ListActivity implements AdapterView.OnItemLongClickListener {
    private ArrayAdapter<String> mAdapter;
    String filename = "locations.txt";
    ArrayList<String> rows = new ArrayList<>();
    HashSet<String> rows_uniq = new HashSet<>();
    EditText object;
    EditText corpus;
    EditText cabinet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations_activity);
        ListView lv = (ListView) findViewById(android.R.id.list);
        View empty = findViewById(android.R.id.empty);
        fileReader();
        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rows);
        lv.setAdapter(mAdapter);
        lv.setEmptyView(empty);
        getListView().setOnItemLongClickListener(this);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Удалить?");
        alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedString = parent.getItemAtPosition(position).toString();
                FileOutputStream fos = null;
                InputStreamReader isr = null;
                try {
                    fos = openFileOutput("tmp.txt", Context.MODE_APPEND);
                    mAdapter.remove(selectedString);
                    mAdapter.notifyDataSetChanged();
                    FileInputStream ins = openFileInput(filename);
                    isr = new InputStreamReader(ins);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.equals(selectedString)) {
                            fos.write(line.getBytes());
                            Log.d("my_los", line);
                            break;
                        }
                    }
                    isr.close();
                    boolean delete = getFileStreamPath(filename).delete();
                    boolean rename = getFileStreamPath("tmp.txt").renameTo(getFileStreamPath(filename));
                    if (delete && rename) {
                        mAdapter.remove(selectedString);
                        mAdapter.notifyDataSetChanged();

                        Toast.makeText(getApplicationContext(),
                                "Запись " + selectedString + " удалёна",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Ошибка",
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (
                        IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                        if (isr != null)
                            isr.close();
                    } catch (IOException ex) {

                        Toast.makeText(LocationsActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(LocationsActivity.this, "Отменено", Toast.LENGTH_LONG).show();
            }
        });
        alert.create().show();

        return true;
    }


    void fileReader() {
        try {
            FileInputStream in = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                rows_uniq.add(line.trim());
                rows.add(line.trim());
            }
            inputStreamReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressLint("InflateParams")
    public void changeList(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Добавить локацию");
        alert.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_activity, null);
        alert.setView(view);
        View finalView = view;
        alert.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(LocationsActivity.this, "Отменено", Toast.LENGTH_LONG).show();
            }
        });
        final AlertDialog dialog = alert.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream fos = null;
                try {
                    object = (EditText) finalView.findViewById(R.id.object);
                    corpus = (EditText) finalView.findViewById(R.id.corpus);
                    cabinet = (EditText) finalView.findViewById(R.id.cabinet);
                    String resultLine = object.getText().toString().trim() + '_' + corpus.getText().toString().trim() + '_' + cabinet.getText().toString().trim() + '\n';
                    fos = openFileOutput(filename, Context.MODE_APPEND);
                    Log.d("my_logs", resultLine);
                    if (!resultLine.equals("__\n")) {
                        if (rows_uniq.add(resultLine)) {
                            fos.write(resultLine.getBytes());
                            mAdapter.add(resultLine);
                            mAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(LocationsActivity.this, "Уже существует", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LocationsActivity.this, "Запись не может быть пустой", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException ex) {

                        Toast.makeText(LocationsActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
