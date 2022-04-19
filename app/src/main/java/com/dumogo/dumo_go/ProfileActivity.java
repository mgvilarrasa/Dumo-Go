package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.util.HashMap;

import utilities.Utils;

/**
 * author Marçal González
 */
public class ProfileActivity extends AppCompatActivity {
    //Variables d'us
    private static String nameUser;
    private static int sessionCode;
    private static boolean isAdmin;
    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static Socket socket;
    private static InetSocketAddress serverAddr;
    //Context
    private final Context context = this;
    //Widgets pantalla
    private TextView mUserName;
    private TextView mName;
    private TextView mMail;
    private TextView mDate;
    private TextView mChangePass;
    private TextView mChangeInfo;
    //Diàlegs
    private Dialog changePassDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Agafa les dades passades a l'activity anterior
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
            isAdmin = extras.getBoolean("IS_ADMIN");
        }
        //inicialitza widgets de pantalla
        mUserName = (TextView) findViewById(R.id.tv_user_name);
        mName = (TextView) findViewById(R.id.tv_name_profile);
        mMail = (TextView) findViewById(R.id.tv_mail_adress);
        mDate = (TextView) findViewById(R.id.tv_date);
        mChangeInfo = (TextView) findViewById(R.id.tv_change_info);
        //Boto canviar dades
        mChangeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Create info dialog
            }
        });
        mChangePass = (TextView) findViewById(R.id.tv_change_pass);
        //Boto canviar pass
        mChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassDialog();
            }
        });
        //Carrega informació de la pàgina
        loadInfo();
    }

    /**
     * Dialog to change password
     */
    private void changePassDialog(){
        //Crea el diàleg de canvi de contrassenya
        changePassDialog = new Dialog(context);
        changePassDialog.setContentView(R.layout.change_pass_dialog);
        //Camps de text
        EditText mOldPass = (EditText) changePassDialog.findViewById(R.id.et_old_pass);
        EditText mNewPass = (EditText) changePassDialog.findViewById(R.id.et_new_pass);
        //Botó tancar diàleg
        Button closeDialog = changePassDialog.findViewById(R.id.bt_exit_pass);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassDialog.dismiss();
            }
        });
        //Botó guardar
        Button savePass = changePassDialog.findViewById(R.id.bt_save_pass);
        savePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOldPass.getText().toString().trim().length()>0 && mNewPass.getText().toString().trim().length()>0){
                    ChangePassTask passTask = new ChangePassTask();
                    passTask.execute(changePassHash());
                }
                else if(mOldPass.getText().toString().trim().length()>0 && mNewPass.getText().toString().trim().length()==0){
                    Toast.makeText(ProfileActivity.this, "Introduir nova contrassenta!", Toast.LENGTH_LONG).show();
                }
                else if(mOldPass.getText().toString().trim().length()==0 && mNewPass.getText().toString().trim().length()>0){
                    Toast.makeText(ProfileActivity.this, "Introduir contrassenya antiga!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Introduir dades!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Method to call the task to load user's info
     */
    private void loadInfo(){
        //Executa connexió al server
        GetInfoTask client = new GetInfoTask();
        client.execute(infoHash());
    }

    /**
     * Put the information in the HashMap to send to server
     * @return HashMap with the information required from server
     */
    private HashMap<String, String> infoHash(){
        HashMap<String, String> loginHash = new HashMap<String, String>();
        //TODO determinar acció
        loginHash.put("accio", "obte_dades");
        loginHash.put("codi", String.valueOf(sessionCode));
        loginHash.put("nom", nameUser);

        return loginHash;
    }
    /**
     * Put the information in the HashMap to send to server
     * @return HashMap with the information required from server
     */
    private HashMap<String, String> changePassHash(){
        HashMap<String, String> changePassHash = new HashMap<String, String>();
        //TODO determinar acció
        changePassHash.put("accio", "canvia_password");
        changePassHash.put("codi", String.valueOf(sessionCode));
        changePassHash.put("nom", nameUser);

        return changePassHash;
    }

    /**
     * Task to get profile information from server
     * Sent onBackground some HashMap regarding login credentials
     * Server sends back HashMap with user's information
     */
    private class GetInfoTask extends AsyncTask<HashMap<String, String>, Void, HashMap<String, String>>{
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

        @Override
        protected HashMap<String, String> doInBackground(HashMap<String, String>... values){
            try {
                //Se conecta al servidor
                serverAddr = new InetSocketAddress(ADDRESS, SERVERPORT);
                Log.i("I/TCP Client", "Connecting...");
                //socket = new Socket(ADDRESS, SERVERPORT);
                socket.connect(serverAddr, 1000);
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
                HashMap<String, String> received = (HashMap) input.readObject();
                input.close();
                output.close();
                //Log
                Log.i("I/TCP Client", "Received " + received.get("login"));
                Log.i("I/TCP Client", "Code " + received.get("codi"));
                //cierra conexion
                socket.close();
                return received;
            } catch (UnknownHostException ex) {
                Log.e("E/TCP  UKN", ex.getMessage());
                return null;
            } catch (SocketTimeoutException ex){
                Log.e("E/TCP Client TIMEOUT", ex.getMessage());
                return null;
            } catch (IOException ex) {
                Log.e("E/TCP Client IO", ex.getMessage());
                return null;
            } catch (ClassNotFoundException ex) {
                Log.e("E/TCP Client CNF", ex.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, String> value){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();
            //TODO change conditions for getting codes from server
            try{
                if(value.get("login").equals("ko")){
                    if(value.get("codi").equals("0001")){
                        Toast.makeText(ProfileActivity.this, "Usuari Erroni!", Toast.LENGTH_LONG).show();
                    }
                    else if(value.get("codi").equals("0002")){
                        Toast.makeText(ProfileActivity.this, "Contrassenya Erronia!", Toast.LENGTH_LONG).show();
                    }
                }
                else if(value.get("login").equals("ok")){
                    if(isAdmin){
                        /*TODO when server ready, get data from HashMap to put on TextViews
                        mUserName.setText(response.getString(1));
                        mName.setText(response.getString(3));
                        mMail.setText(response.getString(5));
                        mDate.setText("");
                        */
                    }
                    else{
                        /*TODO when server ready, get data from HashMap to put on TextViews
                        mUserName.setText(response.getString(1));
                        mName.setText(response.getString(7));
                        mMail.setText(response.getString(5));
                        mDate.setText(response.getString(4));
                        */
                    }
                }else if(value.get("codi").equals(20)){
                    Intent mainActivity = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Error!", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }

    /**
     * Execute task to change User's password
     */
    private class ChangePassTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
                //Obte ResultSet
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
                return null;
            } catch (SocketTimeoutException ex){
                Log.e("E/TCP Client TimeOut", ex.getMessage());
                return 1;
            } catch (IOException ex) {
                Log.e("E/TCP Client IO", ex.getMessage());
                return null;
            } catch (ClassNotFoundException ex) {
                Log.e("E/TCP Client CNF", ex.getMessage());
                return null;
            }
        }
        //Recorre el ResultSet i obté les dades
        @Override
        protected void onPostExecute(Integer response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();
            //TODO Falta codi de confirmació
            try{
                if(response==1234) {
                    Toast.makeText(ProfileActivity.this, "Contrassenya canviada!", Toast.LENGTH_LONG).show();
                    changePassDialog.dismiss();
                }
                else if(response==1){
                    Toast.makeText(ProfileActivity.this, "Error conectant al server!", Toast.LENGTH_LONG).show();
                    changePassDialog.dismiss();
                }
                else if(response==10){
                    Toast.makeText(ProfileActivity.this, "Sessió finalitzada!", Toast.LENGTH_LONG).show();
                    changePassDialog.dismiss();
                    Intent mainActivity = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                }else{
                    Toast.makeText(ProfileActivity.this, "Error!", Toast.LENGTH_LONG).show();
                    changePassDialog.dismiss();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                changePassDialog.dismiss();
            }
        }
    }
}