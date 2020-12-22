package com.example.roomfinder;

// Necessary imports

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by USER on 4/28/2018.
 * A class file used to prepare a database connection for registration and login purposes
 */
public class SqliteConnection {
    SQLiteDatabase sqliteDB;

    public SqliteConnection(File file){

        // If database file doesn't exist, create one and create the users table
        // Else, open the existing database file
        if (!file.exists()){
            sqliteDB = SQLiteDatabase.openOrCreateDatabase(file, null);
            sqliteDB.execSQL("create table users( user_id text, firstname text, surname text, username text, phone text)");
        }else{
            sqliteDB = SQLiteDatabase.openOrCreateDatabase(file, null);
        }
    }

    // Getter to return the database file
    public SQLiteDatabase getDatabaseObject(){
        return sqliteDB;
    }
}
