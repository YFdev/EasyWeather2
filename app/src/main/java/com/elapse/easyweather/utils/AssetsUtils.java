package com.elapse.easyweather.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by YF_lala on 2018/7/24.
 * 该类用于copy Assets中准备好的数据库文件，并提供打开方法
 */

public class AssetsUtils extends SQLiteOpenHelper{
    private static final String TAG = "AssetsUtils";

    private static String DB_PATH = "/data/data/com.elapse.easyweather/databases/";
    private static String DB_NAME = "weather_data.db";
    private static SQLiteDatabase myDatabase;
    private Context mContext;
    public AssetsUtils(Context context) {
        super(context, DB_NAME, null, 1);
        mContext = context;
    }

    public void createDataBase() throws IOException{
        boolean dbExist = checkDataBase();
        if (dbExist){
            return;
        }else {
            this.getReadableDatabase();
            try{
               copyDataBase();
           }catch (IOException e){
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDataBase() throws IOException{
        InputStream is = mContext.getAssets().open(DB_NAME);
        String outPath = DB_PATH + DB_NAME;
        OutputStream os = new FileOutputStream(outPath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0){
            os.write(buffer,0,length);
        }
        os.flush();
        os.close();
        is.close();
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDb = SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){
            Log.d(TAG, "checkDataBase: not exist");
        }
        if (checkDb != null){
            checkDb.close();
        }
        return checkDb != null ? true:false;
    }

    public static SQLiteDatabase getDataBase() throws SQLiteException{
        String path = DB_PATH + DB_NAME;
        myDatabase = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
        return myDatabase;
    }

    @Override
    public synchronized void close() {
        if (myDatabase != null){
            myDatabase.close();
        }
        super.close();
    }

    //    public static boolean getPath(Context context){
//        File dbFile = new File(filePath);
//        if (dbFile.exists()){
//            return true;
//        }else {
//            File path = new File(pathStr);
//            path.mkdir();
//            try {
//                InputStream is = context.getAssets().open("weather_data.db");
////                InputStream is = context.getClass().getClassLoader().getResourceAsStream("assets/"+db_name);
//                FileOutputStream fos = new FileOutputStream(dbFile);
//                byte[] buffer = new byte[1024];
//                int count = 0;
//                while ((count = is.read(buffer)) != -1){
//                    fos.write(buffer,0,count);
//                }
//                fos.flush();
//                fos.close();
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
//        }
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
