package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Variables view
    private EditText mUser, mPass;
    private Button mLogin;
    //Codi conexio
    private String loginCode;
    //Dades conexio
    private static final String ADDRESS = "192.168.43.30";
    private static final int SERVERPORT = 7777;
    private static Socket socket;
    //Context
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUser = (EditText) findViewById(R.id.et_user);
        mPass = (EditText) findViewById(R.id.et_pass);
        mLogin= (Button) findViewById(R.id.bt_enter);

        //Listener botó login
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUser.getText().toString().trim().length()>0 && mPass.getText().toString().trim().length()>0){
                    //new LoginServerTask().execute(loginHash());
                    ClientTask client = new ClientTask();
                    client.execute(loginHash());
                    //Toast.makeText(MainActivity.this, getCodeFromServer(), Toast.LENGTH_LONG).show();
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
     * Creates HashMap to send to Server
     * @return HasMap<String, String>
     */
    private HashMap<String, String> loginHash(){
        HashMap<String, String> loginHash = new HashMap<String, String>();
        loginHash.put("accio", "login");
        loginHash.put("usuari", String.valueOf(mUser.getText()));
        loginHash.put("pass", String.valueOf(mPass.getText()));

        return loginHash;
    }

    private class ClientTask extends AsyncTask<HashMap<String, String>, Void, String>{
        //Diàleg de càrrega
        ProgressDialog progressDialog;

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
        protected String doInBackground(HashMap<String, String>... values){

            try {
                //Se conecta al servidor
                InetAddress serverAddr = InetAddress.getByName(ADDRESS);
                Log.i("I/TCP Client", "Adress " + serverAddr);
                Log.i("I/TCP Client", "Connecting...");
                socket = new Socket(ADDRESS, SERVERPORT);
                Log.i("I/TCP Client", "Connected to server");

                //envia peticion de cliente
                Log.i("I/TCP Client", "Send data to server");
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                HashMap<String, String> request = values[0];
                output.writeObject(request);

                //recibe respuesta del servidor y formatea a String
                Log.i("I/TCP Client", "Received data to server");
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String received = (String) input.readObject();
                Log.i("I/TCP Client", "Received " + received);
                Log.i("I/TCP Client", "");
                //cierra conexion
                socket.close();
                return received;
            }catch (UnknownHostException ex) {
                Log.e("E/TCP Client", ex.getMessage());
                return ex.getMessage();
            } catch (IOException ex) {
                Log.e("E/TCP Client", ex.getMessage());
                return ex.getMessage();
            } catch (ClassNotFoundException ex) {
                Log.e("E/TCP Client", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String value){
            codeFromServer(value);
            progressDialog.dismiss();
            if(value.equals("0001")){
                Toast.makeText(MainActivity.this, "Usuari Erroni!", Toast.LENGTH_LONG).show();
            }
            else if(value.equals("0002")){
                Toast.makeText(MainActivity.this, "Contrassenya Erronia!", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Entrant...", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Takes code from asyncTask
    private void codeFromServer(String code){
        loginCode = code;
    }
    //Get loginCode
    private String getCodeFromServer(){
        return loginCode;
    }


}