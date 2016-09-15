package com.example.vinay.epubscreenreader;

import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TextRendererActivity extends AppCompatActivity {
    Button prev_btn,next_btn,play_btn;
    TextView mTextView;
    CharSequence mText;
    Pagination mPagination;
    int mCurrentIndex = 0;
    MediaPlayer mediaPlayer;
    private static String TAG = "TextRendererActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_renderer);
        initializeTTS();
        prev_btn = (Button)findViewById(R.id.prev_page);
        next_btn = (Button)findViewById(R.id.next_page);
        play_btn = (Button)findViewById(R.id.play_button);
        mTextView = (TextView)findViewById(R.id.ebook_tv);
        String displayString = getIntent().getExtras().getString("display");
        mText = Html.fromHtml(displayString);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Removing layout listener to avoid multiple calls
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mPagination = new Pagination(mText,
                        mTextView.getWidth(),
                        mTextView.getHeight(),
                        mTextView.getPaint(),
                        mTextView.getLineSpacingMultiplier(),
                        mTextView.getLineSpacingExtra(),
                        mTextView.getIncludeFontPadding());
                update();
            }
        });


    }

    private void initializeTTS() {

        if (!PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext())
                .getBoolean("installed", false)) {
            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean("installed", true).commit();
            copyAssests();
        }

    }

    public void copyAssests()
    {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String filename : files)
        {
            System.out.println("In CopyAssets"+filename);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                String foldername= Environment.getExternalStorageDirectory().getPath()+"/Android/data/"+getPackageName().toString()+"/";
                File folder = new File(foldername);
                folder.mkdirs();
                File outfile = new File(foldername+filename);
                out = new FileOutputStream(outfile);
                copyFile(in, out);
                System.out.println("In copyAssets Entire Path"+foldername+filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public native String mainfn(String inputtext, String path, String wavname);
    static
    {
        System.loadLibrary("mainfn");
    }


    private void update() {
        final CharSequence text = mPagination.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }

    public void loadPrevPage(View view){
        mCurrentIndex = (mCurrentIndex > 0) ? mCurrentIndex - 1 : 0;
        update();
    }
    public void loadNextPage(View view){
        mCurrentIndex = (mCurrentIndex < mPagination.size() - 1) ? mCurrentIndex + 1 : mPagination.size() - 1;
        update();
    }

    public void speakTeluguText(View view){
        // check with teluguword - hyderabad
        String text = getResources().getString(R.string.telugu_word);
        Log.i(TAG,text);
        try {
            synthesisWavInBackground(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void synthesisWavInBackground(String msg) throws IOException {// from share
        String speaker_name = "iitm_telugu_old";
        String inputtext = msg.trim();
        inputtext=inputtext.replace("|",".");
        inputtext=inputtext.replace(" . ", " .");
        inputtext=inputtext.replaceAll("\\s+", " ");
        inputtext=inputtext.trim();
        if(inputtext.endsWith( " ." )){
            inputtext = inputtext.substring(0, inputtext.length() - 2);
        }else if(inputtext.endsWith( " . " )){
            inputtext = inputtext.substring(0, inputtext.length() - 3);
        }
        inputtext = inputtext.trim();
        String foldername = Environment.getExternalStorageDirectory().getPath()+"/Android/data/"+getPackageName().toString()+"/";
        String filename = foldername+speaker_name+".htsvoice";
        String wavname = foldername+"1.wav";
        Log.v(TAG,"input text = "+inputtext);
        Log.v(TAG,"filename = "+filename);
        Log.v(TAG,"wavname = "+wavname);
        //Toast.makeText(TextRendererActivity.this,mainfn(inputtext,filename,wavname),Toast.LENGTH_SHORT).show();
        mainfn(inputtext,filename,wavname);
        /////////////////////////////////////////////////////////////////////////////////
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(wavname);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                System.exit(0);
            }
        });
        mediaPlayer.start();

    }

}
