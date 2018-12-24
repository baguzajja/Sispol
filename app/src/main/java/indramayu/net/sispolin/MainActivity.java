package indramayu.net.sispolin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import indramayu.net.sispolin.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import indramayu.net.sispolin.*;
import indramayu.net.sispolin.Config;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;



public class MainActivity extends AppCompatActivity {

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private static final String TAG = "MainActivity";
    String UA = "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString(UA);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setGeolocationEnabled(true);
        webView.setWebChromeClient(new GeoWebChromeClient());
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath(getFilesDir().getPath() );
        setListeners();
        LocationManager mManagerLoc = ( LocationManager ) this.getSystemService ( Context.LOCATION_SERVICE );
        boolean enabled = mManagerLoc.isProviderEnabled ( LocationManager.GPS_PROVIDER );
        if ( !enabled )
        {
            showDialogGPS ();
        }
        else if (
                ContextCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions ( this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 0 );
        }
        else {
            webView.loadUrl("https://m.polresindramayu.net");
        }


    }

    private void showDialogGPS ()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder ( this );
        builder.setCancelable ( false );
        builder.setTitle ( "Aktifkan GPS" );
        builder.setInverseBackgroundForced ( true );

        builder.setPositiveButton ( "Aktif", new DialogInterface.OnClickListener ()
        {
            public void onClick ( DialogInterface dialog, int which )
            {
                startActivity ( new Intent ( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS ) );
            }
        } );

        builder.setNegativeButton ( "Tidak", new DialogInterface.OnClickListener ()
        {
            public void onClick ( DialogInterface dialog, int which )
            {
                dialog.dismiss ();
            }
        } );

        AlertDialog alert = builder.create ();
        alert.show ();
    }

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            Log.i(TAG, "onGeolocationPermissionsShowPrompt()");

            final boolean remember = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Location");
            builder.setMessage("Would like to use your Current Location").setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.invoke(origin, true, remember);
                }
            }).setNegativeButton("Dont Allow ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.invoke(origin, false, remember);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void setListeners (){

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadUrl("about:blank");
                view.clearHistory();
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress){

            }
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                mUploadMessage =uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

            }
        });

        final MyJavaScriptInterface myJavaScriptInterface = new MyJavaScriptInterface(this);
        webView.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
    }


    public class MyJavaScriptInterface {
        Context mContext;

        MyJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            // webView.loadUrl("javascript:document.getElementById(\"Button3\").innerHTML = \"bye\";");
        }

        @JavascriptInterface
        public void openAndroidDialog() {
            AlertDialog.Builder myDialog
                    = new AlertDialog.Builder(MainActivity.this);
            myDialog.setTitle("DANGER!");
            myDialog.setMessage("You can do what you want!");
            myDialog.setPositiveButton("ON", null);
            myDialog.show();
        }

    }

    public void onRequestPermissionsResult ( int requestCode, String permissions[], int[] grantResults )
    {
        switch ( requestCode )
        {
            case 1:
            {

                if ( grantResults.length > 0 && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED )
                {

                }
                else
                {

                }

                return;
            }

        }
    }

    public boolean checkLocationPermission ()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission ( permission );

        return ( res == PackageManager.PERMISSION_GRANTED );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }


//    private File createImageFile() throws IOException {
//
//        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "img_" + timeStamp + "_";
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        return File.createTempFile(imageFileName, ".jpg", storageDir);
//    }


    //public void displayUrl() {
    //    String.valueOf("https://m.polresindramayu.net/");
    //}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            //back to previous url
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Method to close App
     */
    private void finishApp() {
        finish();
        moveTaskToBack(true);
    }


}
