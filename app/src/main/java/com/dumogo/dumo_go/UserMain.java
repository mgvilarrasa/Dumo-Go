package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class UserMain extends AppCompatActivity {
    //Variables passades en Intenten
    private static String nameUser;
    private static int sessionCode;
    private TextView mNameUser;
    private ImageButton mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        //Agafa les dades passades al login
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
        }
        mNameUser = (TextView) findViewById(R.id.tv_welcome_user);
        //Mostra la benvinguda
        mNameUser.setText("Benvingut " + nameUser);
        mProfile = (ImageButton) findViewById(R.id.ibt_profile_user);
        //Botó perfil usuari
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle d'informacio per enviar a la següent activity
                Bundle extras = new Bundle();
                extras.putString("NOM", String.valueOf(nameUser));
                extras.putInt("CODI_SESSIO", sessionCode);
                extras.putBoolean("IS_ADMIN", false);
                //Intent per anar a la pantalla del perfil d'usuari
                Intent intentProfile = new Intent(UserMain.this, ProfileActivity.class);
                //S'afegeixen els extras
                intentProfile.putExtras(extras);
                startActivity(intentProfile);
            }
        });
    }


}