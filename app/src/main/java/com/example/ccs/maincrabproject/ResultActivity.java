package com.example.ccs.maincrabproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class ResultActivity extends Activity {

    private ImageView iview;
    private TextView tview;
    private Button clsbtn;
    private TextView oview;
    private Button uplbtn;
    private Button dscbtn;
    private Classifier mClassifiers;
    private String path;

    private static final int INPUT_SIZE = 128;
    private static final String INPUT_NAME = "x";
    private static final String OUTPUT_NAME = "y_pred";
    private static String filename;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Uri uri;

    private static final String MODEL_FILE = "file:///android_asset/crab-model2.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    private Classification res;
    private List<CrabModel> crabmodels;

    private Executor executor = Executors.newSingleThreadExecutor();

    private float curScale = 1F;
    private float curRotate = 0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        iview = (ImageView) findViewById(R.id.resultimage);
        tview = (TextView) findViewById(R.id.resulttext);
        oview = (TextView) findViewById(R.id.othvalues);
        uplbtn = (Button) findViewById(R.id.upl_btn);
        dscbtn = (Button) findViewById(R.id.dsc_btn);
        crabmodels = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("crabmodels");

        Intent it = getIntent();
        String fname = it.getStringExtra("crabpic");
        this.filename = fname;

        loadModel();
        if(Uri.parse(fname) != null){
            Bitmap bitmap = BitmapFactory.decodeFile(fname);

            Bitmap bitmap2 = BitmapFactory.decodeFile(fname);

            Matrix mat = new Matrix();
            int bounding = dpToPx(350);

            float xScale = ((float) bounding) / bitmap2.getWidth();
            float yScale = ((float) bounding) / bitmap2.getHeight();
            float scale = (xScale <= yScale) ? xScale : yScale;
            mat.postScale(scale, scale);
            mat.preRotate(90);
            bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), mat, true);

            iview.setImageBitmap(bitmap2);

            if(bitmap != null){
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, INPUT_SIZE, INPUT_SIZE);

                this.res = mClassifiers.recognize(convpixels(resizedBitmap));

                String text = "";
                if (res.getLabel() == null) {
                    text += "error";
                } else {
                    //else output its name
                    text += "Your crab is a ";
                    text += String.format("%s: %f\n", this.res.getLabel(),
                            this.res.getConf());
                }
                tview.setText(text);
                oview.setText(this.res.getOthval().toString());
            }else{
                Toast.makeText(getBaseContext(), "Error file not found", Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(getBaseContext(), "Error file not found", Toast.LENGTH_LONG).show();
        }


        uplbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(filename);
            }
        });

        dscbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    private float[] convpixels(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        // Get 28x28 pixel data from bitmap
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] retPixels = new float[INPUT_SIZE * INPUT_SIZE * 3];

        for (int i = 0; i < pixels.length; ++i) {
            final int val = pixels[i];
            retPixels[i * 3 + 0] = (((val >> 16) & 0xFF) - 128) / 128;
            retPixels[i * 3 + 1] = (((val >> 8) & 0xFF) - 128) / 128;
            retPixels[i * 3 + 2] = ((val & 0xFF) - 128) / 128;
        }

        return retPixels;
    }

    public void uploadImage(String filename){
        final Uri filePath = Uri.fromFile(new File(filename));
        final String dbfilename = "images/" + filename.substring(filename.lastIndexOf("/")+1);
        final StorageReference riversRef = mStorageRef.child(dbfilename);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();

        riversRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        progressDialog.dismiss();
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String id = myRef.push().getKey();
                                System.out.println("Hello " + uri.toString());
                                CrabModel cbm = new CrabModel(id, res.getLabel(), res.getConf(), uri.toString(), res);
                                myRef.child(id).setValue(cbm);
                            }
                        });
                        path = taskSnapshot.getDownloadUrl().getPath();

                        //and displaying a success toast
                        Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
                        // Handle unsuccessful uploads
                        // ...
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                }
            });
    }

    private void loadModel(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mClassifiers = mClassifiers.create(getAssets(),
                            "crab-model2.pb", "labels.txt", INPUT_SIZE,
                            INPUT_NAME, OUTPUT_NAME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                    }
                });
    }
}
