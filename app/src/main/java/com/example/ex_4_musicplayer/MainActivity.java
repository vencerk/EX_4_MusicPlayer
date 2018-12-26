package com.example.ex_4_musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaPlayer mediaPlayer=new MediaPlayer();
    private List<Music> musicList = new ArrayList<>();
    TextView tv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1=findViewById(R.id.tv1);
        Button lastM = findViewById(R.id.lastM);
        Button playM = findViewById(R.id.playM);
        Button nextM = findViewById(R.id.nextM);

        lastM.setOnClickListener(this);
        playM.setOnClickListener(this);
        nextM.setOnClickListener(this);

        //如果用户给访问SD卡的权限，那么初始化MediaPlayer，否则直接结束
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            initMediaPlayer();
            getMusicList();
        }


    }

    @Override
    public void onClick(View v){
            switch (v.getId()){
                case R.id.playM:
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();//暂停
                    }else {
                        mediaPlayer.start();//播放
                    }
                    break;


                    default:
                        break;

            }

    }

    private void initMediaPlayer(){
        try{
            File file = new File(Environment.getExternalStorageDirectory(),
                    "团结就是力量.mp3");

            mediaPlayer.setDataSource(file.getPath());//Music的路径

            mediaPlayer.prepare();//准备
            int n = mediaPlayer.getDuration();
            String timelong = n / 1000 + "s";
            //tv1.setText(timelong);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"No",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else{
                    Toast.makeText(this,"无权限使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                default:
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void getMusicList() {
        musicList.clear();

        Cursor cursor = MainActivity.this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);

        if (cursor != null) {
tv1.setText("2");
            while (cursor.moveToNext()) {
                tv1.append("3");
                String s1 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                Music m = new Music();
                m.setNameM(s1);
                m.setSinger(singer);
                musicList.add(m);
            }
        }
        cursor.close();
        MusicAdapter adapter = new MusicAdapter(MainActivity.this, R.layout.music_item, musicList);
        ListView lvw = findViewById(R.id.listWords);
        lvw.setAdapter(adapter);
    }
}
