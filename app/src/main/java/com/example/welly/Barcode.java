package com.example.welly;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class Barcode extends AppCompatActivity implements View.OnClickListener{
    Button scanBtn;
    TextView textView;
    String barcodeNum;
    ImageView btn_alram;
    String dataBarcode;
    String getData;

    String key1="1361c635ebfa4ffbb6b1";
    String key2="Svm%2B6xr32aOY87UpaVtoSRfzxu03yUkebwz3d2zw37g4v0QhHWhRyBmYXdbMEiFtdzkyTW%2FBzbzm5KdPswBHmw%3D%3D";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        scanBtn = findViewById(R.id.btn_scan);
        scanBtn.setOnClickListener(this);
        btn_alram = findViewById(R.id.btn_alram);
        btn_alram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Barcode.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        textView = findViewById(R.id.result2);
    }

    @Override
    public void onClick(View v) {
        scanCode();

    }
    private void scanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Scanning Result");
                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scanCode();
                    }
                }).setNegativeButton("finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        barcodeNum = result.getContents();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                dataBarcode = getXmlData1(barcodeNum);
                                getData = getXmlData2(dataBarcode);


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        textView.setText(getData);

                                    }
                                });
                            }
                        }).start();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();



            }
            else{
                Toast.makeText(this, "No Result", Toast.LENGTH_LONG).show();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    String getXmlData1(String barcodeNum){
        StringBuffer buffer=new StringBuffer();
        String str= barcodeNum;
        String product = URLEncoder.encode(str);


        String queryUrl="http://openapi.foodsafetykorea.go.kr/api/"+key1+"/C005/xml/1/1000" //요청 URL
                + "/BAR_CD=" + product;
        try{
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//테그 이름 얻어오기

                        if(tag.equals("row id=")) ;// 첫번째 검색결과
                        else if(tag.equals("PRDLST_NM")){
                            xpp.next();
                            buffer.append(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n"); //줄바꿈 문자 추가
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag= xpp.getName(); //테그 이름 얻어오기

                        if(tag.equals("row")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                        break;
                }

                eventType= xpp.next();
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return buffer.toString();//StringBuffer 문자열 객체 반환

    }//getXmlData method....

    String getXmlData2(String dataBarcode){
        StringBuffer buffer=new StringBuffer();



        if (dataBarcode.isEmpty()) {
            buffer.append("검색결과 없음\n");
            return buffer.toString();
        } else {
            String str= dataBarcode;
            String product = URLEncoder.encode(str);

            String queryUrl="http://apis.data.go.kr/1470000/HtfsInfoService/getHtfsItem?ServiceKey=" //요청 URL
                    + key2 + "&Prduct=" + product + "&type=xml";


            try{
                URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
                InputStream is= url.openStream(); //url위치로 입력스트림 연결

                XmlPullParserFactory factory= XmlPullParserFactory.newInstance();//xml파싱을 위한
                XmlPullParser xpp= factory.newPullParser();
                xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

                String tag;

                xpp.next();
                int eventType= xpp.getEventType();
                while( eventType != XmlPullParser.END_DOCUMENT ){
                    switch( eventType ){
                        case XmlPullParser.START_DOCUMENT:
                            buffer.append("파싱 시작...\n\n");
                            break;

                        case XmlPullParser.START_TAG:
                            tag= xpp.getName();//테그 이름 얻어오기

                            if(tag.equals("item")) ;// 첫번째 검색결과
                            else if(tag.equals("ENTRPS")){
                                buffer.append("업소명 : ");
                                xpp.next();
                                buffer.append(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n"); //줄바꿈 문자 추가
                            }
                            else if(tag.equals("PRDUCT")){
                                buffer.append("제품명 : ");
                                xpp.next();
                                buffer.append(xpp.getText());//category 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n");//줄바꿈 문자 추가
                            }

                            else if(tag.equals("DISTB_PD")){
                                buffer.append("보존및유통기한 :");
                                xpp.next();
                                buffer.append(xpp.getText());//mapx 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("  ,  "); //줄바꿈 문자 추가
                            }

                            else if(tag.equals("SRV_USE")){
                                buffer.append("섭취량 섭취방법 :");
                                xpp.next();
                                buffer.append(xpp.getText());//mapy 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n"); //줄바꿈 문자 추가
                            }

                            else if(tag.equals("INTAKE_HINT1")){
                                buffer.append("섭취시 주의사항 :");
                                xpp.next();
                                buffer.append(xpp.getText());//mapy 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n"); //줄바꿈 문자 추가
                            }
                            else if(tag.equals("MAIN_FNCTN")){
                                buffer.append("주된 기능 :");
                                xpp.next();
                                buffer.append(xpp.getText());//mapy 요소의 TEXT 읽어와서 문자열버퍼에 추가
                                buffer.append("\n"); //줄바꿈 문자 추가
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tag= xpp.getName(); //테그 이름 얻어오기

                            if(tag.equals("item")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                            break;
                    }

                    eventType= xpp.next();
                }

            } catch (Exception e){
                e.printStackTrace();
            }

        }


        buffer.append("검색 끝\n");
        return buffer.toString();//StringBuffer 문자열 객체 반환

    }//getXmlData method....

}