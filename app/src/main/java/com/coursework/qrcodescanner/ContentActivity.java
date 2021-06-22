package com.coursework.qrcodescanner;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.Calendar;

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
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Изменить запись");
        alert.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_row_dialog_activity, null);
        alert.setView(view);

        alert.setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ContentActivity.this, "Отменено", Toast.LENGTH_LONG).show();
            }
        });
        final AlertDialog dialog = alert.create();
        String[] row = l.getItemAtPosition(position).toString().split(" ");

        EditText inv_n = (EditText) view.findViewById(R.id.inv_n);
        inv_n.setText(row[0].substring(4));

        EditText ser_n = (EditText) view.findViewById(R.id.ser_n);
        //все индексы сдвинуть на 1, когда серийник появится
        ser_n.setText(row[1].substring(4));

        EditText date = (EditText) view.findViewById(R.id.date);
        date.setText(row[2]);
        new DateInputMask(date);

        EditText object = (EditText) view.findViewById(R.id.object);
        EditText corpus = (EditText) view.findViewById(R.id.corpus);
        EditText cabinet = (EditText) view.findViewById(R.id.cabinet);

        object.setText(row[3].substring(3));
        corpus.setText(row[4].substring(5));
        if(!row[5].substring(4).equals("\n")) {
            cabinet.setText(row[5].substring(4).trim());
        }else{
            cabinet.setText("");
        }

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checkInv = false;
                boolean checkSer = false;
                boolean checkDate = false;
                if(!inv_n.getText().toString().equals("")){
                    checkInv = true;
                }
                if(!ser_n.getText().toString().equals("")){
                    checkSer = true;
                }
                if(!date.getText().toString().equals("")){
                    checkDate = true;
                }
                if(checkDate && checkInv && checkSer){

                    String newStr = "Инв:" + inv_n.getText().toString().trim() +
                            " Сер:" + ser_n.getText().toString().trim() + ' ' +
                            date.getText().toString().trim() + " Об:" +
                            object.getText().toString().trim() + " Корп:" +
                            corpus.getText().toString().trim() + " Каб:" +
                            cabinet.getText().toString().trim() + '\n';

                    rewriteFile(l.getItemAtPosition(position).toString(), newStr);
                    Toast.makeText(ContentActivity.this, "Все верно", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ContentActivity.this, "Поля не могут быть пустыми", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Удалить?");
        alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                deleteRow(selectedItem);
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
                if(!line.equals("")) {
                    rows.add(line);
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex){

            Log.d(LOG_TAG, ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void deleteRow(String selectedItem){
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
    void rewriteFile(String oldItem, String newItem){
        File sourceFile = new File(name);
        File sdPath = Environment.getExternalStorageDirectory();
        File outputFile = new File(sdPath.getAbsolutePath() + "/Отчёты/tmp.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourceFile));

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("String", "Строка:" + line);
                if (!line.equals(oldItem)) {
                    if (!line.equals("")) {
                        writer.write(line);
                    }
                } else {
                    writer.write(newItem);
                }
                writer.newLine();
            }
            reader.close();
            writer.close();
            boolean delete = sourceFile.delete();
            boolean rename = outputFile.renameTo(sourceFile);

            if (delete && rename) {
                int index = mAdapter.getPosition(oldItem);
                mAdapter.remove(oldItem);
                mAdapter.insert(newItem, index);
                mAdapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(),
                        "Успешно",
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
}
