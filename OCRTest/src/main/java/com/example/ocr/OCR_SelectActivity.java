package com.example.ocr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class OCR_SelectActivity extends Activity {
    private static final String TAG = OCR_SelectActivity.class.getSimpleName();

    RelativeLayout rl_camera, rl_gallery;

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;

    String camera_image_name;
    File flCaptureImage;
    private SharedPreferences permissionStatus;
    public String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    public String TESSDATA = "tessdata";
    private boolean sentToSettings = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_select_activity);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        prepareDirectoryForTesseract();

        rl_camera = (RelativeLayout) findViewById(R.id.rl_camera);
        rl_gallery = (RelativeLayout) findViewById(R.id.rl_gallery);


        rl_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(OCR_SelectActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(OCR_SelectActivity.this, Manifest.permission.CAMERA)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(OCR_SelectActivity.this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs Camera permission.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(OCR_SelectActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA, false)) {
                        //Previously Permission Request was cancelled with 'Dont Ask Again',
                        // Redirect to Settings after showing Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(OCR_SelectActivity.this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs Camera permission.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                sentToSettings = true;
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 1);
                                Toast.makeText(getBaseContext(), "Go to Permissions to Grant Camera", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        //just request the permission
                        ActivityCompat.requestPermissions(OCR_SelectActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                    }


                    SharedPreferences.Editor editor = permissionStatus.edit();
                    editor.putBoolean(Manifest.permission.CAMERA, true);
                    editor.commit();


                } else {
                    //You already have the permission, just go ahead.
                    File tempfile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "OCR");

                    if (!tempfile.exists()) {
                        tempfile.mkdirs();
                    }

                    camera_image_name = "TS_" + System.currentTimeMillis() + ".png";

                    flCaptureImage = new File(tempfile.getAbsolutePath(), camera_image_name);

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(flCaptureImage));
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                }


            }
        });

        rl_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (ActivityCompat.checkSelfPermission(OCR_SelectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(OCR_SelectActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(OCR_SelectActivity.this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs storage permission.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(OCR_SelectActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                        //Previously Permission Request was cancelled with 'Dont Ask Again',
                        // Redirect to Settings after showing Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(OCR_SelectActivity.this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs storage permission.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                sentToSettings = true;
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, 1);
                                Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        //just request the permission
                        ActivityCompat.requestPermissions(OCR_SelectActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }


                    SharedPreferences.Editor editor = permissionStatus.edit();
                    editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
                    editor.commit();


                } else {
                    //You already have the permission, just go ahead.

                    Intent int_album = new Intent(Intent.ACTION_PICK);
                    int_album.setType("image/*");
                    startActivityForResult(int_album, REQUEST_GALLERY);
                }


            }
        });
    }

    private void prepareDirectoryForTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory failed");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }

    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    File file = new File(getPath(selectedImage));
                    Log.d("Path", "" + file.getAbsolutePath());
                    //file_bitmap(file);
                    Intent call_crop_activity = new Intent(OCR_SelectActivity.this, OCR_CropActivity.class);
                    call_crop_activity.putExtra("DataFrom", "Gallery");
                    call_crop_activity.putExtra("FilePath", file.getAbsolutePath());
                    startActivity(call_crop_activity);
                }
                break;

            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {

                    //Bitmap camara_image = (Bitmap) data.getExtras().get("data");
                    Intent call_crop_activity = new Intent(OCR_SelectActivity.this, OCR_CropActivity.class);
                    call_crop_activity.putExtra("DataFrom", "Camera");
                   /* call_crop_activity.putExtra("BitmapImage", camara_image);*/
                    call_crop_activity.putExtra("FilePath", flCaptureImage.getAbsolutePath());
                    startActivity(call_crop_activity);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }


}