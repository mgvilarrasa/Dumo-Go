package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
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

import utilities.DatePickerFragment;
import utilities.ServerCalls;
import utilities.Utils;

/**
 * author Marçal González
 */
public class UserManagement extends AppCompatActivity {
    //Variables usuari actiu
    private static String nameUser;
    private static int sessionCode;
    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static Socket socket;
    private static InetSocketAddress serverAddr;
    //Variables view
    private AutoCompleteTextView mUserAc;
    private Button mAddUser;
    private Button mDeleteUser;
    private RadioButton mUsersRb;
    private RadioButton mAdminsRb;
    private ListView mUserList;
    //Variables d'us
    private String[] listUsers;
    private String userSelected;
    private Context context = this;
    //Texts add user
    private String userName;
    private String nom;
    private String lastName;
    private String birthDate;
    private String address;
    private String country;
    private String dni;
    private String pass;
    private String date;
    private String email;
    //Diàlegs
    private Dialog addUserDialog;
    //Crides server
    private ServerCalls serverCalls;
    private int responseServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        //Agafa les dades passades a l'activity anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
        }
        //Crides al servidor
        serverCalls = new ServerCalls(UserManagement.this);
        //Inicialitza view variables
        mAddUser = (Button) findViewById(R.id.bt_add_user);
        mDeleteUser = (Button) findViewById(R.id.bt_delete_user);
        mUsersRb = (RadioButton) findViewById(R.id.rb_User);
        mAdminsRb = (RadioButton) findViewById(R.id.rb_Admin);
        mUsersRb.setChecked(true);
        //Omple l'array d'usuaris per la cerca
        listUsers = listUsers();
        //Adapter usuaris mostrats
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listUsers);
        mUserAc = (AutoCompleteTextView) findViewById(R.id.ac_users);
        //Afegeix els possibles cercadors
        mUserAc.setAdapter(adapter);
        mUserList = (ListView) findViewById(R.id.user_list);
        //Afegeix elements a la llista
        mUserList.setAdapter(adapter);
        //Obte l'usuari seleccionat
        mUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                userSelected = adapter.getItem(position);
            }
        });
        //Boto afegir usuari
        mAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserDialog();
            }
        });
        //Boto eliminar usuari
        mDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUserDialog();
            }
        });
    }

    //TODO delete when final list is ready
    private static final String[] USERS = new String[]{
            "Pep25", "Oscar39", "Marc45", "Carmelo", "Mgv11", "mgv"
    };

    //TODO when server is ready, complete function
    private String[] listUsers() {
        //TODO troba llista usuaris
        return USERS;
    }

    /**
     * New user dialog
     */
    private void addUserDialog() {
        //Crea el diàleg de canvi de contrassenya
        addUserDialog = new Dialog(context);
        addUserDialog.setContentView(R.layout.add_user_dialog);
        //Camps de text
        EditText mUserName = (EditText) addUserDialog.findViewById(R.id.et_au_userName);
        EditText mPass = (EditText) addUserDialog.findViewById(R.id.et_au_pass);
        EditText mNom = (EditText) addUserDialog.findViewById(R.id.et_au_nom);
        EditText mCognoms = (EditText) addUserDialog.findViewById(R.id.et_au_cognoms);
        EditText mDni = (EditText) addUserDialog.findViewById(R.id.et_au_dni);
        EditText mDate = (EditText) addUserDialog.findViewById(R.id.et_au_dataAlta);
        EditText mBirthDate = (EditText) addUserDialog.findViewById(R.id.et_au_data_naixement);
        EditText mAddress = (EditText) addUserDialog.findViewById(R.id.et_au_direccio);
        EditText mCountry = (EditText) addUserDialog.findViewById(R.id.et_au_pais);
        CheckBox mIsAdmin = (CheckBox) addUserDialog.findViewById(R.id.cb_au_isAdmin);
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        //Popula la data al editText
                        DecimalFormat mFormat = new DecimalFormat("00");
                        final String selectedDate = year + "-" + mFormat.format(Double.valueOf(month + 1)) + "-" + mFormat.format(Double.valueOf(day));
                        mDate.setText(selectedDate);
                    }
                });

                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
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
        EditText mMail = (EditText) addUserDialog.findViewById(R.id.et_au_email);
        addUserDialog.show();
        //Botó tancar diàleg
        Button closeDialog = addUserDialog.findViewById(R.id.bt_au_exit);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserDialog.dismiss();
            }
        });
        //Botó guardar
        Button addUser = addUserDialog.findViewById(R.id.bt_au_add);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserName.getText().toString().trim().length() > 0 && mPass.getText().toString().trim().length() > 0 && mNom.getText().toString().trim().length() > 0
                        && mDni.getText().toString().trim().length() > 0 && mDate.getText().toString().trim().length() > 0 && mMail.getText().toString().trim().length() > 0
                        && mCognoms.getText().toString().trim().length() > 0 && mAddress.getText().toString().trim().length() > 0 && mCountry.getText().toString().trim().length() > 0
                        && mBirthDate.getText().toString().trim().length() > 0) {
                    userName = mUserName.getText().toString();
                    nom = mNom.getText().toString();
                    dni = mDni.getText().toString();
                    pass = mPass.getText().toString();
                    date = mDate.getText().toString();
                    email = mMail.getText().toString();
                    country = mCountry.getText().toString();
                    lastName = mCognoms.getText().toString();
                    address = mAddress.getText().toString();
                    birthDate = mBirthDate.getText().toString();
                    //TODO Clean AddUserTask addUserTask = new AddUserTask();
                    if(mIsAdmin.isChecked()) {
                        responseServer = serverCalls.hastToInt(addUserHash(true));
                        addUserResponse(responseServer);
                        //TODO Clean addUserTask.execute(addUserHash(true));
                    }
                    else{
                        responseServer = serverCalls.hastToInt(addUserHash(false));
                        addUserResponse(responseServer);
                        //TODO Clean addUserTask.execute(addUserHash(false));
                    }
                } else {
                    Toast.makeText(UserManagement.this, "Introduir dades!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Delete user dialog
     */
    private void deleteUserDialog(){
        if(userSelected != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Eliminar usuari");
            builder.setMessage("Eliminar l'usuari " + userSelected + "?");
            builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO Clean DeleteUserTask deleteUserTask = new DeleteUserTask();
                    if(mAdminsRb.isChecked()){
                        responseServer = serverCalls.hastToInt(deleteUserHash(true));
                        deleteUserResponse(responseServer);
                        //TODO Clean deleteUserTask.execute(deleteUserHash(true));
                    }
                    else{
                        responseServer = serverCalls.hastToInt(deleteUserHash(false));
                        deleteUserResponse(responseServer);
                        //TODO Clean deleteUserTask.execute(deleteUserHash(false));
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
        else{
            Toast.makeText(UserManagement.this, "Usuari no seleccionat", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Manages response from server when adding user
     * @param response
     */
    private void addUserResponse(int response){
        if(response==1000 || response == 2000) {
            Toast.makeText(UserManagement.this, "Usuari afegit!", Toast.LENGTH_SHORT).show();
            addUserDialog.dismiss();
        }
        else if(response==1){
            Toast.makeText(UserManagement.this, "Error conectant al server!", Toast.LENGTH_SHORT).show();
            addUserDialog.dismiss();
        }
        else if(response==10){
            Toast.makeText(UserManagement.this, "Sessió finalitzada!", Toast.LENGTH_SHORT).show();
            addUserDialog.dismiss();
            Intent mainActivity = new Intent(UserManagement.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        }
        else if(response==1010 || response==2010){
            Toast.makeText(UserManagement.this, "Usuari no valid!", Toast.LENGTH_SHORT).show();
        }else if(response==1020 || response==2020){
            Toast.makeText(UserManagement.this, "Contrassenya no vàlida!", Toast.LENGTH_SHORT).show();
        }else if(response==1030 || response==2030){
            Toast.makeText(UserManagement.this, "Format DNI incorrecte!", Toast.LENGTH_SHORT).show();
        }else if(response==1031 || response==2031){
            Toast.makeText(UserManagement.this, "DNI repetit!", Toast.LENGTH_SHORT).show();
        }else if(response==1040 || response==2040){
            Toast.makeText(UserManagement.this, "Email incorrecte!", Toast.LENGTH_SHORT).show();
        }else if(response==1041 || response==2041){
            Toast.makeText(UserManagement.this, "Email ja existeix!", Toast.LENGTH_SHORT).show();
        }else if(response==0){
            Toast.makeText(UserManagement.this, "ERROR del servidor!", Toast.LENGTH_SHORT).show();
            addUserDialog.dismiss();
        }else{
            Toast.makeText(UserManagement.this, "Error!", Toast.LENGTH_SHORT).show();
            addUserDialog.dismiss();
        }
    }

    private void deleteUserResponse(int response){
        if(response==3000 || response == 4000) {
            Toast.makeText(UserManagement.this, "Usuari eliminat!", Toast.LENGTH_SHORT).show();
        }
        else if(response==1){
            Toast.makeText(UserManagement.this, "Error conectant al server!", Toast.LENGTH_SHORT).show();
        }
        else if(response==10){
            Toast.makeText(UserManagement.this, "Sessió finalitzada!", Toast.LENGTH_SHORT).show();
            Intent mainActivity = new Intent(UserManagement.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        }
        else if(response==3010 || response==4010){
            Toast.makeText(UserManagement.this, "Usuari inexistent!", Toast.LENGTH_SHORT).show();
        }else if(response==0){
            Toast.makeText(UserManagement.this, "ERROR del servidor!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(UserManagement.this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * HashMap for adding user/admin
     * @param isAdmin
     * @return HashMap fulfilled
     */
    private HashMap<String, String> addUserHash(boolean isAdmin) {
        HashMap<String, String> addUserHash = new HashMap<String, String>();
        if (isAdmin) {
            addUserHash.put("accio", "afegir_admin");
            addUserHash.put("nom_admin", userName);
        } else {
            addUserHash.put("accio", "afegir_usuari");
            addUserHash.put("user_name", userName);
        }
        addUserHash.put("codi", String.valueOf(sessionCode));
        addUserHash.put("password", pass);
        addUserHash.put("nom", nom);
        addUserHash.put("dni", dni);
        addUserHash.put("data_alta", date);
        addUserHash.put("correu", email);
        addUserHash.put("admin_alta", nameUser);
        addUserHash.put("cognoms", lastName);
        addUserHash.put("data_naixement", birthDate);
        addUserHash.put("pais", country);
        addUserHash.put("direccio", address);

        return addUserHash;
    }

    /**
     * Hash for deleting user/admin
     * @param isAdmin
     * @return HashMap fulfilled
     */
    private HashMap<String, String> deleteUserHash(boolean isAdmin) {
        HashMap<String, String> deleteUserHash = new HashMap<String, String>();
        if (isAdmin) {
            deleteUserHash.put("accio", "esborrar_admin");
            deleteUserHash.put("nom_admin", userSelected);
        } else {
            deleteUserHash.put("accio", "esborrar_usuari");
            deleteUserHash.put("user_name", userSelected);
        }
        deleteUserHash.put("codi", String.valueOf(sessionCode));

        return deleteUserHash;
    }


    //TODO CLEAN TASKS
    /**
     * Execute task to add User
     */
    private class AddUserTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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

        @Override
        protected void onPostExecute(Integer response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();

            try{
                if(response==1000 || response == 2000) {
                    Toast.makeText(UserManagement.this, "Usuari afegit!", Toast.LENGTH_LONG).show();
                    addUserDialog.dismiss();
                }
                else if(response==1){
                    Toast.makeText(UserManagement.this, "Error conectant al server!", Toast.LENGTH_LONG).show();
                    addUserDialog.dismiss();
                }
                else if(response==10){
                    Toast.makeText(UserManagement.this, "Sessió finalitzada!", Toast.LENGTH_LONG).show();
                    addUserDialog.dismiss();
                    Intent mainActivity = new Intent(UserManagement.this, MainActivity.class);
                    startActivity(mainActivity);
                }
                else if(response==1010 || response==2010){
                    Toast.makeText(UserManagement.this, "Usuari no valid!", Toast.LENGTH_LONG).show();
                }else if(response==1020 || response==2020){
                    Toast.makeText(UserManagement.this, "Contrassenya no vàlida!", Toast.LENGTH_LONG).show();
                }else if(response==1030 || response==2030){
                    Toast.makeText(UserManagement.this, "Format DNI incorrecte!", Toast.LENGTH_LONG).show();
                }else if(response==1031 || response==2031){
                    Toast.makeText(UserManagement.this, "DNI repetit!", Toast.LENGTH_LONG).show();
                }else if(response==1040 || response==2040){
                    Toast.makeText(UserManagement.this, "Email incorrecte!", Toast.LENGTH_LONG).show();
                }else if(response==1041 || response==2041){
                    Toast.makeText(UserManagement.this, "Email ja existeix!", Toast.LENGTH_LONG).show();
                }else if(response==0){
                    Toast.makeText(UserManagement.this, "ERROR del servidor!", Toast.LENGTH_LONG).show();
                    addUserDialog.dismiss();
                }else{
                    Toast.makeText(UserManagement.this, "Error!", Toast.LENGTH_LONG).show();
                    addUserDialog.dismiss();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                addUserDialog.dismiss();
            }
        }
    }

    /**
     * Execute task to delete User/admin
     */
    private class DeleteUserTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
                if(response==3000 || response == 4000) {
                    Toast.makeText(UserManagement.this, "Usuari eliminat!", Toast.LENGTH_LONG).show();
                }
                else if(response==1){
                    Toast.makeText(UserManagement.this, "Error conectant al server!", Toast.LENGTH_LONG).show();
                }
                else if(response==10){
                    Toast.makeText(UserManagement.this, "Sessió finalitzada!", Toast.LENGTH_LONG).show();
                    Intent mainActivity = new Intent(UserManagement.this, MainActivity.class);
                    startActivity(mainActivity);
                }
                else if(response==3010 || response==4010){
                    Toast.makeText(UserManagement.this, "Usuari inexistent!", Toast.LENGTH_LONG).show();
                }else if(response==0){
                    Toast.makeText(UserManagement.this, "ERROR del servidor!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(UserManagement.this, "Error!", Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }
}