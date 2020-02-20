package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import  java.util.regex.Pattern;
import  java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    String imageSrc="";
    Bitmap downloadedImage;
    String imageName="";

    ArrayList<String> celebrityImageURLs = new ArrayList<String>();
    ArrayList<String> celebrityNames = new ArrayList<String>();
    ArrayList<String> options = new ArrayList<String>();

    ArrayList<Bitmap> celebrityImages = new ArrayList<Bitmap>();

    int positionOfCorrectOption=0;
    DownloadImage downloadImage;

    int chosenCeleb=0;

    Button button1;
    Button button2;
    Button button3;
    Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        String resultReturned = null;
        DownloadHTMLContent downloadHTMLContent = new DownloadHTMLContent();
        try {
            resultReturned = downloadHTMLContent.execute("http://www.posh24.se/kandisar").get();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Log.i("HTML Returned", resultReturned);

        /**
         * returns image urls and
         * passes it to DownloadImage class
         **/
        //Pattern p = Pattern.compile("img src=\"(.*?)\"");
        Pattern p = Pattern.compile("class=\"image\">\n" +
                "\t\t\t\t\t\t<img src=\"(.*?)\"");
        Matcher m = p.matcher(resultReturned);

        while(m.find()){
            imageSrc = m.group(1);
            Log.i("IMAGE SOURCE:", imageSrc);
            celebrityImageURLs.add(imageSrc);
            try {
                downloadImage = new DownloadImage();
                downloadedImage = downloadImage.execute(imageSrc).get();
                celebrityImages.add(downloadedImage);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.i("Total Image URLs", Integer.toString(celebrityImageURLs.size()));
        Log.i("Total Bitmaps", Integer.toString(celebrityImages.size()));


        /**
         * returns image names
         **/
        p = Pattern.compile("alt=\"(.*?)\"");
        m = p.matcher(resultReturned);

        while (m.find()){
            imageName = m.group(1);
            Log.i("IMAGE NAME:", imageName);
            celebrityNames.add(imageName);
        }
        Log.i("Celeb Names", celebrityNames.toString());

        GenerateQuestions();
    }

    public void GenerateQuestions(){

        //options = new String[4];

        Random random = new Random();
        chosenCeleb = random.nextInt(celebrityNames.size());
        imageView.setImageBitmap(celebrityImages.get(chosenCeleb));

        positionOfCorrectOption = random.nextInt(4);
        Log.i("Position", Integer.toString(positionOfCorrectOption));
        int randomOptions;

        for(int j=0; j<4; j++){
            if(j == positionOfCorrectOption){
                options.add(j, celebrityNames.get(chosenCeleb));
                //options[j] = celebrityNames.get(chosenCeleb);
            }
            else {
                randomOptions = random.nextInt(celebrityNames.size());
                Log.i("Random Options", Integer.toString(randomOptions));
                while(randomOptions == chosenCeleb) {
                    randomOptions = random.nextInt(celebrityNames.size());
                }
                options.add(j, celebrityNames.get(randomOptions));
                //options[j] = celebrityNames.get(randomOptions);
            }
        }
        button1.setText(options.get(0));
        button2.setText(options.get(1));
        button3.setText(options.get(2));
        button4.setText(options.get(3));
    }

    public void OptionSelected(View view){
        Button button = (Button) view;
        String tag = (String) button.getTag();
        Log.i("TAG", tag);

        if(Integer.parseInt(tag) == positionOfCorrectOption){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Wrong :(", Toast.LENGTH_SHORT).show();
        }

        GenerateQuestions();
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return  bitmap;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadHTMLContent extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection httpURLConnection;
            int data;
            String result = "";

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                data = reader.read();
                while (data != -1){

                    char current = (char) data;
                    result += current;

                    data = reader.read();
                }
                return  result;
            }
            catch (MalformedURLException e){
                e.printStackTrace();
                return "MalformedURLException";
            }
            catch (IOException e) {
                e.printStackTrace();
                return "IOException";
            }
        }
    }
}