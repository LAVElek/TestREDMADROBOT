package lav.testredmadrobot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class ActivityMain extends Activity {

    EditText edInstagramNik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edInstagramNik = (EditText)findViewById(R.id.edInstagramNik);
    }

    public void onBtnGetCollageClick(View view) {
        Intent intent = new Intent(this, ActivityPhotos.class);
        intent.putExtra(getResources().getString(R.string.nik_tag), edInstagramNik.getText().toString());
        startActivity(intent);
    }
}
