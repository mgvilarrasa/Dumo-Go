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

import utilities.Utils;

public class MainActivity extends AppCompatActivity {

    //Variables view
    private EditText mUser, mPass;
    private Button mLogin;
    private CheckBox mIsAdmin;
    //Codi conexio
    //TODO DELETE - private static HashMap<String, String> loginResponse;
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
                    //Executa connexió al client
                    LoginTask client = new LoginTask();
                    client.execute(loginHash());
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
            loginHash.put("nom_user", String.valueOf(mUser.getText()));
        }

        loginHash.put("password", String.valueOf(mPass.getText()));

        return loginHash;
    }

    /**
     * Login Task to connect to Server check for login
     * Sent onBackground some HashMap regarding login credentials
     * Server sends back OK code or Fail Code
     */
    private class LoginTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
        //Diàleg de càrrega
        ProgressDialog progressDialog;
        //Mostra barra de progrés
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle("Conectant al servidor");
            progressDialog.setMessage("Esperi...");
            progressDialog.show();
        }
        //Conecta Server i envia dades login. Rep codi de connexio o KO
        @Override
        protected Integer doInBackground(HashMap<String, String>... values){
            try {
                //Se conecta al servidor
                serverAddr = new InetSocketAddress(ADDRESS, SERVERPORT);
                Log.i("I/TCP Client", "Connecting...");
                socket = new Socket();
                socket.connect(serverAddr, 5000);
                Log.i("I/TCP Client", "Connected to server");
                //envia peticion de cliente
                Log.i("I/TCP Client", "Send data to server");
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                HashMap<String, String> request = values[0];
                output.writeObject(request);
                //recibe respuesta del servidor y formatea a String
                Log.i("I/TCP Client", "Getting data from server");
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                //Obte HashMap
                int received = (Integer) input.readObject();
                input.close();
                output.close();
                //Log
                Log.i("I/TCP Client", "Received");
                Log.i("I/TCP Client", "Code " + received);
                //cierra conexion
                socket.close();
                return received;
            }catch (UnknownHostException ex) {
                Log.e("E/TCP  UKN", ex.getMessage());
                return 0;
            } catch (SocketTimeoutException ex){
                Log.e("E/TCP Client TimeOut", ex.getMessage());
                return 1;
            } catch (IOException ex) {
                Log.e("E/TCP Client IO", ex.getMessage());
                return 0;
            } catch (ClassNotFoundException ex) {
                Log.e("E/TCP Client CNF", ex.getMessage());
                return 0;
            }
        }
        //Si login OK, enmagatxema la dada
        //Si dada OK, mostra Toast
        @Override
        protected void onPostExecute(Integer value){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();
            try{
                if(value==8010 || value==7010) {
                    Toast.makeText(MainActivity.this, "Usuari Erroni!", Toast.LENGTH_LONG).show();
                    mUser.requestFocus();
                }

                else if(value==8020 || value==7020){
                    Toast.makeText(MainActivity.this, "Contrassenya Erronia!", Toast.LENGTH_LONG).show();
                    mPass.requestFocus();
                }

                else if(value==8030 || value==7030){
                    Toast.makeText(MainActivity.this, "Error conectant Base de Dades!", Toast.LENGTH_LONG).show();
                    mPass.requestFocus();
                }

                else if(value==10){
                    Toast.makeText(MainActivity.this, "Sessió finalitzada!", Toast.LENGTH_LONG).show();
                }
                else if(value==1){
                    Toast.makeText(MainActivity.this, "Error conectant al servidor", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Entrant...", Toast.LENGTH_LONG).show();
                    codeFromServer(value);
                    goActivityUser();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
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
    //Control sortir de l'aplicacio
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Click enrere altre cop per sortir", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
        return;
    }
}