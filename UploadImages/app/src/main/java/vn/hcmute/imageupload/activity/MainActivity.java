package vn.hcmute.imageupload.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hcmute.imageupload.Model.ImageUpload;
import vn.hcmute.imageupload.Network.retrofit.ServiceAPI;
import vn.hcmute.imageupload.R;
import vn.hcmute.imageupload.utils.Const;
import vn.hcmute.imageupload.utils.RealPathUtil;

public class MainActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 100;
    // Quyền lưu trữ cho Android dưới API 33
    // Quyền lưu trữ cho Android dưới API 33
    public static String[] storage_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Quyền lưu trữ cho Android 13 (API 33 trở lên)
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_33_and_above = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO
    };

    // Hàm trả về quyền cần thiết tuỳ vào phiên bản Android
    public static String[] permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return storage_permissions_33_and_above; // Sử dụng quyền mới từ API 33 trở lên
        } else {
            return storage_permissions; // Sử dụng quyền cũ cho các phiên bản trước API 33
        }
    }
    private Button btnChoose, btnUpload;
    private ImageView imageViewUpload, imageViewChoose;
    private EditText editTextUserName;
    private TextView textViewUsername;
    private Uri mUri;
    String userId;
    private ProgressDialog mProgressDialog;
    public static final String TAG = MainActivity.class.getName();

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //request code
                        Intent data = result.getData();
                        if (data == null) {
                            return;

                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageViewChoose.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }


    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Nhận Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            userId = intent.getStringExtra("id");
            Log.d("MainActivity", "User ID nhận được: " + userId);
        }
        //gọi hàm ánh xạ
        AnhXa();
        //khỏi tạo Progressbar
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Please wait upload .... ");
        //bắt sự kiện nút chọn ảnh
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                //Choose image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUri != null) {
                    UploadImage1();
                }
            }
        });
    }

    private void AnhXa() {
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewUpload = findViewById(R.id.imgMultipart);
        editTextUserName = findViewById(R.id.editUserName);
        textViewUsername = findViewById(R.id.tvUsername);
        imageViewChoose = findViewById(R.id.imgChoose);
    }

    // Kiểm tra quyền và yêu cầu quyền nếu chưa có
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        // Kiểm tra quyền trên Android 13 (API 33 trở lên) và các phiên bản mới hơn (API 34, 35)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(permissions(), MY_REQUEST_CODE);
            }
        } else {
            // Kiểm tra quyền trên các phiên bản Android cũ hơn (trước API 33)
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(permissions(), MY_REQUEST_CODE);
            }
        }
    }

    // Xử lý kết quả cấp quyền từ người dùng
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Quyền bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        mActivityResultLauncher.launch(intent); // Thay thế startActivityForResult
    }


    public void UploadImage1() {
        // Kiểm tra ảnh đã chọn chưa
        if (mUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh trước", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra userId
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog.show();
        //khai báo biến và setText nếu có
        String username = editTextUserName.getText().toString().trim();
        RequestBody requestUsername = RequestBody.create(MediaType.parse("multipart/form-data"), username);
        // create RequestBody instance from file
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);
        Log.e("ffff", IMAGE_PATH);
        File file = new File(IMAGE_PATH);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part partbodyavatar = MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);

        //gọi Retrofit
        ServiceAPI.serviceapi.upload(requestUsername, partbodyavatar).enqueue(new Callback<List<ImageUpload>>() {
            @Override
            public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                mProgressDialog.dismiss();
                List<ImageUpload> imageUpload = response.body();
                if (imageUpload. size() > 0) {
                    for (int i = 0; i <imageUpload. size() ; i++) {
                        textViewUsername.setText(imageUpload.get(i).getUsername());
                        Glide.with(MainActivity. this)
                                .load(imageUpload.get(i).getAvatar()).into(imageViewUpload);
                        Toast.makeText(MainActivity. this, "Thành công", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity. this, "Thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                mProgressDialog.dismiss();
                Log.e("TAG", t.toString());
                Toast.makeText( MainActivity. this, "Goi API thất bại", Toast.LENGTH_LONG).show();
            }
        });


    }
}