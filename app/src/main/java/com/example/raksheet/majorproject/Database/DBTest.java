package com.example.raksheet.majorproject.Database;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.raksheet.majorproject.Process.TaskMaster;
import com.example.raksheet.majorproject.R;

/**
 * Created by Raksheet on 23-02-2017.
 */

public class DBTest extends AppCompatActivity {
    TextView textView;
    Button test,delete;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_testing);

        textView = (TextView) findViewById(R.id.db_texetview);
        test = (Button) findViewById(R.id.db_testing);
        delete = (Button) findViewById(R.id.db_delete);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler db = new DatabaseHandler(getApplication());
                String str = "";
                for(TaskMaster t : db.getAllTasks()){
                    str += "Task ID: "+t.getTaskID()+"\nCode: "+t.getCode()+"\nData: "+t.getData()
                            +"\ncomplexity: "+t.getComplexity()+"\nstatus: "+t.getStatus();
                    str+="\n";
                }
                textView.setText(str);
                db.close();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler db = new DatabaseHandler(getApplication());
                db.deleteAllTasks();
                db.close();
            }
        });

    }
}
