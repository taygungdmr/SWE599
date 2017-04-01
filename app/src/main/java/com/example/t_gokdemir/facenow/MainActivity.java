package com.example.t_gokdemir.facenow;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("da993076c30247acb3903f9a2d028528");
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Bitmap myBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.tom);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(myBitmap);

        Button btnProcess = (Button)findViewById(R.id.btnProcess);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick (View v) {

                detectAndFrame(myBitmap);
            }
        });
    }

    private void detectAndFrame(final Bitmap myBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream,String,Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {

            private ProgressDialog progress = new ProgressDialog(MainActivity.this);

            @Override
            protected void onPostExecute(Face[] faces) {
                progress.dismiss();
                if(faces == null) return;
                imageView.setImageBitmap(drawFaceRectangleOnBitmap(myBitmap,faces));

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                progress.setMessage(values[0]);
            }

            @Override
            protected Face[] doInBackground(InputStream... params) {
                publishProgress("Detecting...");
                try{
                    Face[] result = faceServiceClient.detect(params[0],true,false,null);
                    if(result == null){
                        publishProgress("Detection finished... Nothing is detected!");
                        return null;
                    }

                    publishProgress(String.format("Detection finished. %d face (s) detected", result.length));
                    return result;

                }catch (ClientException e) {
                    e.printStackTrace();

                }catch (Exception e){
                    publishProgress("Detection failed.");
                    return null;
                }
            }
        };

        detectTask.execute(inputStream);

    }

    private Bitmap drawFaceRectangleOnBitmap(Bitmap myBitmap, Face[] faces){
        Bitmap bitmap = myBitmap.copy(Bitmap.Config.ARGB_8888,true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        int strokeWidth=12;
        paint.setStrokeWidth(strokeWidth);
        if(faces != null)
        {
            for (Face face:faces)
            {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left+faceRectangle.width,
                        faceRectangle.top+faceRectangle.height,
                        paint);
            }
        }

        return bitmap;
    }
}
