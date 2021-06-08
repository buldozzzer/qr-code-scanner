package com.coursework.qrcodescanner;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class ContentActivity extends ListActivity implements AdapterView.OnItemLongClickListener {
    private ArrayAdapter<String> mAdapter;
    String name;
    ArrayList<String> rows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity);
        ListView lv = (ListView) findViewById(android.R.id.list);
        View empty = findViewById(android.R.id.empty);
        Bundle arguments = getIntent().getExtras();
        name = arguments.get("filename").toString();
        fileReader(name);
        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rows);
        lv.setAdapter(mAdapter);
        lv.setEmptyView(empty);
        getListView().setOnItemLongClickListener(this);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Удалить?");
        alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                File sourceFile = new File(name);
                File sdPath = Environment.getExternalStorageDirectory();
                File outputFile = new File(sdPath.getAbsolutePath() + "/Отчёты/tmp.txt");
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(sourceFile));

                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.equals(selectedItem)) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                    reader.close();
                    writer.close();
                    boolean delete = sourceFile.delete();
                    boolean rename = outputFile.renameTo(sourceFile);

                    if (delete && rename) {
                        mAdapter.remove(selectedItem);
                        mAdapter.notifyDataSetChanged();

                        Toast.makeText(getApplicationContext(),
                                "Запись " + selectedItem + " удалёна",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Ошибка",
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (
                        IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ContentActivity.this, "Отменено", Toast.LENGTH_LONG).show();
            }
        });
        alert.create().show();

        return true;
    }

    void fileReader (String filename){
        final String LOG_TAG = "myLogs";
        try {
            File file = new File(filename);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            while (line != null) {
                rows.add(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex){

            Log.d(LOG_TAG, ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
