package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import utilities.Utils;

public class AdminMain extends AppCompatActivity {
    //Variables passades en Intenten
    private static String nameUser;
    private static int sessionCode;
    private TextView mNameUser;
    private ImageButton mProfile;
    private ImageButton mUserManagement;

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
}