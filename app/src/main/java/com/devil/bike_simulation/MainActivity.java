package com.devil.bike_simulation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ImageView iv;
    EditText number;
    TextView textNum;
    TextView textState;

    int flag=0;
    int sendFlag=0;

    int bike_num;
    int sendNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv=(ImageView)findViewById(R.id.iv);
        textNum=(TextView)findViewById(R.id.textNum);
        textState=(TextView)findViewById(R.id.textState);
        number=(EditText)findViewById(R.id.number);

        textNum.setText("空");
        textState.setText("未知");
    }



    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        float scaleSize = 1.0f;
        while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {
            scaleSize *= 2;
        }
        float sx = 1.0f / scaleSize;
        canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
        canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }

    public void generate(View view) {

        String input_string=number.getText().toString().trim();

        if(input_string.isEmpty())
        {
            sendFlag=0;
            Toast.makeText(this, "输入的车辆编号不能为空！！！", Toast.LENGTH_LONG).show();
            textNum.setText("空");
            textState.setText("未知");
            iv.setImageDrawable(null);
            return;
        }

        try {
            bike_num=Integer.parseInt(input_string);
        } catch (Exception e) {
            sendFlag=0;
            Toast.makeText(this, "请输入标准格式的编号！！！", Toast.LENGTH_LONG).show();
            iv.setImageDrawable(null);
            number.setText("");
            textNum.setText("空");
            textState.setText("未知");
            e.printStackTrace();
            return;
        }

        if(bike_num>10000){
            sendFlag=0;
            Toast.makeText(this, "请输入10000以内的编号！！！", Toast.LENGTH_LONG).show();
            iv.setImageDrawable(null);
            number.setText("");
            textNum.setText("空");
            textState.setText("未知");
            return;
        }

        sendFlag=1;
        sendNum=bike_num;
        number.setText("");
        textNum.setText(""+sendNum);
        textState.setText("未知");

        Bitmap qrBitmap = generateBitmap(sendNum+"",400, 400);
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap = addLogo(qrBitmap, logoBitmap);
        iv.setImageBitmap(bitmap);

        if(flag==0)
        {sendRequestConnection();}//网络连接，车辆编号状态轮询查询
        else{flag=1;}
    }



    //网络传输部分
    //0 车编号不存在
    //1 开锁状态
    //2  上锁状态
    private void sendRequestConnection(){

        new Thread(new Runnable() {
            private String HOST="67.209.186.100";//"67.209.186.100";//IP地址需要根据现场环境进行修改
            private int PORT=10001;
            private int stateCode;
            private int p_num;
            private String message=null;
            private PrintWriter printWriter;
            private BufferedReader in;

            @Override
            public void run()
            {


            while(true)
            {

            if(sendFlag==1) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(HOST, PORT), 10000);//设置连接请求超时时间10 s
                    socket.setSoTimeout(10000);//设置读操作超时时间10 s
                    //Socket socket = new Socket(HOST, PORT);
                    //socket.setSoTimeout(10000);

                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));//输入流

                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);

                    p_num=bike_num;
                    printWriter.println("bike_num" + " " + p_num);

                    message = in.readLine();


                        if (message == null) {
                            stateCode=-1;
                            Log.v("stateCode", "网络连接中断");

                            if(p_num==bike_num && sendFlag==1){
                                showTimeOut();// 网络连接错误
                            }

                        } else {
                            message = message.trim();
                            stateCode = Integer.valueOf(message);
                        }


                        //返回状态码，0为失败 不存在该编号， 1为开锁，  2为上锁
                        if (stateCode == 0) {
                            if(p_num==bike_num && sendFlag==1){
                                showError();
                            }
                        }

                        if(stateCode==1 || stateCode==2) {
                            if(p_num==bike_num && sendFlag==1) {
                                showSuccess(stateCode);
                            }
                        }

                } catch (IOException e) {
                    showTimeOut();
                    e.printStackTrace();
                }

            }//if 结束

                try {
                    Thread.sleep(3000);  //休眠 3 秒
                }catch (InterruptedException e){e.printStackTrace();}

            } //while 结束

            }
        }).start();
    }

    private void showSuccess(final int stateCode)
    {   //1  2
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "查询成功！！！", Toast.LENGTH_LONG).show();

                if (stateCode==1){
                    textState.setText("已开锁");
                }else{
                    textState.setText("已上锁");
                }

            }
        });
    }

    private void showError()
    {   // 0
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "车编号不存在！！！", Toast.LENGTH_LONG).show();

                sendFlag=0;
                textNum.setText("空");
                textState.setText("未知");
                iv.setImageDrawable(null);
            }
        });
    }

    private void showTimeOut()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "网络连接错误！！！", Toast.LENGTH_LONG).show();
                textState.setText("未知");
            }
        });
    }





}
