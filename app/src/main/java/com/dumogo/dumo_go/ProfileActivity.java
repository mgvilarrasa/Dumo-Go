package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.text.DecimalFormat;
import java.util.HashMap;
import model.User;
import utilities.DatePickerFragment;
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
    private TextView mMemberNum;
    private TextView mUserName;
    private TextView mCreateDate;
    private EditText mName;
    private EditText mLastName;
    private EditText mDni;
    private EditText mBirthDate;
    private EditText mAddress;
    private EditText mCountry;
    private EditText mPhone;
    private EditText mMail;
    private Button mChangeInfo;
    private Button mChangePass;
    //Resposta servidor
    private HashMap<String, String> responseServer;

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
        mMemberNum = (TextView) findViewById(R.id.tv_prof_num_member);
        mUserName = (TextView) findViewById(R.id.tv_prof_user_name);
        mCreateDate = (TextView) findViewById(R.id.tv_prof_create_date);
        mName = (EditText) findViewById(R.id.et_prof_name);
        mLastName = (EditText) findViewById(R.id.et_prof_lastName);
        mDni = (EditText) findViewById(R.id.et_prof_dni);
        mBirthDate = (EditText) findViewById(R.id.et_prof_birthDate);
        mAddress = (EditText) findViewById(R.id.et_prof_adress);
        mCountry = (EditText) findViewById(R.id.et_prof_country);
        mPhone = (EditText) findViewById(R.id.et_prof_phone);
        mMail = (EditText) findViewById(R.id.et_prof_mail);
        mChangeInfo = (Button) findViewById(R.id.bt_prof_changeProf);
        mChangePass = (Button) findViewById(R.id.bt_prof_changePass);
        //Listener boto canviar informacio
        mChangeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //Listener boto canviar contrassenya
        mChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassDialog();
            }
        });
        //Listener per escollir data en calendari
        mBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        //Popula la data al editText
                        DecimalFormat mFormat = new DecimalFormat("00");
                        final String selectedDate = year + "-" + mFormat.format(Double.valueOf(month + 1)) + "-" + mFormat.format(Double.valueOf(day));
                        mBirthDate.setText(selectedDate);
                    }
                });

                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        //Listener boto canviar informacio
        mChangeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mName.getText().toString().trim().length()>0 && mLastName.getText().toString().trim().length()>0 && mDni.getText().toString().trim().length()>0
                        && mBirthDate.getText().toString().trim().length()>0 && mAddress.getText().toString().trim().length()>0 && mCountry.getText().toString().trim().length()>0
                        && mPhone.getText().toString().trim().length()>0 && mMail.getText().toString().trim().length()>0){
                    updateInfoDialog();
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Falten dades!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        GetInfoTask getInfoTask = new GetInfoTask();
        getInfoTask.execute(infoHash());
    }

    /**
     * Dialog to change password
     */
    private void changePassDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Canvia contrassenya");
        builder.setMessage("Introduir la nova contrassenya");
        final EditText newPass = new EditText(context);
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(newPass);
        builder.setPositiveButton("Canviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(newPass.getText().toString().trim().length()>0){
                    ChangePassTask passTask = new ChangePassTask();
                    passTask.execute(changePassHash(newPass.getText().toString()));
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Introduir nova contrassenya!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Sortir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * Method to call the task to load user's info
     */
    private void loadInfo(){
        if(responseServer.get("codi_retorn").equals(String.valueOf(6000))){
            User user = Utils.hashToAdmin(responseServer);
            mMemberNum.setText("Numero de soci: " + user.getMemberNum());
            mUserName.setText("Nom usuari: " + user.getUserName());
            mCreateDate.setText("");
            mName.setText(user.getName());
            mLastName.setText(user.getLastName());
            mDni.setText(user.getId());
            mBirthDate.setText(user.getBirthDate());
            mAddress.setText(user.getAddress());
            mCountry.setText(user.getCountry());
            mPhone.setText(user.getPhoneNum());
            mMail.setText(user.getMail());
        }
        if(responseServer.get("codi_retorn").equals(String.valueOf(5000))){
            User user = Utils.hashToUser(responseServer);
            mMemberNum.setText("Numero de soci: " + user.getMemberNum());
            mUserName.setText("Nom usuari: " + user.getUserName());
            mCreateDate.setText("Data d'alta: " + user.getCreateDate());
            mName.setText(user.getName());
            mLastName.setText(user.getLastName());
            mDni.setText(user.getId());
            mBirthDate.setText(user.getBirthDate());
            mAddress.setText(user.getAddress());
            mCountry.setText(user.getCountry());
            mPhone.setText(user.getPhoneNum());
            mMail.setText(user.getMail());
        }
    }

    /**
     * Put the information in the HashMap to send to server
     * @return HashMap with the information required from server
     */
    private HashMap<String, String> infoHash(){
        HashMap<String, String> loginHash = new HashMap<String, String>();
        if(isAdmin){
            loginHash.put("accio", "mostra_admin");
            loginHash.put("nom_admin", nameUser);
        }
        else{
            loginHash.put("accio", "mostra_usuari");
            loginHash.put("user_name", nameUser);
        }
        loginHash.put("codi", String.valueOf(sessionCode));

        return loginHash;
    }
    /**
     * Put the information in the HashMap to send to server
     * @return HashMap with the information required from server
     */
    private HashMap<String, String> changePassHash(String newPass){
        HashMap<String, String> changePassHash = new HashMap<String, String>();
        changePassHash.put("accio", "canvia_password");
        changePassHash.put("codi", String.valueOf(sessionCode));
        changePassHash.put("password_nou", newPass);

        return changePassHash;
    }

    /**
     * Put the information in the HashMap to send to server
     * @return HashMap with the information to change on server
     */
    private HashMap<String, String> updateInfoHash(){
        HashMap<String, String> updateInfoHash = new HashMap<String, String>();
        if(isAdmin){
            updateInfoHash.put("accio", "modifica_admin");
            updateInfoHash.put("nou_nom_admin", nameUser);
            updateInfoHash.put("nom_admin", nameUser);
        }
        else{
            updateInfoHash.put("accio", "modifica_usuari");
            updateInfoHash.put("nou_user_name", nameUser);
            updateInfoHash.put("user_name", nameUser);
        }
        updateInfoHash.put("codi", String.valueOf(sessionCode));
        updateInfoHash.put("nom", mName.getText().toString());
        updateInfoHash.put("cognoms", mLastName.getText().toString());
        updateInfoHash.put("dni", mDni.getText().toString());
        updateInfoHash.put("data_naixement", mBirthDate.getText().toString());
        updateInfoHash.put("direccio", mAddress.getText().toString());
        updateInfoHash.put("pais", mCountry.getText().toString());
        updateInfoHash.put("telefon", mPhone.getText().toString());
        updateInfoHash.put("correu", mMail.getText().toString());

        return updateInfoHash;
    }

    /**
     * Change info user dialog
     */
    private void updateInfoDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modifica usuari");
        builder.setMessage("Guardar les noves dades?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UpdateUserTask updateUserTask = new UpdateUserTask();
                updateUserTask.execute(updateInfoHash());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
            try{
                if(response==10){
                    Toast.makeText(ProfileActivity.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(ProfileActivity.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }

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
                HashMap<String, String> received = (HashMap) input.readObject();
                input.close();
                output.close();
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
        protected void onPostExecute(HashMap<String, String> response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();
            try{
                if(response==null){
                    Toast.makeText(ProfileActivity.this, "Error consultant dades", Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(ProfileActivity.this, AdminMain.class);
                    startActivity(mainActivity);
                    finish();
                }
                else{
                    if(response.get("codi_retorn").equals("5000") || response.get("codi_retorn").equals("6000")){
                        responseServer = response;
                        loadInfo();
                    }
                    else if(response.get("codi_retorn").equals("5010") || response.get("codi_retorn").equals("6010")){
                        Toast.makeText(ProfileActivity.this, "Usuari no valid", Toast.LENGTH_SHORT).show();
                        Intent mainActivity = new Intent(ProfileActivity.this, AdminMain.class);
                        startActivity(mainActivity);
                        finish();
                    }
                    else if(response.get("codi_retorn").equals("0")){
                        Toast.makeText(ProfileActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                        Intent mainActivity = new Intent(ProfileActivity.this, AdminMain.class);
                        startActivity(mainActivity);
                        finish();
                    }
                    else{
                        Toast.makeText(ProfileActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                        Intent mainActivity = new Intent(ProfileActivity.this, AdminMain.class);
                        startActivity(mainActivity);
                        finish();
                    }
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                Intent mainActivity = new Intent(ProfileActivity.this, AdminMain.class);
                startActivity(mainActivity);
                finish();
            }
        }
    }

    /**
     * Execute task to add User
     */
    private class UpdateUserTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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

        @Override
        protected void onPostExecute(Integer response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();

            try{
                if(response==10){
                    Toast.makeText(ProfileActivity.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(ProfileActivity.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }
}