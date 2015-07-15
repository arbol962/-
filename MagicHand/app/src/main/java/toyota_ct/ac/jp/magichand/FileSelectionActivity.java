package toyota_ct.ac.jp.magichand;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FileSelectionActivity extends Activity
        implements OnItemClickListener, OnClickListener
{
    // レイアウトパラメーター
    private static final int		WC					= LinearLayout.LayoutParams.WRAP_CONTENT;
    private static final int		FP					= LinearLayout.LayoutParams.FILL_PARENT;
    // ボタンタグ
    private static final int		BUTTONTAG_CANCEL	= 0;

    private ListView				m_listview;				// リストビュー
    private FileInfoArrayAdapter	m_fileinfoarrayadapter;	// ファイル情報配列アダプタ

    // アクティビティ起動時に呼ばれる
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // アクティビティの戻り値の初期化
        setResult( Activity.RESULT_CANCELED );

        // 呼び出し元からパラメータ取得
        String strInitialDir = null;
        Bundle extras = getIntent().getExtras();
        if( null != extras )
        {
            strInitialDir = extras.getString( "initialdir" );
        }
        if( null == strInitialDir || false == new File( strInitialDir ).isDirectory() )
        {
            strInitialDir = "/";
        }

        // レイアウト
        LinearLayout layout = new LinearLayout( this );
        layout.setOrientation( LinearLayout.VERTICAL );
        setContentView( layout );

        // リストビュー
        m_listview = new ListView( this );
        m_listview.setScrollingCacheEnabled( false );
        m_listview.setOnItemClickListener( this );
        LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams( FP, 0 );
        layoutparams.weight = 1;
        m_listview.setLayoutParams( layoutparams );
        layout.addView( m_listview );

        // ボタン
        Button button = new Button( this );
        button.setText( "Cancel" );
        button.setTag( BUTTONTAG_CANCEL );
        button.setOnClickListener( this );
        layoutparams = new LinearLayout.LayoutParams( FP, WC );
        button.setLayoutParams( layoutparams );
        button.setPadding( 10, 10, 10, 10 );
        layout.addView( button );

        fill( new File( strInitialDir ) );
    }

    // アクティビティ内の表示内容構築
    private void fill( File fileDirectory )
    {
        // タイトル
        setTitle( fileDirectory.getAbsolutePath() );

        // ファイルリスト
        File[] aFile = fileDirectory.listFiles();
        List<FileInfo> listFileInfo = new ArrayList<FileInfo>();
        if( null != aFile )
        {
            for( File fileTemp : aFile )
            {
                listFileInfo.add( new FileInfo( fileTemp.getName(), fileTemp ) );
            }
            Collections.sort( listFileInfo );
        }
        // 親フォルダに戻るパスの追加
        if( null != fileDirectory.getParent() )
        {
            listFileInfo.add( 0, new FileInfo( "..", new File( fileDirectory.getParent() ) ) );
        }

        m_fileinfoarrayadapter = new FileInfoArrayAdapter( this, listFileInfo );
        m_listview.setAdapter( m_fileinfoarrayadapter );
    }

    // ListView内の項目をクリックしたときの処理
    public void onItemClick(	AdapterView<?> l,
                                View v,
                                int position,
                                long id )
    {
        FileInfo fileinfo = m_fileinfoarrayadapter.getItem( position );

        if( true == fileinfo.getFile().isDirectory() )
        {
            fill( fileinfo.getFile() );
        }
        else
        {
            // 呼び出し元へのパラメータ設定
            Intent intent = new Intent();
            intent.putExtra( "file", fileinfo.getFile() );
            // アクティビティの戻り値の設定
            setResult( Activity.RESULT_OK, intent );

            // アクティビティ終了
            finish();
        }
    }

    // ボタンをクリックしたときの処理
    public void onClick( View arg0 )
    {
        final int iTag = (Integer)arg0.getTag();
        switch( iTag )
        {
            case BUTTONTAG_CANCEL:
                // アクティビティ終了
                finish();
                break;
        }
    }
}