package com.cdfortis.chensync.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cdfortis.chensync.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private TextView textPath;
    private ListView listDirectory;
    private SimpleAdapter adapter;
    private List<Map<String, String>> directors = new ArrayList<>();
    private File currentPath;
    private File rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);
        textPath = (TextView) findViewById(R.id.textPath);
        listDirectory = (ListView) findViewById(R.id.listDirectory);
        adapter = new SimpleAdapter(this, directors, R.layout.directory_item, new String[]{"name"}, new int[]{R.id.textName});
        listDirectory.setAdapter(adapter);
        listDirectory.setOnItemClickListener(this);
        rootPath = Environment.getExternalStorageDirectory();
        setupChildren(rootPath);
    }


    private void setupChildren(File path) {
        currentPath = path;
        textPath.setText(currentPath.getAbsolutePath());
        directors.clear();
        File[] files = currentPath.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                Map<String, String> map = new HashMap<>();
                map.put("name", file.getName());
                directors.add(map);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = new File(currentPath, directors.get(position).get("name"));
        setupChildren(file);
    }

    public void onAdd(View view) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PATH, currentPath.getAbsolutePath());
        setResult(RESULT_OK, intent);
        this.finish();
    }

    public void onCancel(View view) {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.equals(rootPath.getAbsolutePath(), currentPath.getAbsolutePath())) {
            super.onBackPressed();
        } else {
            setupChildren(currentPath.getParentFile());
        }
    }
}
