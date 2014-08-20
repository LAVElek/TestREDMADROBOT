package lav.testredmadrobot;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ActivityPhotos extends Activity {

    private TextView tvNoPhoto;
    private ProgressBar pbLoadProgress;
    private GridView gvPhotosList;
    private Button btnGenerateCollage;
    private ImageAdapter mListImage;

    private String userNik = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        tvNoPhoto = (TextView)findViewById(R.id.tvNoData);
        pbLoadProgress = (ProgressBar)findViewById(R.id.pbLoadProgress);
        gvPhotosList = (GridView)findViewById(R.id.gvPhotosList);
        btnGenerateCollage = (Button)findViewById(R.id.btnGenerateCollage);
        gvPhotosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mListImage.getItemChosed(position)) {
                    mListImage.setItemChosed(position, true);
                    mListImage.notifyDataSetChanged();
                }
                else {
                    mListImage.setItemChosed(position, false);
                    mListImage.notifyDataSetChanged();
                }
            }
        });

        // ник пользователя
        userNik = getIntent().getStringExtra(getResources().getString(R.string.nik_tag));

        mListImage = new ImageAdapter(this);
        gvPhotosList.setAdapter(mListImage);

        new InstagramLoader().execute(userNik);
    }

    public void btnGenerateCollageClick(View view) {
        if (mListImage.getCountChoiseItem() == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_select_photo), Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> selectedUrl = new ArrayList<String>();
        for(int i = 0; i < mListImage.getCount(); i++) {
            if (mListImage.getItemChosed(i)) {
                selectedUrl.add((String)mListImage.getItem(i));
            }
        }
        Intent intent = new Intent(this, ActivityCollage.class);
        intent.putStringArrayListExtra("photos_url", selectedUrl);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------
    private class InstagramLoader extends AsyncTask<String, JSONObject, Void>{

        private final String TAG_QUERY_STR = "QUERY_STR";
        private final String TAG_USER_ID = "USER_ID";

        private final String INSTAGRAM_API_URL = "https://api.instagram.com/v1"; // api instagram
        private final String FIND_USERS = "/users/search?q=" + TAG_QUERY_STR + "&client_id=" + getResources().getString(R.string.CLIENT_ID); // запрос на поиск юзеров
        private final String GET_MEDIA = "/users/" + TAG_USER_ID + "/media/recent/?client_id=" + getResources().getString(R.string.CLIENT_ID); // запрос медиа контента(фото + видео) юзера

        private String url_str;
        private boolean isFindUser = false;

        @Override
        protected Void doInBackground(String... params) {

            String jsonStr = "";
            String userID = "";
            JSONObject users = null;
            JSONObject media = null;
            URL request;
            HttpsURLConnection httpsURLConnection;

            try {
                url_str = INSTAGRAM_API_URL + FIND_USERS.replace(TAG_QUERY_STR, params[0]);

                request = new URL(url_str);
                httpsURLConnection = (HttpsURLConnection) request.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setDoInput(true);

                jsonStr = streamToString(httpsURLConnection.getInputStream());
                httpsURLConnection.disconnect();
                users = new JSONObject(jsonStr);

                // смотрим есть ли пользователь с таким ником
                isFindUser = false;
                JSONArray userList = users.getJSONArray("data");
                if ((userList == null) || (userList.length() == 0)){
                    return null;
                }
                // первый в списке - с максимальным совпадением
                JSONObject jobj = userList.getJSONObject(0);
                if (jobj.getString("username").equalsIgnoreCase(params[0])){
                    userID = jobj.getString("id");
                    isFindUser = true;
                }
                else{
                    return null;
                }

                // вытаскиваем фотки
                url_str = INSTAGRAM_API_URL + GET_MEDIA.replace(TAG_USER_ID, userID);
                JSONArray data;
                JSONObject item;
                do {
                    request = new URL(url_str);
                    httpsURLConnection = (HttpsURLConnection) request.openConnection();
                    httpsURLConnection.setRequestMethod("GET");
                    httpsURLConnection.setDoInput(true);

                    jsonStr = streamToString(httpsURLConnection.getInputStream());
                    httpsURLConnection.disconnect();
                    media = new JSONObject(jsonStr);
                    data = media.getJSONArray("data");
                    for(int i = 0; i < data.length(); i++){
                        item = data.getJSONObject(i);
                        // берем только картинки
                        if (item.getString("type").equalsIgnoreCase("image")){
                            mListImage.add(item.getJSONObject("images").getJSONObject("thumbnail").getString("url"));
                        }
                    }
                } while (!(url_str = media.getJSONObject("pagination").getString("next_url")).isEmpty());
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvNoPhoto.setVisibility(View.GONE);
            gvPhotosList.setVisibility(View.GONE);
            pbLoadProgress.setVisibility(View.VISIBLE);
            btnGenerateCollage.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pbLoadProgress.setVisibility(View.GONE);
            // не найден пользователь
            if (!isFindUser){
                tvNoPhoto.setVisibility(View.VISIBLE);
                tvNoPhoto.setText(getResources().getString(R.string.no_find_user));
                return;
            }
            // отсутствуют фотки
            if (mListImage.getCount() == 0) {
                tvNoPhoto.setVisibility(View.VISIBLE);
                tvNoPhoto.setText(getResources().getString(R.string.no_find_photos));
                return;
            }
            gvPhotosList.setVisibility(View.VISIBLE);
            btnGenerateCollage.setVisibility(View.VISIBLE);
        }

        private String streamToString(InputStream is) throws IOException {
            String string = "";

            if (is != null) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    reader.close();
                } finally {
                    is.close();
                }

                string = stringBuilder.toString();
            }

            return string;
        }
    }
}
