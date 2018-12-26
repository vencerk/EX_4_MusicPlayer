package com.example.ex_4_musicplayer;

import android.Manifest;
import android.content.ContentResolver;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaPlayer mediaPlayer;//播放器
    private List<Music> musicList = new ArrayList<>();//歌曲
    private List<File> musicFile = new ArrayList<>();//mp3文件
    private int cMusicId = 0;//当前播放的音乐的id
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
            getMusicList();//获取播放列表并显示
            initMediaPlayer();//初始化播放器
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
                case R.id.nextM:
                    try {
                       // onDestroy();
                        mediaPlayer.stop();
                        cMusicId = (cMusicId + 1) % musicList.size();
                        initMediaPlayer();
                        mediaPlayer.start();//播放
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case R.id.lastM:
                    try{
                        //onDestroy();
                        mediaPlayer.stop();
                        cMusicId=(cMusicId+musicList.size()-1)%musicList.size();
                        initMediaPlayer();
                        mediaPlayer.start();//播放
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                    default:
                        break;

            }

    }

    private void initMediaPlayer(){
        try{
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setDataSource(musicList.get(cMusicId).getPath());//默认第一个音乐
            tv1.setText(musicList.get(cMusicId).getNameM()+cMusicId);
            mediaPlayer.prepare();//准备
        }catch (Exception e){
            e.printStackTrace();
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
        File SdcardFile = Environment.getExternalStorageDirectory();//sdcard路径
        getSDcardFile(SdcardFile);//获取mp3文件路径
        int sfLength = musicFile.size(),i=0;
        musicList.clear();
            while (i<sfLength) {
                File c = musicFile.get(i);
                String path = c.getPath();
                String name = c.getName();
                Music music = new Music(name, path);
                musicList.add(music);
                i++;
            }
        MusicAdapter adapter = new MusicAdapter(MainActivity.this, R.layout.music_item, musicList);
        ListView lvw = findViewById(R.id.listWords);
        lvw.setAdapter(adapter);

    }
    public void getSDcardFile(File groupPath) {
        //循环获取sdcard目录下面的目录和文件
        for (int i = 0; i < groupPath.listFiles().length; i++) {
            File childFile = groupPath.listFiles()[i];
            //假如是目录的话就继续调用getSDcardFile()将childFile作为参数传递的方法里面
            if (childFile.isDirectory()) {
                getSDcardFile(childFile);
            } else {
                //如果是文件的话,判断是不是以.mp3结尾,是就加入到List里面
                if (childFile.toString().endsWith(".mp3")) {
                    musicFile.add(childFile);
                }
            }
        }
    }

}
