package com.teamtreehouse.mememaker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.teamtreehouse.mememaker.models.Meme;
import com.teamtreehouse.mememaker.models.MemeAnnotation;

import java.util.ArrayList;
import java.util.Date;

public class MemeDataSource {

    private Context mContext;
    private MemeSQLiteHelper mMemeSQLiteHelper;

    public MemeDataSource(Context context) {
        mContext = context;
        mMemeSQLiteHelper = new MemeSQLiteHelper(context);

    }

    private SQLiteDatabase open() {
        return mMemeSQLiteHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase database) {
        database.close();
    }

    public void delete(int memeId){
        SQLiteDatabase database = open();
        database.beginTransaction();

        database.delete(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                String.format("%s=%s", MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME,
                        String.valueOf(memeId)),
                null);
        database.delete(MemeSQLiteHelper.MEMES_TABLE,
                String.format("%s=%s",BaseColumns._ID,
                        String.valueOf(memeId)),
                null);

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public ArrayList<Meme> read() {
        ArrayList<Meme> memes = readMemes();
        addMemesAnnotations(memes);

        return memes;
    }

    public ArrayList<Meme> readMemes() {
        SQLiteDatabase database = open();

        Cursor cursor = database.query(
                MemeSQLiteHelper.ANNOTATIONS_TABLE,
                new String[]{MemeSQLiteHelper.COLUMN_MEME_NAME, BaseColumns._ID, MemeSQLiteHelper.COLUMN_MEME_ASSET},
                null,
                null,
                null,
                null,
                MemeSQLiteHelper.COLUMN_MEME_CREATE_DATE + " DESC");

        ArrayList<Meme> memes = new ArrayList<Meme>();
        if (cursor.moveToFirst()) {
            do {
                Meme meme = new Meme(getIntFromColumnName(cursor, BaseColumns._ID),
                        getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_ASSET),
                        getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_NAME),
                        null);
                memes.add(meme);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close(database);
        return memes;
    }

    public void addMemesAnnotations(ArrayList<Meme> memes) {
        SQLiteDatabase database = open();
        for (Meme meme : memes) {
            ArrayList<MemeAnnotation> annotations = new ArrayList<MemeAnnotation>();
            Cursor cursor = database.rawQuery(
                    "SELECT * FROM " + MemeSQLiteHelper.MEMES_TABLE +
                            " WHERE MEME_ID = " + meme.getId(), null);
            if (cursor.moveToFirst()) {
                do {
                    MemeAnnotation annotation = new MemeAnnotation(
                            getIntFromColumnName(cursor, BaseColumns._ID),
                            getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR),
                            getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE),
                            getIntFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_X),
                            getIntFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_Y));
                    annotations.add(annotation);
                } while (cursor.moveToNext());
            }
            meme.setAnnotations(annotations);
            cursor.close();
            database.close();
        }
    }

    public void update(Meme meme) {
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues updateMemeValues = new ContentValues();
        updateMemeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        database.update(MemeSQLiteHelper.MEMES_TABLE, updateMemeValues,
                String.format("%s=%d", BaseColumns._ID, meme.getId()), null);
        for (MemeAnnotation annotation : meme.getAnnotations()) {
            ContentValues updateAnnotations = new ContentValues();
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, annotation.getTitle());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, annotation.getLocationX());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, annotation.getLocationY());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, meme.getId());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, annotation.getColor());

            if (annotation.hasBeenSaved()) {
                database.update(MemeSQLiteHelper.MEMES_TABLE,
                        updateAnnotations,
                        String.format("%s=%d", BaseColumns._ID, annotation.getId()),
                        null);
            } else {
                database.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                        null,
                        updateAnnotations);
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);

    }

    private int getIntFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    public void create(Meme meme) {
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues memeValues = new ContentValues();
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_ASSET, meme.getAssetLocation());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_CREATE_DATE, new Date().getTime());

        long memeID = database.insert(mMemeSQLiteHelper.MEMES_TABLE, null, memeValues);

        for (MemeAnnotation memeAnnotation :
                meme.getAnnotations()) {
            ContentValues annotationsValues = new ContentValues();
            annotationsValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, memeAnnotation.getColor());
            annotationsValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, memeAnnotation.getTitle());
            annotationsValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, memeAnnotation.getLocationX());
            annotationsValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, memeAnnotation.getLocationY());
            annotationsValues.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, memeID);

            database.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE, null, annotationsValues);

        }

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }
}













