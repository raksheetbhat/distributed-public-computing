package com.example.raksheet.majorproject.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.raksheet.majorproject.Process.ClientTask;
import com.example.raksheet.majorproject.Process.TaskClass;
import com.example.raksheet.majorproject.Process.TaskMaster;
import com.example.raksheet.majorproject.Storage.StorageMaster;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raksheet on 05-02-2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "majorproject";
    private static final String TABLE_STORAGE = "storagemaster";
    private static final String TABLE_TASK = "taskmaster";

    //fields in storage table
    private static final String KEY_STORAGE_ID = "storageID";
    private static final String KEY_STORAGE_FRAGMENT_ID = "fragmentID";
    private static final String KEY_STORAGE_FILE_NAME = "fileName";
    private static final String KEY_STORAGE_FILE_SIZE = "filesize";

    //fields in task table
    private static final String KEY_TASK_ID = "taskID";
    private static final String KEY_TASK_CODE = "code";
    private static final String KEY_TASK_DATA = "data";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_COMPLEXITY = "complexity";
    private static final String KEY_TASK_SERVER_SENT = "server_sent";

    //create statements
    private String CREATE_STORAGE_TABLE = "CREATE TABLE " + TABLE_STORAGE + "( " +
            KEY_STORAGE_ID + " INTEGER ," + KEY_STORAGE_FRAGMENT_ID + " INTEGER ," +
            KEY_STORAGE_FILE_NAME + " TEXT," + KEY_STORAGE_FILE_SIZE + " REAL, PRIMARY KEY (" +
            KEY_STORAGE_ID + ", " + KEY_STORAGE_FRAGMENT_ID + ")" +
            ");";

    private String CREATE_TASK_TABLE = "CREATE TABLE " + TABLE_TASK + "( " +
            KEY_TASK_ID + " INTEGER PRIMARY KEY, " + KEY_TASK_CODE + " TEXT, " +
            KEY_TASK_DATA + " TEXT, " + KEY_TASK_STATUS + " INTEGER, "
            + KEY_TASK_COMPLEXITY + " REAL, " + KEY_TASK_SERVER_SENT + " INTEGER);";

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STORAGE_TABLE);
        db.execSQL(CREATE_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);

        // Create tables again
        onCreate(db);
    }

    public void addStorageUser(StorageMaster storage){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STORAGE_ID, storage.getStorageID());
        values.put(KEY_STORAGE_FRAGMENT_ID, storage.getFragmentID());
        values.put(KEY_STORAGE_FILE_NAME, storage.getFileName());
        values.put(KEY_STORAGE_FILE_SIZE, storage.getFilesize());

        // Inserting Row
        db.insert(TABLE_STORAGE, null, values);
        db.close(); // Closing database connection
    }

    public StorageMaster getStorageUser(int storageID, int fragmentID){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STORAGE, new String[] { KEY_STORAGE_ID,
                        KEY_STORAGE_FRAGMENT_ID, KEY_STORAGE_FILE_NAME, KEY_STORAGE_FILE_SIZE}, KEY_STORAGE_ID + "=? AND " + KEY_STORAGE_FRAGMENT_ID + "=?",
                new String[] { String.valueOf(storageID),String.valueOf(fragmentID) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        // return contact
        return new StorageMaster(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)),cursor.getString(2), Float.parseFloat(cursor.getString(3)));
    }

    public List<StorageMaster> getAllStorageUsers(){
        List<StorageMaster> storageList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STORAGE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StorageMaster contact = new StorageMaster();
                contact.setStorageID(Integer.parseInt(cursor.getString(0)));
                contact.setFragmentID(Integer.parseInt(cursor.getString(1)));
                contact.setFileName(cursor.getString(2));
                contact.setFilesize(Float.parseFloat(cursor.getString(3)));
                // Adding contact to list
                storageList.add(contact);
            } while (cursor.moveToNext());
        }

        return storageList;
    }

    public int getStorageCount(){
        String countQuery = "SELECT  * FROM " + TABLE_STORAGE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public int updateStorageUser(StorageMaster storage){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STORAGE_ID, storage.getStorageID());
        values.put(KEY_STORAGE_FRAGMENT_ID, storage.getFragmentID());
        values.put(KEY_STORAGE_FILE_NAME, storage.getFileName());
        values.put(KEY_STORAGE_FILE_SIZE, storage.getFilesize());

        // updating row
        return db.update(TABLE_STORAGE, values, KEY_STORAGE_ID + " = ? AND "+ KEY_STORAGE_FRAGMENT_ID +" = ?",
                new String[] { String.valueOf(storage.getStorageID()),String.valueOf(storage.getFragmentID()) });
    }

    public void deleteStorageUser(StorageMaster storage){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STORAGE, KEY_STORAGE_ID + " = ? AND "+ KEY_STORAGE_FRAGMENT_ID +" = ?",
                new String[] { String.valueOf(storage.getStorageID()),String.valueOf(storage.getFragmentID()) });
        db.close();
    }

    public void addTask(TaskMaster task){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_ID, task.getTaskID());
        values.put(KEY_TASK_CODE, task.getCode());
        values.put(KEY_TASK_DATA, task.getData());
        values.put(KEY_TASK_STATUS, 0);
        values.put(KEY_TASK_COMPLEXITY, task.getComplexity());
        values.put(KEY_TASK_SERVER_SENT, task.getSentToServer());

        // Inserting Row
        db.replace(TABLE_TASK, null, values);
        db.close(); // Closing database connection
    }

    public TaskMaster getTask(int taskID){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASK, new String[] { KEY_TASK_ID,
                        KEY_TASK_CODE,KEY_TASK_DATA, KEY_TASK_STATUS, KEY_TASK_COMPLEXITY, KEY_TASK_SERVER_SENT}
                , KEY_TASK_ID + "=?",
                new String[] { String.valueOf(taskID) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        // return contact
        return new TaskMaster(Integer.parseInt(cursor.getString(0)),
               cursor.getString(1),cursor.getString(2) ,Integer.parseInt(cursor.getString(3)),
                Float.parseFloat(cursor.getString(4)),Integer.parseInt(cursor.getString(5)));
    }

    public TaskMaster fetchMaxTask(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASK, new String[] { KEY_TASK_ID,
                        KEY_TASK_CODE,KEY_TASK_DATA, KEY_TASK_STATUS, KEY_TASK_COMPLEXITY,KEY_TASK_SERVER_SENT}
                , KEY_TASK_STATUS + "=? AND "+KEY_TASK_SERVER_SENT+"= 0",
                new String[] { String.valueOf(0) }, null, null, KEY_TASK_COMPLEXITY+" DESC", "1");
        if (cursor != null)
            cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            // return contact
            return new TaskMaster(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),cursor.getString(2) ,Integer.parseInt(cursor.getString(3)),
                    Float.parseFloat(cursor.getString(4)),Integer.parseInt(cursor.getString(5)));
        }
        return null;
    }

    public TaskMaster fetchMaxTaskRaw(){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TASK + " WHERE " + KEY_TASK_STATUS + " = 0 ORDER BY "
                + KEY_TASK_COMPLEXITY + " DESC LIMIT 1;";

        Cursor cursor = db.rawQuery(query,null);
        if (cursor != null)
            cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            // return contact
             return new TaskMaster(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),cursor.getString(2) ,Integer.parseInt(cursor.getString(3)),
                    Float.parseFloat(cursor.getString(4)),Integer.parseInt(cursor.getString(5)));
        }
        cursor.close();
        return null;
    }

    public List<TaskMaster> fetchMaxTasks(){
        SQLiteDatabase db = this.getReadableDatabase();

        List<TaskMaster> results = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TASK + " WHERE " + KEY_TASK_STATUS + " = 0 ORDER BY "
                + KEY_TASK_COMPLEXITY + " DESC;";

        Cursor cursor = db.rawQuery(query,null);
        if (cursor != null)
            cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            // return contact
            results.add(new TaskMaster(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),cursor.getString(2) ,Integer.parseInt(cursor.getString(3)),
                    Float.parseFloat(cursor.getString(4)),Integer.parseInt(cursor.getString(5))));
        }
        cursor.close();
        return results;
    }

    public List<TaskMaster> fetchRemainingTasks(){
        SQLiteDatabase db = this.getReadableDatabase();

        List<TaskMaster> results = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TASK + " WHERE " + KEY_TASK_SERVER_SENT + " = 0 AND " +
                KEY_TASK_STATUS + " = 1;";

        Cursor cursor = db.rawQuery(query,null);
        if (cursor != null)
            cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            // return contact
            results.add(new TaskMaster(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),cursor.getString(2) ,Integer.parseInt(cursor.getString(3)),
                    Float.parseFloat(cursor.getString(4)),Integer.parseInt(cursor.getString(5))));
        }
        cursor.close();
        return results;
    }

    public int countRemainingTasks(){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + TABLE_TASK + " WHERE " + KEY_TASK_STATUS + " = 0;";

        Cursor cursor = db.rawQuery(query,null);
        if (cursor != null)
            cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            return Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        return 0;
    }

    public List<TaskMaster> getAllTasks(){
        List<TaskMaster> tasksList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TASK;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TaskMaster contact = new TaskMaster();
                contact.setTaskID(Integer.parseInt(cursor.getString(0)));
                contact.setCode(cursor.getString(1));
                contact.setData(cursor.getString(2));
                contact.setStatus(Integer.parseInt(cursor.getString(3)));
                contact.setComplexity(Float.parseFloat(cursor.getString(4)));
                contact.setSentToServer(Integer.parseInt(cursor.getString(5)));
                // Adding contact to list
                tasksList.add(contact);
            } while (cursor.moveToNext());
        }

        return tasksList;
    }

    public int getTaskCount(){
        String countQuery = "SELECT  * FROM " + TABLE_TASK;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateTask(TaskMaster task){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_ID, task.getTaskID());
        values.put(KEY_TASK_CODE, task.getCode());
        values.put(KEY_TASK_DATA, task.getData());
        values.put(KEY_TASK_STATUS, task.getStatus());
        values.put(KEY_TASK_COMPLEXITY, task.getComplexity());
        values.put(KEY_TASK_SERVER_SENT, task.getSentToServer());

        // updating row
        return db.update(TABLE_TASK, values, KEY_TASK_ID + " = ?",
                new String[] { String.valueOf(task.getTaskID()) });
    }

    public void deleteTask(TaskMaster task){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASK, KEY_TASK_ID + " = ?",
                new String[] { String.valueOf(task.getTaskID())});
        db.close();
    }

    public void deleteAllTasks()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_TASK);
        db.close();
    }
}
