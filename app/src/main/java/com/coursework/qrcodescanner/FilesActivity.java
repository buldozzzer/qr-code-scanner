package com.coursework.qrcodescanner;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilesActivity extends ListActivity implements AdapterView.OnItemLongClickListener {
    private ArrayAdapter<String> mAdapter;
    ArrayList<String> names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_activity);
        getFileList();
        ListView lv = (ListView) findViewById(android.R.id.list);
        View empty = findViewById(android.R.id.empty);
        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        lv.setAdapter(mAdapter);
        lv.setEmptyView(empty);
        getListView().setOnItemLongClickListener(this);

    }

    private void getFileList() {
        File sdPath = Environment.getExternalStorageDirectory();
        File dir = new File(sdPath.getAbsolutePath() + "/Отчёты");
        File[] arrFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".txt");
            }
        });
        if (arrFiles != null) {
            for (File file :
                    arrFiles) {
                names.add(file.getName());
            }
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        File sdPath = Environment.getExternalStorageDirectory();
        String path = sdPath.getAbsolutePath() + "/Отчёты/" +
                l.getItemAtPosition(position).toString();
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra("filename", path);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Удалить?");
        alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                File sdPath = Environment.getExternalStorageDirectory();
                File file = new File(sdPath.getAbsolutePath() + "/Отчёты/" + selectedItem);
                boolean result = file.delete();
                if (result) {
                    mAdapter.remove(selectedItem);
                    mAdapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(),
                            "Файл " + selectedItem + " удалён",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Ошибка",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FilesActivity.this, "Отменено", Toast.LENGTH_LONG).show();
            }
        });
        alert.create().show();

        return true;
    }
}
