package com.theonlylies.tageditor;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 5);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("PERMISSONS:", "START");
        if (5 == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSONS:", "GRANTED!!!");
                MediaStoreReadMedia();
                MediaStoreReadALBUMS();
                String path="/storage/emulated/0/Music/TCTS feat. Sage The Gemini & Kelis - Do It Like Me (Icy Feet).mp3";
                updateFileMediaStoreMedia(path);
                //removeArtwork(33L);
            } else {
                Log.d("PERMISSONS:", "UNGRANTED!!!");

            }
        }
    }

    /*
    APPROVED FOR UPDATE MEDIASTORE INFO
     */

    boolean updateFileMediaStoreMedia(String path){
       Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
       String nPath = "\""+path+"\"";
       Cursor cursor = getContentResolver().query(contentUri, null, MediaStore.Audio.Media.DATA+"=="+nPath, null, null);
       long id=-1;

       try{
           if (cursor.moveToFirst())id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
           try {
               MP3File file = new MP3File(path);
               file.getID3v2Tag().setField(FieldKey.YEAR,"1869");
               file.getID3v2Tag().setField(FieldKey.ALBUM,"ПУТИН 2018!");
               file.commit();
           } catch (IOException | TagException | ReadOnlyFileException | CannotWriteException | InvalidAudioFrameException e) {
               e.printStackTrace();
           }
           if(id!=-1)getContentResolver().delete(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id),null,null);
           scanFile(path);

       }catch (NullPointerException | IllegalArgumentException e){
           e.printStackTrace();
           return false;
       }
       return true;
    }

    void scanFile(String filePath){
        MediaScannerConnection.scanFile(this,
                new String[] { filePath }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    /*
    Approved method to delete artwork fully!
     */
    boolean removeArtwork(String pathToMusicFile){

        String nPath = "\""+pathToMusicFile+"\"";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.IS_MUSIC+"!= 0 and _data=="+nPath, null, null);
        long id = -1;
        if(cursor!=null && cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }


        cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Albums.ALBUM_ID+"=="+id, null, null);
        String albumPath=null;
        if(cursor!=null && cursor.moveToFirst()){
            albumPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
        }

        boolean tag=false;

        if(albumPath!=null && !albumPath.isEmpty()){
            File file = new File(albumPath);
            if(file.exists()){
                MP3File mp3File= null;
                try {
                    mp3File = new MP3File(albumPath);
                    mp3File.getID3v2Tag().deleteArtworkField();
                    mp3File.commit();
                    tag=true;
                } catch (IOException | TagException | InvalidAudioFrameException | ReadOnlyFileException | CannotWriteException e) {
                    e.printStackTrace();
                }

            }
        }

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        int i= getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri, id), null, null);
        return i>0 && tag;
    }

    void MediaStoreReadMedia() {
        String[] proj = {};
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(contentUri, null, MediaStore.Audio.Media.IS_MUSIC+"!= 0", null, null);
        //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String s;
        Log.d("cursor count:",String.valueOf(cursor.getCount()));
        if(cursor.moveToFirst()){
            do{
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Log.d("_ID COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                Log.d("TITLE COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                Log.d("ALBUM COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Log.d("ABUM_ID COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                Log.d("ARTIST COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                Log.d("DATE_ADDED COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
                Log.d("YEAR COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
                Log.d("TRACK COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                Log.d("DATA COL", s = s != null ? s : "empty");
                Log.d("---", "------------------------------------------------------------------");
            }while (cursor.moveToNext());
        }

    }

    void MediaStoreReadALBUMS() {
        String[] proj = {MediaStore.Images.Media.DATA};
        Uri contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String s;
        Log.d("cursor count:",String.valueOf(cursor.getCount()));
        if(cursor.moveToFirst()){
            do{
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                Log.d("ALBUM_ART COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                Log.d("ALBUM COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                Log.d("_ID COL", s = s != null ? s : "empty");
                s = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
                Log.d("ARTIST COL", s = s != null ? s : "empty");
                Log.d("---", "------------------------------------------------------------------");
            }while (cursor.moveToNext());
        }

    }
}
