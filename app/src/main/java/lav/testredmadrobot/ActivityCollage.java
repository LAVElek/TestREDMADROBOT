package lav.testredmadrobot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ActivityCollage extends Activity {

    private final int DIALOG_NO_EMAIL = 1;
    private final int DIALOG_ERROR_SEND = 2;

    EditText edEMail;
    Button btnSendMail;
    ImageView imCollage;
    Bitmap mCollage;

    ArrayList<String> selectedURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        edEMail = (EditText)findViewById(R.id.edEmail);
        btnSendMail = (Button)findViewById(R.id.btnSend);
        imCollage = (ImageView)findViewById(R.id.imCollage);

        selectedURL = getIntent().getStringArrayListExtra("photos_url");

        // если зашли после поворота экрана
        if(savedInstanceState == null) {
            new DrawCollage().execute();
        }
        else {
            mCollage = savedInstanceState.getParcelable("collage");
            imCollage.setImageBitmap(mCollage);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("collage", mCollage);
    }

    protected Dialog onCreateDialog(int id){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.dialog_warning);
        switch(id){
            case DIALOG_NO_EMAIL:
                adb.setMessage(R.string.dialog_text_no_email);
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(R.string.OK, null);
                return adb.create();
            case DIALOG_ERROR_SEND:
                adb.setMessage(R.string.dialog_text_error_send);
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton(R.string.OK, null);
                return adb.create();
        }

        return super.onCreateDialog(id);
    }

    public void OnClickSend(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        if (edEMail.getText().toString().trim().isEmpty()) {
            showDialog(DIALOG_NO_EMAIL);
            return;
        }

        try {
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), mCollage, "test", null);
            Uri collageUri = Uri.parse(path);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{edEMail.getText().toString()});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Коллаж");
            intent.putExtra(Intent.EXTRA_TEXT, "Результат работы тестовой программы");
            intent.putExtra(Intent.EXTRA_STREAM, collageUri);
            intent.setType("multipart/mixed");
            startActivity(Intent.createChooser(intent, "Отправка письма..."));
            finish();
        }
        catch(Exception e){
            e.printStackTrace();
            showDialog(DIALOG_ERROR_SEND);
        }
    }

    private class DrawCollage extends AsyncTask<Void, Bitmap, Void>{
        final int SIZE_COLLAGE_PHOTO = 150;
        final int WIDTH_BETWEEN_PHOTOS = 5; // расстояние между фотографиями в пикселях

        int width, heigth, count_column, count_row, col, row;
        Canvas canva;

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap res = null;
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                for (int i = 0; i < selectedURL.size(); i++) {
                    url = new URL(selectedURL.get(i));
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoInput(true);
                    res = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
                    httpURLConnection.disconnect();
                    publishProgress(res);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // будем считать, что если изображений меньше либо равно 6, то делаем по 2 изображения в строку
            // в противном случае по 3
            count_column = (selectedURL.size() <= 6) ? 2 : 3;
            count_column = (selectedURL.size() < 2) ? 1 : count_column;
            count_row = (int)Math.ceil((double)selectedURL.size() / (double)count_column);
            width = SIZE_COLLAGE_PHOTO * count_column + WIDTH_BETWEEN_PHOTOS * (count_column - 1);
            heigth = SIZE_COLLAGE_PHOTO * count_row + WIDTH_BETWEEN_PHOTOS * (count_row - 1);

            mCollage = Bitmap.createBitmap(width, heigth, Bitmap.Config.RGB_565);
            canva = new Canvas(mCollage);
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.FILL);
            canva.drawRect(0, 0, width, heigth, p);
            col = 0;
            row = 0;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            imCollage.setImageBitmap(mCollage);
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            int pos_x, pos_y;

            pos_x = col * SIZE_COLLAGE_PHOTO + WIDTH_BETWEEN_PHOTOS * col;
            pos_y = row * SIZE_COLLAGE_PHOTO + WIDTH_BETWEEN_PHOTOS * row;
            col++;
            if (col == count_column) {
                col = 0;
                row++;
            }
            canva.drawBitmap(values[0], pos_x, pos_y, new Paint());
        }
    }

}
