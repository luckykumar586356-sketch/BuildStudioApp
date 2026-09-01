package com.template;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private ImageButton fabAdd;
    private ListView listView;
    private ArrayList<String> itemsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fabAdd = findViewById(R.id.fab_add);
        listView = findViewById(R.id.list_view);

        itemsList = new ArrayList<String>();
        itemsList.add("Item 1 - Project Setup");
        itemsList.add("Item 2 - Android Layout");
        itemsList.add("Item 3 - Java Activity");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemsList);
        listView.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newNum = itemsList.size() + 1;
                itemsList.add("New Item #" + newNum);
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Item #" + newNum + " added!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
