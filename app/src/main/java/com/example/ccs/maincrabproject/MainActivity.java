package com.example.ccs.maincrabproject;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private CardContentFragment ccf;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private Intent takePictureIntent;

    private static final String INPUT_NAME = "x";
    private static final String OUTPUT_NAME = "y_pred";
    private static final String MODEL_FILE = "file:///android_asset/crab-model2.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    private static Classification res;

    private static Classifier mClassifiers;

    private static final int INPUT_SIZE = 128;

    private String filename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ccf = new CardContentFragment();
        loadModel();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("crabmodels");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

//                Intent i = new Intent(getBaseContext(), CameraActivity.class);
//                startActivity(i);

                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.ccs.maincrabproject.fileprovider", photoFile);
                System.out.println(photoURI.getPath());
                this.filename = photoFile.getAbsoluteFile().toString();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("filename", photoFile.getAbsoluteFile());
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            System.out.println(this.filename);
            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Bitmap bitmap = BitmapFactory.decodeFile(this.filename);

            if(bitmap != null){
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, INPUT_SIZE, INPUT_SIZE);

                res = mClassifiers.recognize(convpixels(resizedBitmap));

                String text = "";
                if (res.getLabel() == null) {
                    text += "error";
                } else {
                    //else output its name
                    text += "Your crab is a ";
                    text += String.format("%s: %f\n", res.getLabel(),
                            res.getConf());
                }
                Toast.makeText(getBaseContext(), "Classified As: " + text, Toast.LENGTH_LONG).show();
                uploadImage(this.filename);
            }else{
                Toast.makeText(getBaseContext(), "Error file not found", Toast.LENGTH_LONG).show();
            }

            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "crab-" + timeStamp + "-";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/CrabApp");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(ccf, "Uploaded");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                System.out.println("connected");
                ccf.adapter.clearCrabs();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    Classification cla = new Classification();
                    Map map = (Map<String, Float>) locationSnapshot.child("result").child("othval").getValue();
                    cla.setOthval(map);
                    cla.setLabel(locationSnapshot.child("result").child("label").getValue().toString());
                    cla.setConf(Float.parseFloat(locationSnapshot.child("result").child("conf").getValue().toString()));
                    CrabModel crab = new CrabModel();
                    crab.setId(locationSnapshot.child("id").getValue().toString());
                    crab.setClassifier(locationSnapshot.child("classifier").getValue().toString());
                    crab.setFileurl(locationSnapshot.child("fileurl").getValue().toString());
                    crab.setProbval(Float.parseFloat(locationSnapshot.child("probval").getValue().toString()));
                    crab.setResult(cla);
                    ccf.adapter.addCrabs(crab);
                    System.out.println("location: " + crab.getId() + " Classifier: " + crab.getClassifier() + " Classification: " + crab.getResult().getOthval()); //log
                }

                System.out.println(ccf.adapter.getItemCount());

                ccf.adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void uploadImage(String filename){
        final Uri filePath = Uri.fromFile(new File(filename));
        final String dbfilename = "images/" + filename.substring(filename.lastIndexOf("/")+1);
        final StorageReference riversRef = mStorageRef.child(dbfilename);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        //progressDialog.show();

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
                        //path = taskSnapshot.getDownloadUrl().getPath();

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
                //progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        });
    }

    private static float[] convpixels(Bitmap bmp){
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
}
