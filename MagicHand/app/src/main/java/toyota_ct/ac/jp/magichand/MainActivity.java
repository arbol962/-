package toyota_ct.ac.jp.magichand;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.annotation.TargetApi;


public class MainActivity extends AppCompatActivity {

    //非同期タスク
    private TCPSocketTask task_;

    private List<String> fileList = new ArrayList<String>();
    private ListView lv;
    private File[] files;
    private Bitmap imgBitmap;
    // オプションメニューID
    private static final int	MENUID_FILE			= 0;
    //リクエストコード
    private static final int REQUEST_FILESELECT = 0;
    private static final int REQUEST_KITKAT = 1;
    // 初期フォルダ
    private String m_strInitialDir = "/mnt/sdcard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.fileSelButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileSelect();
            }
        });

        Button buttn = (Button) findViewById(R.id.button);
        buttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });


    }

    public void fileSelect(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // ここはKITKAT（Android 4.4）以降で実行される。
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_KITKAT);
        }else {
            Intent intent = new Intent( this, FileSelectionActivity.class );
            intent.putExtra( "initialdir", m_strInitialDir );
            startActivityForResult(intent, REQUEST_FILESELECT);
        }

    }

    public void startConnection(){
        BufferedReader reader = null;

        //宛先アドレスとポートをフォームから取得
        String address = ((EditText) findViewById(R.id.editText)).getText().toString();
        String strPort = ((EditText) findViewById(R.id.editText2)).getText().toString();
        int port = 0;
        try {
            port = Integer.parseInt(strPort);
        }catch(Exception e){
            Toast.makeText(this, "接続先をご確認ください", Toast.LENGTH_SHORT).show();
        }
        task_ = new TCPSocketTask(this);
        task_.newConnection(address,port,imgBitmap);

        task_.execute();
    }

    // アクティビティ呼び出し結果の取得
    @Override
    protected void onActivityResult(	int requestCode,
                                        int resultCode,
                                        Intent intent )
    {
        if( REQUEST_FILESELECT == requestCode && RESULT_OK == resultCode )
        {
            Bundle extras = intent.getExtras();
            if( null != extras )
            {
                File file = (File)extras.getSerializable( "file" );
                Toast.makeText( this, "File Selected : " + file.getPath(), Toast.LENGTH_SHORT ).show();
                // 選択ファイルを設定
                ((TextView)findViewById(R.id.text)).setText(file.getPath() + file.getName());
                // 変更するための支持を表示
                //((TextView)findViewById(R.id.textView)).setText("変更するにはメニューボタン_ _ _ _↑");
                ((TextView)findViewById(R.id.textView2)).setText("　　　が選択されています。");
                imgBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ((ImageView)findViewById(R.id.imageView)).setImageBitmap(imgBitmap);
                m_strInitialDir = file.getParent();

                Button btn = (Button)findViewById(R.id.button);
                btn.setEnabled(true);
            }
        }else if(REQUEST_KITKAT == requestCode && RESULT_OK == resultCode){
            Uri open_file = intent.getData();
            BitmapFactory.Options mOptions = new BitmapFactory.Options();
            mOptions.inSampleSize = 10;
            InputStream is;
            try {
                is = getContentResolver().openInputStream(open_file);
                // mBitmap は Bitmap 型として定義したインスタンス変数
                imgBitmap = BitmapFactory.decodeStream(is);
                is.close();
                // mOpenImage は Bitmap を表示させるための ImageView のインスタンス
                ((ImageView)findViewById(R.id.imageView)).setImageBitmap(imgBitmap);
                Button btn = (Button)findViewById(R.id.button);
                btn.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}