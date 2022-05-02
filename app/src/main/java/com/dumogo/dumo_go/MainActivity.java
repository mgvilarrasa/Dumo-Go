package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
public class MainActivity extends AppCompatActivity {

    //Variables view
    private EditText mUser, mPass;
    private Button mLogin;
    private CheckBox mIsAdmin;
    private static int loginResponse;
    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static Socket socket;
    private static InetSocketAddress serverAddr;
    //Context
    private final Context context = this;
    //Control sortir
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Oculta Titol Aplicació -- Més net
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        //Classe per les crides al server
        ServerCalls serverCalls = new ServerCalls(MainActivity.this);

        mUser = (EditText) findViewById(R.id.et_user);
        mPass = (EditText) findViewById(R.id.et_pass);
        mLogin= (Button) findViewById(R.id.bt_enter);
        mIsAdmin = (CheckBox) findViewById(R.id.cb_Admin);

        //Listener botó login
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Tots els campls plens
                if(mUser.getText().toString().trim().length()>0 && mPass.getText().toString().trim().length()>0){
                    //Fa la petició al server
                    int response = serverCalls.hastToInt(loginHash());
                    actionLogin(response);
                }
                else if(mUser.getText().toString().trim().length()>0 && mPass.getText().toString().trim().length()==0){
                    Toast.makeText(MainActivity.this, "Introduir Contrassenya!", Toast.LENGTH_LONG).show();
                }
                else if(mUser.getText().toString().trim().length()==0 && mPass.getText().toString().trim().length()>0){
                    Toast.makeText(MainActivity.this, "Introduir Usuari!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Introduir dades d'acces", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Depending on type of user, starts UserMain or AdminMain activity
     */
    private void goActivityUser(){

        //Bundle d'informacio per enviar a la següent activity
        Bundle extras = new Bundle();
        extras.putString("NOM", String.valueOf(mUser.getText()));
        extras.putInt("CODI_SESSIO", getCodeFromServer());

        if(mIsAdmin.isChecked()){
            //Intent per anar a la pantalla d'inici del Admin
            Intent intentMainAdmin = new Intent(MainActivity.this, AdminMain.class);
            //S'afegeixen els extras
            intentMainAdmin.putExtras(extras);
            startActivity(intentMainAdmin);
        }
        else{
            //Intent per anar a la pantalla d'inici del User
            Intent intentMainUser = new Intent(MainActivity.this, UserMain.class);
            //S'afegeixen els extras
            intentMainUser.putExtras(extras);
            startActivity(intentMainUser);
        }
    }

    /**
     * Creates HashMap to send to Server for login
     * @return HasMap<String, String> with login credentials + action
     */
    private HashMap<String, String> loginHash(){
        HashMap<String, String> loginHash = new HashMap<String, String>();
        if(mIsAdmin.isChecked()){
            loginHash.put("accio", "comprobar_admin");
            loginHash.put("nom_admin", String.valueOf(mUser.getText()));
        }
        else{
            loginHash.put("accio", "comprobar_usuari");
            loginHash.put("user_name", String.valueOf(mUser.getText()));
        }

        loginHash.put("password", String.valueOf(mPass.getText()));

        return loginHash;
    }

    private void actionLogin(int response){
        if(response==8010 || response==7010) {
            Toast.makeText(MainActivity.this, "Usuari Erroni!", Toast.LENGTH_SHORT).show();
            mUser.requestFocus();
        }

        else if(response==8020 || response==7020){
            Toast.makeText(MainActivity.this, "Contrassenya Erronia!", Toast.LENGTH_SHORT).show();
            mPass.requestFocus();
        }

        else if(response==8030 || response==7030){
            Toast.makeText(MainActivity.this, "Error conectant Base de Dades!", Toast.LENGTH_SHORT).show();
            mPass.requestFocus();
        }

        else if(response==10){
            Toast.makeText(MainActivity.this, "Sessió finalitzada!", Toast.LENGTH_SHORT).show();
        }
        else if(response==1){
            Toast.makeText(MainActivity.this, "Error conectant al servidor", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Entrant...", Toast.LENGTH_SHORT).show();
            codeFromServer(response);
            goActivityUser();
        }
    }

    //Takes code from asyncTask
    private void codeFromServer(int response){
        loginResponse = response;
    }
    //Get loginCode
    private int getCodeFromServer(){
        return loginResponse;
    }
    /**
     * BackPressed. Twice to exit app. first time, it will show a Toast
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