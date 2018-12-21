package indramayu.net.sispolin;


import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import indramayu.net.sispolin.R;

public class MainSplash extends Activity {
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window  window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    Thread splashThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        StartAnimations();
    }
    private void StartAnimations() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        animation.reset();
        View layout = findViewById(R.id.linSplash);
        layout.clearAnimation();
        layout.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this, R.anim.translate);
        animation.reset();
        View imageView = findViewById(R.id.iconSplash);
        imageView.clearAnimation();
        imageView.startAnimation(animation);

        splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;

                    while (waited < 3500) {
                        sleep(100);
                        waited += 100;
                    }

                    Intent intent = new Intent(MainSplash.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    MainSplash.this.finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(MainSplash.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        splashThread.start();
    }
}
