package com.example.imagenservidor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    Button btnBajarImagen;
    ImageView imgImagen;
    Button btnGuardarImagen;

    Context context;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private RequestQueue colaPeticiones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBajarImagen = findViewById(R.id.btnBajarImagen);
        imgImagen = findViewById(R.id.imgImagen);
        btnGuardarImagen = findViewById(R.id.btnGuardarImagen);

        context = getApplicationContext();
        colaPeticiones = Volley.newRequestQueue(this);


        btnBajarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bajar imagen del servidor
                bajarImagen();
            }
        });

        btnGuardarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pedir permiso para guardar la imagen
                pedirPermiso();
            }
        });
    }

    private void bajarImagen(){
        // Petición para obtener la imagen

        ImageRequest request = new ImageRequest(
                "https://apcpruebas.es/toni/cocina.jpg",
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imgImagen.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Adapter", "Error en respuesta Bitmap: "+ error.getMessage());
                    }
                });

        // Añadir petición a la cola
        colaPeticiones.add(request);
    }


    private void guardarImagen(){
        //guarda la imagen en la galeria
        Bitmap bmp = ((BitmapDrawable)imgImagen.getDrawable()).getBitmap();
        String filename = "cocina";
        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File file = new File(storageLoc, filename + ".jpg");

        try{
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            scanFile(context, Uri.fromFile(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void scanFile(Context context, Uri imageUri){
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
        Toast.makeText(context, "Imagen guardada", Toast.LENGTH_LONG).show();
    }

    //métodos para solicitar los permisos para guardar la imagen en la galeria


    private void pedirPermiso(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    guardarImagen();
                } else {
                    requestPermission(); // Code for permission
                }
            }
        }}

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "La aplicación necesita permiso para guardar la imagen en la galeria.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                guardarImagen();
            } else {
                Toast.makeText(context, "La imagen no se ha guardado", Toast.LENGTH_LONG).show();
            }
            break;
        }
    }

}
