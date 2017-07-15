package com.example.ocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageWeakPixelInclusionFilter;

/**
 * Created by imac309 on 19/11/16.
 */
public class OCR_CropActivity extends Activity {
    private static final String TAG = OCR_CropActivity.class.getSimpleName();
    String file_path;
    CropImageView crop_imageView;

    RelativeLayout rl_generate_data;

    int mDegree = 0;
    boolean is_crop = false;
    Bitmap scaled_bitmap;
    Bitmap crop_image_bitmap;

    ImageView iv_generated_imageview;
    ImageView iv_back, iv_rotate, iv_crop;

    Switch sw_enhance;

    Context context;
    String data_from;
    Bitmap generated_bitmap;

    Bitmap final_image_bitamp;

    public TessBaseAPI baseApi;
    public String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    public String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";

    int removedkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_crop_activity);

        context = OCR_CropActivity.this;

        baseApi = new TessBaseAPI();
        baseApi.init(DATA_PATH, DEFAULT_SOURCE_LANGUAGE_CODE, TessBaseAPI.OEM_DEFAULT);
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);

        iv_rotate = (ImageView) findViewById(R.id.iv_rotate);
        iv_crop = (ImageView) findViewById(R.id.iv_crop);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        sw_enhance = (Switch) findViewById(R.id.sw_enhance);

        rl_generate_data = (RelativeLayout) findViewById(R.id.rl_generate_data);

        iv_generated_imageview = (ImageView) findViewById(R.id.iv_generated_imageview);

        data_from = getIntent().getStringExtra("DataFrom");
        if (data_from.equalsIgnoreCase("Gallery")) {
            file_path = getIntent().getStringExtra("FilePath");
            generated_bitmap = BitmapFactory.decodeFile(file_path);
        } else if (data_from.equalsIgnoreCase("Camera")) {

            file_path = getIntent().getStringExtra("FilePath");
            generated_bitmap = BitmapFactory.decodeFile(file_path);

            //Intent intent = getIntent();
            //generated_bitmap = intent.getParcelableExtra("BitmapImage");
            //generated_bitmap = generated_bitmap.copy(generated_bitmap.getConfig(), true);
        }

        /*int nh = (int) (generated_bitmap.getHeight() * (1024.0 / generated_bitmap.getWidth()));
        scaled_bitmap = Bitmap.createScaledBitmap(generated_bitmap, 1024, nh, true);*/

        scaled_bitmap = resizeImageDisplay(generated_bitmap);

        crop_imageView = (CropImageView) findViewById(R.id.crop_imageView);
        crop_imageView.setScaleType(CropImageView.ScaleType.CENTER_INSIDE);
        crop_imageView.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        crop_imageView.setCropShape(CropImageView.CropShape.RECTANGLE);
        crop_imageView.setImageBitmap(scaled_bitmap);

        iv_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                crop_image_bitmap = crop_imageView.getCroppedImage();
                final_image_bitamp = crop_image_bitmap;
                crop_imageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
                    @Override
                    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {


                        iv_crop.setVisibility(View.GONE);
                        iv_rotate.setVisibility(View.GONE);
                        crop_imageView.setVisibility(View.GONE);

                        iv_generated_imageview.setVisibility(View.VISIBLE);
                        rl_generate_data.setVisibility(View.VISIBLE);
                        sw_enhance.setVisibility(View.VISIBLE);

                        iv_generated_imageview.setImageBitmap(crop_image_bitmap);

                        is_crop = true;
                    }
                });
                crop_imageView.getCroppedImageAsync();
            }
        });

        sw_enhance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    GPUImage gpuImage = new GPUImage(context);
                    gpuImage.setImage(crop_image_bitmap);
                    gpuImage.setFilter(new GPUImageWeakPixelInclusionFilter());
                    final_image_bitamp = gpuImage.getBitmapWithFilterApplied();
                    iv_generated_imageview.setImageBitmap(final_image_bitamp);
                } else {
                    final_image_bitamp = crop_image_bitmap;
                    iv_generated_imageview.setImageBitmap(crop_image_bitmap);
                }
            }
        });


        iv_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDegree = mDegree + 90;
                crop_imageView.setRotatedDegrees(mDegree);
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        rl_generate_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new OcrRecognizeAsyncTask_Gallery(context, baseApi, final_image_bitamp).execute();
            }
        });
    }

    final class OcrRecognizeAsyncTask_Gallery extends AsyncTask<Void, Void, Boolean> {

        private TessBaseAPI baseApi;
        private Bitmap bitmap_data;

        private ProgressDialog bitmap_dialog;

        Context context;
        String textResult = "";

        OcrRecognizeAsyncTask_Gallery(Context activity, TessBaseAPI baseApi, Bitmap data) {
            this.context = activity;
            this.baseApi = baseApi;
            this.bitmap_data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bitmap_dialog = new ProgressDialog(context);
            bitmap_dialog.setTitle("Please wait");
            bitmap_dialog.setCancelable(false);
            bitmap_dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Bitmap bitmap = bitmap_data;

            TextRecognizer treg = new TextRecognizer.Builder(OCR_CropActivity.this).build();
            if (treg.isOperational()) {

                SparseArray<TextBlock> str = treg.detect(new Frame.Builder().setBitmap(final_image_bitamp).build());
                ArrayList<TextBlock> textbloackarr = new ArrayList();
                ArrayList<String> result_data = new ArrayList<>();
                while (str.size() > 0) {
                    textbloackarr.add(getTopOne(str));
                    str.remove(removedkey);
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < textbloackarr.size(); i++) {
                    sb.append(((TextBlock) textbloackarr.get(i)).getValue());
                    sb.append("\n");
                    result_data.add(((TextBlock)textbloackarr.get(i)).getValue());
                }
                treg.release();
                textResult = sb.toString();
                return true;
            } else {
                try {
                    baseApi.setImage(bitmap);
                    textResult = baseApi.getUTF8Text();
                    ArrayList<String> ocrChar = new ArrayList<>();

                    int count = 0;
                    /*Pix pix_image= baseApi.getThresholdedImage();
                    Bitmap bitmap_image = WriteFile.writeBitmap(pix_image);*/
                    //baseApi.setImage(pix_image);
                    //textResult = baseApi.getUTF8Text();
                    final ResultIterator iterator = baseApi.getResultIterator();
                    iterator.begin();
                    do {
                        String lastUTF8Text = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                        float lastConfidence = iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                        DecimalFormat df = new DecimalFormat("#.00");
                        if (lastUTF8Text != null) {
                            ocrChar.add(lastUTF8Text + " [" + df.format(lastConfidence) + "] ");
                        }
                        Log.d("ocrChar", "" + ocrChar.get(count));
                        count++;
                    }
                    while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));

                    // Check for failure to recognize text
                    if (textResult == null || textResult.equals("")) {
                        return false;
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                    try {
                        baseApi.clear();
                    } catch (NullPointerException e1) {
                        Log.e(TAG, e1.toString());
                    }
                    return false;
                }
                Log.d("textResult", "" + textResult);
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (baseApi != null) {
                baseApi.clear();
            }
            bitmap_dialog.dismiss();

            if (result) {
                Intent call_result_activity = new Intent(OCR_CropActivity.this, OCR_ResultActivity.class);
                call_result_activity.putExtra("ResultData", textResult);
                startActivity(call_result_activity);
            } else {
                Toast.makeText(context, "No data Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private TextBlock getTopOne(SparseArray<TextBlock> sparearry) {
        TextBlock topone = (TextBlock) sparearry.get(sparearry.keyAt(0));
        int top = ((TextBlock) sparearry.get(sparearry.keyAt(0))).getBoundingBox().top;
        int left = ((TextBlock) sparearry.get(sparearry.keyAt(0))).getBoundingBox().left;
        removedkey = sparearry.keyAt(0);
        for (int i = 1; i < sparearry.size(); i++) {
            Rect rc = ((TextBlock) sparearry.get(sparearry.keyAt(i))).getBoundingBox();
            if (rc.top < top) {
                topone = (TextBlock) sparearry.get(sparearry.keyAt(i));
                removedkey = sparearry.keyAt(i);
                top = rc.top;
                left = rc.left;
            }
        }
        return topone;
    }

    private Bitmap resizeImageDisplay(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            return Bitmap.createScaledBitmap(bitmap, 1280, (int) ((((double) 1280) / ((double) bitmap.getWidth())) * ((double) bitmap.getHeight())), true);
        }
        return Bitmap.createScaledBitmap(bitmap, (int) ((((double) 1280) / ((double) bitmap.getHeight())) * ((double) bitmap.getWidth())), 1280, true);
    }

    @Override
    public void onBackPressed() {
        if (is_crop) {
            is_crop = false;
            sw_enhance.setChecked(false);
            iv_crop.setVisibility(View.VISIBLE);
            iv_rotate.setVisibility(View.VISIBLE);
            crop_imageView.setVisibility(View.VISIBLE);
            iv_generated_imageview.setVisibility(View.GONE);
            rl_generate_data.setVisibility(View.GONE);
            sw_enhance.setVisibility(View.GONE);
        } else {
            finish();
        }
    }
}