package com.mine.miniflash;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.muddzdev.styleabletoast.StyleableToast;

import static android.os.SystemClock.sleep;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;

    RelativeLayout relativeLayout;
    Switch aSwitch;
    TextView textOnOff, textSwapFlash, AboutTextView;
    CameraManager cameraManager;
    String camera_id, on_of_text;
    ImageButton imageButton;
    Button SOS_Button;

    Dialog dialog;

    int id = 0;
    long previousTime = 0;

    boolean temp = true;
    boolean lamp = true;
    boolean sosBtnCheck = true;
    boolean ButtonSound = false;
    boolean SOS_Stage = false;
    boolean SOS_Check = false;


    //back button double press method//
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (2000 + previousTime > (previousTime = System.currentTimeMillis()))
        {
            super.onBackPressed();
        } else {
            Toast toast= Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,800);
            toast.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().show();

        //Status bar hide method//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        lottieAnimationView = findViewById(R.id.sosAnimation);

        SOS_Button = findViewById(R.id.SOS_Button);
        relativeLayout = findViewById(R.id.mainPageLayout);
        aSwitch = findViewById(R.id.Btn_On_Off);
        textOnOff = findViewById(R.id.textOnOff);
        textSwapFlash = findViewById(R.id.textSwapFlash);
        imageButton = findViewById(R.id.SwapFlash);

        dialog = new Dialog(this);

        MediaPlayer SOSSOUND = MediaPlayer.create(this, R.raw.sossound);
        SOS_Button.setOnClickListener(v -> {
            if(ButtonSound) SOSSOUND.start();
            if(SOS_Check){
                SOS_Check = false;
            }else {
                SOS_Check = true;
            }
            sosONOFF();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sosOnOff();
            }
        });

        MediaPlayer FLIPSOUND = MediaPlayer.create(this, R.raw.flip_button_sound);
        imageButton.setOnClickListener(v -> {
            if(ButtonSound) FLIPSOUND.start();
            swapFlash();
        });

        MediaPlayer ONOFFSOUND = MediaPlayer.create(this, R.raw.switch_button_sound);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(ButtonSound) ONOFFSOUND.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flashLight(isChecked);
            }
        });
    }

    //SOS Controller//
    private void sosONOFF() {

        if(sosBtnCheck){
            relativeLayout.setBackgroundResource(R.color.white);

            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();

            SOS_Button.setTextColor(Color.parseColor("#006400"));
            imageButton.setVisibility(View.INVISIBLE);
            textSwapFlash.setVisibility(View.INVISIBLE);
            aSwitch.setVisibility(View.INVISIBLE);
            textOnOff.setVisibility(View.INVISIBLE);

            getSupportActionBar().hide();

            sosBtnCheck = false;
        }else {
            relativeLayout.setBackgroundResource(R.drawable.backgoround);

            lottieAnimationView.setVisibility(View.INVISIBLE);
            lottieAnimationView.pauseAnimation();

            SOS_Button.setTextColor(Color.parseColor("#FF0000"));
            imageButton.setVisibility(View.VISIBLE);
            textSwapFlash.setVisibility(View.VISIBLE);
            aSwitch.setVisibility(View.VISIBLE);
            textOnOff.setVisibility(View.VISIBLE);

            getSupportActionBar().show();

            sosBtnCheck = true;
        }

    }

    //flash swap method//
    private void swapFlash() {
        if(temp){
            imageButton.setImageResource(R.drawable.ic_front_image);
            textSwapFlash.setText("Front Flash");
            Toast toast = Toast.makeText(this, "Swap Back To Front Flash Light", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,-400);
            toast.show();
            temp = false;
            id = 1;
        }
        else {
            imageButton.setImageResource(R.drawable.ic_back_image);
            textSwapFlash.setText("Back Flash");
            Toast toast = Toast.makeText(this, "Swap Front To Back Flash Light", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,-400);
            toast.show();
            temp = true;
            id = 0;
        }
    }

    //Flash ON OFF method//
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void flashLight(boolean isChecked) {
        try {
            cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

            if(cameraManager.getCameraCharacteristics(String.valueOf(id)).get(CameraCharacteristics.FLASH_INFO_AVAILABLE))
            {
                //Camera id Check And Flash On/Off Method
                camera_id = cameraManager.getCameraIdList()[id];
                cameraManager.setTorchMode(camera_id, isChecked);
                on_of_text = isChecked? "ON" : "OFF";
                textOnOff.setText(on_of_text);
            }
            else {
                if(lamp){
                    //Front Flash Not Detected Toast Message
                    StyleableToast styleableToast = StyleableToast.makeText(this,"Front Flash Light Not Found!!", Toast.LENGTH_SHORT, R.style.toaststyle);
                    styleableToast.setGravity(Gravity.BOTTOM);
                    styleableToast.show();

                    //Screen Brightness High
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.screenBrightness = 1.0f;
                    getWindow().setAttributes(lp);
                    lamp = false;

                    //Hide Main Screen Others Component
                    relativeLayout.setBackgroundResource(R.color.white);
                    imageButton.setVisibility(View.INVISIBLE);
                    SOS_Button.setVisibility(View.INVISIBLE);
                    textSwapFlash.setVisibility(View.INVISIBLE);

                    //Hide Top Navigation bar
                    Objects.requireNonNull(getSupportActionBar()).hide();
                }
                else {
                    //Screen Brightness Low
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.screenBrightness = 0.2f;
                    getWindow().setAttributes(lp);
                    lamp = true;

                    //Show Main Screen Others Component
                    relativeLayout.setBackgroundResource(R.drawable.background);
                    imageButton.setVisibility(View.VISIBLE);
                    textSwapFlash.setVisibility(View.VISIBLE);
                    if(SOS_Stage){
                        SOS_Button.setVisibility(View.VISIBLE);
                    }

                    //Show Top Navigation Bar
                    Objects.requireNonNull(getSupportActionBar()).show();
                }
            }

        }
        catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    //menu item show method//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Show Menu Options in The Top Navigation Bar
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return true;
    }


    //menu items id selected method//
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Menu Items Selected Action
        switch(item.getItemId())
        {
            case R.id.RateUs:
                return true;
            case R.id.MoreApp:
                return true;
            case R.id.About:
                show_About_Dialog();
                return true;
            case R.id.Setting:
                Show_Setting_Page();
                return true;
            case R.id.Exit:
//                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Setting page call method//
    @SuppressLint("ResourceType")
    private void Show_Setting_Page() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    //dialog show method//
    private void show_About_Dialog() {
        //Custom Dialog Show And Other Options
        dialog.setContentView(R.layout.about_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView About_Dialog_Image_View = dialog.findViewById(R.id.AboutImageView);
        TextView AboutTextView = dialog.findViewById(R.id.AbouttextView);
        Button Ok_Button = dialog.findViewById(R.id.OkButton);

        Toast toast = Toast.makeText(this, "Dialog Closed", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,-400);

        Ok_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                toast.show();
            }
        });
        dialog.show();
    }

    //preference page activity check method//
    private void Load_Setting() {
        //Preference variable assign//
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        //SOS ON OFF Method//
        boolean check_SOS = sp.getBoolean("S_SOS", false);
        if(check_SOS){
            SOS_Button.setVisibility(View.VISIBLE);
            SOS_Stage = true;
        }else {
            SOS_Button.setVisibility(View.INVISIBLE);
            SOS_Stage = false;
        }

        //Button Sound Method//
        ButtonSound = sp.getBoolean("S_BUTTON_SOUND", false);
    }

    //SoS function//

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sosOnOff(){
        SOSThread thread = new SOSThread(10);
        thread.start();
    }

    //Thread Class Assign//
    @RequiresApi(api = Build.VERSION_CODES.M)
    class SOSThread extends Thread{
        int seconds;
        SOSThread(int seconds){
            this.seconds = seconds;
        }

        @Override
        public void run() {
            cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            try {
                camera_id = cameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            while (SOS_Check) {
                try {
                    cameraManager.setTorchMode(camera_id, true);
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                try {
                    cameraManager.setTorchMode(camera_id, false);
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //
    @Override
    protected void onResume() {
        Load_Setting();
        super.onResume();
    }
}