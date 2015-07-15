package toyota_ct.ac.jp.magichand;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * TCPSocketTask.java
 * Created by Amon Keishima on 15/06/10.
 */
public class TCPSocketTask extends AsyncTask<String, Integer, String> {

    private Context mContext;
    private Bitmap sendImage;
    private String sendStr;
    String[] strs;
    private String receiveStr;

    private String address;     //接続先IPアドレス
    private int port;        //接続先ポート
    private Socket socket;      //ソケット

    private BufferedReader reader;

    private int ERROR_CODE = 0;

    //アドレスのセッタ
    public void setAddress(String address){
        this.address = address;
    }

    //ポートのセッタ
    public void setPort(int port){
        this.port = port;
    }

    //アドレスのゲッタ
    public String getAddress(){
        return this.address;
    }

    //ポートのゲッタ
    public int getPort(){
        return this.port;
    }

    public TCPSocketTask(Context context){
        mContext = context;
    }

    /**
     * ソケットの接続先の設定を行います
     * @param address
     * @param port
     */
    public void newConnection(String address, int port, Bitmap image){
        this.setAddress(address);
        this.setPort(port);
        this.sendImage = image;
    }

    //実行前に何かをするならここ
    @Override
    protected void onPreExecute() {
        //readerの初期化
        reader = null;

        //ソケットの初期化
        this.socket = null;
    }

    //実行するタスクはここ
    @Override
    protected String doInBackground(String... params) {
        try{
            sendStr = encodeTobase64(sendImage);
            sendStr = sendStr.concat("<EOF>\n\n");
        }catch(Exception e){
            return "IMAGE_CONVERT_EXCEPTION";
        }

        try{
            //新規ソケット接続の開始
            socket = new Socket(getAddress(), getPort());
            socket.setTcpNoDelay(true);
            //System.out.println("BUFFER SIZE :"+socket.getSendBufferSize());

            //PrintWriterを使ったソケット書き込み
            PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);

            Charset.forName("UTF-8").encode(sendStr);

            //送信したデータのサイズ確認
            System.out.println("送信に使うデータの長さは：" + sendStr.length());
            //System.out.println(sendTxt);

            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(),10240);
            bos.write(sendStr.getBytes());
            bos.flush();

            //受信


            //受信開始
            while(true) {
                if(recieve()){
                    //trueならなにもしない
                    break;
                }else{
                    if(ERROR_CODE == 0) {
                        break;
                    }else if(ERROR_CODE == 114){
                        //error
                        break;
                    }
                }
            }



            //Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            //受信したファイルサイズの確認
            //System.out.println("message Length is" + message.length());
            //System.out.println(message);
            //受信データの表示
            //Logger.debugEntire(message);

            //受信文字列からEOFの削除
           // message = message.replaceAll("<EOF>", "");

            //受信文字列からBase64デコードで画像へ
           // Bitmap recievedBmp = this.decodeBase64(message);

            //デコードした画像をUIImageViewに反映
            //ImageView image = (ImageView) findViewById(R.id.imageView);
            //image.setImageBitmap(recievedBmp);

            pw.close();
            socket.close();

            return "DONE";





        } catch (SocketException e) {
            e.printStackTrace();
            return "Exception";

        } catch (UnknownHostException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return "Done";
    }

    //実行中に進捗報告された場合はここ(メインスレッドになる)
    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    //@Override public void onCancel(DialogInterface dialog) {

    //}
    @Override protected void onCancelled() {

    }

    @Override
    protected void onPostExecute(String result) {
        if(result.equalsIgnoreCase("Exception")){
            Toast.makeText(this.mContext, "例外が発生しました！", Toast.LENGTH_SHORT).show();
        }else if(result.equalsIgnoreCase("DONE")){
            Toast.makeText(this.mContext, "処理が終了しました。", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean recieve(){

        try{
            InputStream is = socket.getInputStream();
            InputStreamReader ir = new InputStreamReader(is,"UTF-8");
            BufferedReader br = new BufferedReader(ir);

            while(is.available() == 0);

            StringBuilder record = new StringBuilder();
            String line = null;
            //受信し続ける限り実行します
            while((line=br.readLine())!=null)   // reading here
            {

                record.append(line);        //受信文字列に一行ずつ追加していく
                System.out.println(line);   //デバッグ用に表示

                if( line == null ){
                    System.out.println("NULL");
                    reader.close();
                    break;
                }

                if(line.contains("<EOF>")){   //<EOF>が送られてきたら終了
                    System.out.println("--------Got EOF--------");
                    //reader.close();
                    break;
                }

                if (line.length()==0){
                    System.out.println("Found an empty line.");
                    break;
                }
            }
            //line=reader.readLine();
            //record.append(line);        //受信文字列に一行ずつ追加していく
            //System.out.println(line);   //デバッグ用に表示

            br.close();

            //処理用にmessageという文字列を生成
            String message = new String(record);

            //ここで解析、イベント実行→return true;
            if(message.startsWith("1 started")) {


                //文頭語解析
                //String SendToServer = "0";
                //String RECEIVE = "1";


                if(message.startsWith("send 2 1")){ //クライアントからサーバーへ送る指令ならば

                    strs = message.split(" ");     //データの先頭に付属しているコマンドIDを分割する

                    String COMMAND = strs[0];
                    String SOURCE = "2 ";
                    String FILE_PATH = strs[2];     //送り先のFILE PATH
                    String DESTINATION = "1 ";
                    String DATA = strs[4];      //SOURCE,DESTINATIONしか使わないが、一応ほかの変数も保持しておく

                    sendStr = encodeTobase64(sendImage);
                    sendStr = sendStr.concat("<EOF>\n\n");
                    sendStr = "-1 "+"start "+SOURCE+DESTINATION+sendStr;

                    PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
                    Charset.forName("UTF-8").encode(sendStr);

                }else if(message.startsWith("data 1 2")){   //タブレットで受信だったならば
                    strs = message.split(" ");
                    String COMMAND = strs[0];
                    String SOURCE = "2 ";
                    String FILE_PATH = strs[2];
                    String DESTINATION = "1 ";
                    String DATA = strs[4];      //SOURCE,DESTINATIONしか使わないが、一応ほかの変数も保持しておく
                    decodeBase64(DATA);
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            return false;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            ERROR_CODE = 114;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            //なにはともあれ
            return false;
        }
    }

    /**
     * Bitmapイメージをbase64へエンコードします(JPEG変換)
     * @param image
     * @return
     */
    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        //String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        String imageEncoded = android.util.Base64.encodeToString(b,android.util.Base64.URL_SAFE);

        //imageEncoded= imageEncoded.replace(System.getProperty("line.separator"), "");
        //Logger.debugEntire(imageEncoded);
        return imageEncoded;
    }
    /**
     * base64文字列をBitmapイメージへデコードします
     * @param input
     * @return
     */
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = android.util.Base64.decode(input, Base64.URL_SAFE);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
