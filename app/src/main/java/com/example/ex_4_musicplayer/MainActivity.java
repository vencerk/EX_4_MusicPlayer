package com.example.ex_4_musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MediaPlayer mediaPlayer;//播放器
    private List<Music> musicList = new ArrayList<>();//歌曲列表
    private List<File> musicFile = new ArrayList<>();//mp3文件列表
    private List<File> lrcFile = new ArrayList<>();//歌词文件列表
    private List<lrcCLass> lrcList = new ArrayList<>();//歌词
    private int cMusicId = 0;//当前播放的音乐的id

    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentTime=0;//当前音乐播放的进度
    private int lrcFlag=0;//当前歌曲是否有歌词
    private SeekBar seekBar;
    private Timer timer;
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

        seekBar = findViewById(R.id.playSeekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());

        //如果用户给访问SD卡的权限，那么初始化MediaPlayer，否则直接结束
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            getMusicList();//获取播放列表并显示
            initMediaPlayer(musicList.get(cMusicId).getPath());//初始化播放器
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //tv1.append(""+mediaPlayer.getCurrentPosition());
                    if(lrcFlag!=0) {
                        lrcShow();
                    }else {
                        tv1.setText("当前播放："+musicList.get(cMusicId).getNameM());
                    }
                    break;
            }

        }
    };

    @Override
    public void onClick(View v){
            switch (v.getId()){
                case R.id.playM:
                    Button playM = findViewById(R.id.playM);
                    if(mediaPlayer.isPlaying()){
                        playM.setText("播放");
                        mediaPlayer.pause();//暂停
                    }else {
                        playM.setText("暂停");
                        mediaPlayer.start();//播放
                    }
                    break;
                case R.id.nextM:
                    try {
                        mediaPlayer.stop();
                        cMusicId = (cMusicId + 1) % musicList.size();
                        initMediaPlayer(musicList.get(cMusicId).getPath());
                        mediaPlayer.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case R.id.lastM:
                    try{
                        mediaPlayer.stop();


                        timer.cancel();
                        timer=null;


                        cMusicId=(cMusicId+musicList.size()-1)%musicList.size();
                        initMediaPlayer(musicList.get(cMusicId).getPath());
                        mediaPlayer.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                    default:
                        break;

            }

    }

    //初始化播放器
    private void initMediaPlayer(String path){
        try{
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();//准备
            seekBar.setMax(mediaPlayer.getDuration());
            currentTime=0;
            String n1 = musicList.get(cMusicId).getNameM()+".lrc";//歌曲名字加上.lrc
            //找到当前歌曲对应的歌词
            lrcFlag=0;
            for(int i =0;i<lrcFile.size();i++) {
                String n2 = lrcFile.get(i).getName();//歌词文件名字 *.lrc
                if(n1.equals(n2)) {
                    lrcResolve(lrcFile.get(i));//初始化歌词列表
                    lrcFlag = 1;
                    break;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isSeekBarChanging&&mediaPlayer.isPlaying()){//如果进度条未改变，并且当前正在播放
                    //tv1.append(""+mediaPlayer.getCurrentPosition());
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    //lrcShow(currentTime);

                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);


                }
            }
        },0,1000);

}
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        timer.cancel();
        timer = null;
    }

    //获取mp3文件列表
    public void getMusicList() {
        File SdcardFile = Environment.getExternalStorageDirectory();//sdcard路径
        getSDcardFile(SdcardFile);//获取mp3文件路径
        int sfLength = musicFile.size(),i=0;
        musicList.clear();
            while (i<sfLength) {
                File c = musicFile.get(i);
                String path = c.getPath();
                String name = c.getName();

                name=name.substring(0,name.length()-4);
                //tv1.setText(name);
                Music music = new Music(name, path);
                musicList.add(music);
                i++;
            }
        MusicAdapter adapter = new MusicAdapter(MainActivity.this, R.layout.music_item, musicList);
        ListView lvw = findViewById(R.id.listWords);
        lvw.setAdapter(adapter);

        lvw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mpath = musicList.get(i).getPath();
                cMusicId=i;
                mediaPlayer.stop();
                initMediaPlayer(mpath);
                mediaPlayer.start();//播放

                //Toast.makeText(MainActivity.this,word,Toast.LENGTH_LONG).show();

            }
        });

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
                //判断是不是以lrc结尾,是就加入到List里面
                if(childFile.toString().endsWith(".lrc")){
                    lrcFile.add(childFile);
                }
            }
        }
    }

    //歌词解析
    private void lrcResolve(File filename) {

        lrcList.clear();//清空上一首的歌词列表

        InputStreamReader is=null;
    try {
        is = new InputStreamReader(new FileInputStream(filename), "utf-8");
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }catch (UnsupportedEncodingException e){
        e.printStackTrace();
    }
        BufferedReader reader = new BufferedReader(is);
        String line;
        //StringBuilder stringBuilder = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
               // stringBuilder.append(line);
                Log.v("abcc",line);

                if(line.substring(0,2).equals("[0")) {//暂时只考虑十分钟以内的歌曲
                    long minute = Integer.parseInt(line.substring(line.indexOf("[") + 1, line.lastIndexOf(":")));
                    long second = Integer.parseInt(line.substring(line.indexOf(":") + 1,line.lastIndexOf(".")));
                    long millisecond = Integer.parseInt(line.substring(line.indexOf(".") + 1,line.lastIndexOf("]")));
                    long time1=(minute * 60 * 1000 + second * 1000 + millisecond);
                    String s1 = line.substring(line.indexOf("]") + 1,line.length());
                    lrcCLass lc = new lrcCLass();
                    lc.setTime1(time1);
                    lc.setOneLrc(s1);
                    lrcList.add(lc);
                }
            }

            //tv1.setText(stringBuilder.toString());
            reader.close();
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//歌词显示
    private void lrcShow() {
        long cTime = (long) mediaPlayer.getCurrentPosition()/1000;
        lrcCLass lc1= lrcList.get(currentTime);
        long n= lc1.getTime1()/1000;
        if (cTime >= n) {
            String s1 = lc1.getOneLrc();
            tv1.setText(lc1.getOneLrc());
            currentTime++;
        }

    }




    //进度条
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }
        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }
        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer(musicList.get(cMusicId).getPath());
                }else{
                    Toast.makeText(this,"无权限使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
        }
    }

}

