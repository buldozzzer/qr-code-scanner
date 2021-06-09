package com.coursework.qrcodescanner;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import butterknife.BindView;

import static org.apache.commons.codec.CharEncoding.US_ASCII;
import static org.apache.commons.codec.CharEncoding.UTF_8;

public class LocationsActivity extends ListActivity implements AdapterView.OnItemLongClickListener {
    private ArrayAdapter<String> mAdapter;
    String filename = "locations.txt";
    ArrayList<String> rows = new ArrayList<>();
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
                    Log.d("my_los", String.valueOf(rename));
                    Log.d("my_los", String.valueOf(delete));
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
                    try{
                        if(fos!=null)
                            fos.close();
                        if(isr!=null)
                            isr.close();
                    }
                    catch(IOException ex){

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


    void fileReader (){
        try {
            FileInputStream in = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
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
        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_activity, null);
        alert.setView(view);
        View finalView = view;
        alert.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FileOutputStream fos = null;
                try {
                    object = (EditText) finalView.findViewById(R.id.object);
                    corpus = (EditText) finalView.findViewById(R.id.corpus);
                    cabinet = (EditText) finalView.findViewById(R.id.cabinet);
                    String resultLine = object.getText().toString()+'_'+corpus.getText().toString()+'_'+cabinet.getText().toString()+'\n';
                    fos = openFileOutput(filename, Context.MODE_APPEND);
                    Log.d("my_logs", resultLine);
                    fos.write(resultLine.getBytes());
                    mAdapter.add(resultLine);
                    mAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try{
                        if(fos!=null)
                            fos.close();
                    }
                    catch(IOException ex){

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
    }
}
