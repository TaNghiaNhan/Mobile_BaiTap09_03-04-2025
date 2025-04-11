package vn.hcmute.imageupload.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.hcmute.imageupload.R;

public class FirstMainActivity extends AppCompatActivity {

    private ImageView imageV;
    private TextView txtV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_main);
        imageV = findViewById(R.id.imgAvatar);
        txtV = findViewById(R.id.tvUserId);
        String userId = txtV.getText().toString();
        imageV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstMainActivity.this, MainActivity.class);
                intent.putExtra("id", userId);
                startActivity(intent);
                finish();
            }
        });
    }
}