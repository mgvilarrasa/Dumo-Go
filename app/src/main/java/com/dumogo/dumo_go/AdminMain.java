package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import utilities.ServerCalls;
import utilities.Utils;

/**
 * author Marçal González
 */
public class AdminMain extends AppCompatActivity {
    //Variables passades en Intenten
    private static String nameUser;
    private static int sessionCode;
    //Variables View
    private TextView mNameUser;
    private ImageButton mProfile;
    private ImageButton mUserManagement;
    //Context
    private Context context = this;
    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static Socket socket;
    private static InetSocketAddress serverAddr;
    //Control sortir
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        //Agafa les dades passades al login
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
        }
        mNameUser = (TextView) findViewById(R.id.tv_welcome_admin);
        //Mostra la benvinguda
        mNameUser.setText("Benvingut " + nameUser);
        mProfile = (ImageButton) findViewById(R.id.ibt_profile_admin);
        //Botó perfil usuari
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle d'informacio per enviar a la següent activity
                Bundle extrasPf = new Bundle();
                extrasPf.putString("NOM", String.valueOf(nameUser));
                extrasPf.putInt("CODI_SESSIO", sessionCode);
                extrasPf.putBoolean("IS_ADMIN", true);
                //Intent per anar a la pantalla del perfil d'usuari
                Intent intentProfile = new Intent(AdminMain.this, ProfileActivity.class);
                //S'afegeixen els extras
                intentProfile.putExtras(extrasPf);
                startActivity(intentProfile);
            }
        });
        //Boto administracio usuaris
        mUserManagement =(ImageButton) findViewById(R.id.ibt_users);
        mUserManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle d'informacio per enviar a la següent activity
                Bundle extrasUm = new Bundle();
                extrasUm.putString("NOM", String.valueOf(nameUser));
                extrasUm.putInt("CODI_SESSIO", sessionCode);
                //Intent per anar a la pantalla del perfil d'usuari
                Intent intentProfile = new Intent(AdminMain.this, UserManagement.class);
                //S'afegeixen els extras
                intentProfile.putExtras(extrasUm);
                startActivity(intentProfile);
            }
        });
    }

    //Menu superior per tenir opcio de logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.admin_main_menu, menu);
        return true;
    }
    //Opcio fer logout
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.menu_settings){
            //Crea hash de logout
            HashMap<String, String> hashLogout = new HashMap<>();
            hashLogout.put("codi", String.valueOf(sessionCode));
            hashLogout.put("accio", "tancar_sessio");
            //Crida al server
            ServerCalls serverCalls = new ServerCalls(AdminMain.this);
            int response = serverCalls.hastToInt(hashLogout);
            exitProgram(response);
        }
        else{
            return super.onContextItemSelected(item);
        }
        return true;
    }

    /**
     * Exit program using logout
     * @param response
     */
    private void exitProgram(int response){
        if(response==20){
            Toast.makeText(AdminMain.this, "Sortint...", Toast.LENGTH_SHORT).show();
            Intent mainActivity = new Intent(AdminMain.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        }else if(response==1){
            Toast.makeText(AdminMain.this, "Error conectant amb el servidor!", Toast.LENGTH_SHORT).show();
            Intent mainActivity = new Intent(AdminMain.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        }else{
            Intent mainActivity = new Intent(AdminMain.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        }
    }

    /**
     * BackPressed. Twice to logout. first time, it will show a Toast
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Click enrere altre cop per sortir", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}