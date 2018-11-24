package com.example.tatyana.natania;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddDealsActivity extends AppCompatActivity {


    EditText mTitleEt, mDescrEt;
    ImageView mPostIv;
    Button mUploadBtn;

    //Folder path for Firebase Storage
    String mStoragePath = "All_Image_Uploads/";
    //Root Database name for firebase database
    String mDatabasePath = "Data";

    //Creating URI
    Uri mFilePathUri;

    //Creating StorageReference and Database  reference
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    //ProgressDialog

    ProgressDialog mProgressDialog;

    //Image reques code for coosing image
    int IMAGE_REQUEST_CODE = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deals);

        //Actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");

        mTitleEt = findViewById(R.id.pTitleEt);
        mDescrEt = findViewById(R.id.pDescrrEt);
        mPostIv = findViewById(R.id.pImageIv);
        mUploadBtn = findViewById(R.id.pUploadBtn);

     //image click to choose image
        mPostIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating intent
                Intent intent = new Intent();
                //setting intent type as image to select image from phone storage
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
            }
        });

        //button click to upload data to farebase
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          //call method to upload data to firebase
                uploadDataToFerebase();
            }
        });

        //assing FirebaseStorage instance to storage reference object
        mStorageReference = FirebaseStorage.getInstance().getReference();
        //assin FirebaseDatabase instance with root database name
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(mDatabasePath);
       //progress dialog
       mProgressDialog = new ProgressDialog(AddDealsActivity.this);
    }
    private void uploadDataToFerebase() {
        //check whether filepathuri is enply or no
        if (mFilePathUri != null){
            //setting progress bar title
            mProgressDialog.setTitle("Uploading...");
            //show progress dialog
            mProgressDialog.show();
            //create second storageReference
            StorageReference storageReference2nd = mStorageReference.child(mStoragePath + System.currentTimeMillis() + "." + getFileExtension(mFilePathUri));

                    //adding addOnSucessListener to storageReference2nd
            storageReference2nd.putFile(mFilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get title
                            String mPostTitle = mTitleEt.getText().toString().trim();
                            //get description
                            String mPostDescr = mDescrEt.getText().toString().trim();
                            //hid progress dialog
                            mProgressDialog.dismiss();
                            //show toast that image is uploaded
                            Toast.makeText(AddDealsActivity.this,"Uploaded succes...", Toast.LENGTH_SHORT).show();
                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mPostTitle, mPostDescr, taskSnapshot.toString(),mPostTitle.toLowerCase());
                            //getting image upload id
                            String imageUploadId = mDatabaseReference.push().getKey();
                            //adding image i'ds child
                            mDatabaseReference.child(imageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(AddDealsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.setTitle("Uploading");
                        }
                    });
        }
        else {
            Toast.makeText(this,"Please select image or add image name", Toast.LENGTH_SHORT).show();
        }
    }

    //mathod to get the selected image file extension from file path uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        //returning the file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){
            mFilePathUri = data.getData();

            try {
                //getting selected image into bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePathUri);
                //setting bitmap into imageview
                mPostIv.setImageBitmap(bitmap);
            }
            catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
