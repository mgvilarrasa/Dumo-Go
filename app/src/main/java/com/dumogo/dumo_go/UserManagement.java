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
import android.widget.RadioGroup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import utilities.DatePickerFragment;
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
    private RadioGroup mTypeUsers;
    //Variables d'us
    private String[] listUsers;
    private String userSelected;
    private ArrayList<HashMap<String, String>> usersHashList;
    private Context context = this;
    private boolean getListOk;
    //Adapter
    ArrayAdapter<String> adapter;
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
    private String phone;
    //Diàlegs
    private Dialog addUserDialog;

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
        //Inicialitza view variables
        mAddUser = (Button) findViewById(R.id.bt_add_user);
        mDeleteUser = (Button) findViewById(R.id.bt_delete_user);
        mTypeUsers = (RadioGroup) findViewById(R.id.rg_um_typeUsers);
        mUsersRb = (RadioButton) findViewById(R.id.rb_User);
        mAdminsRb = (RadioButton) findViewById(R.id.rb_Admin);
        mUsersRb.setChecked(true);
        //Cercador de usuaris
        mUserAc = (AutoCompleteTextView) findViewById(R.id.ac_users);
        //Llista d'usuaris
        mUserList = (ListView) findViewById(R.id.user_list);
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
        //Listener radioGroup
        mTypeUsers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                loadUsers();
            }
        });

        loadUsers();
    }

    /**
     * Execute task to get users
     * if error, goes back on app
     * if ok, loads list of users
     */
    private void loadUsers() {
        GetListTask getListTask = new GetListTask();
        if(mUsersRb.isChecked()){
            getListTask.execute(getListUsersHash());
        }
        if(mAdminsRb.isChecked()){
            getListTask.execute(getListAdminsHash());
        }
    }

    /**
     * Generates list of users as String vector
     * @param hashList List of users hashmap
     * @return String vecor with users names
     */
    private String[] listUsers(ArrayList<HashMap<String, String>> hashList) {
        int numItems = hashList.size();
        String crida = "";
        String[] users = new String[numItems];
        if(mUsersRb.isChecked()){
            crida = "nom_user";
        }
        else if(mAdminsRb.isChecked()){
            crida = "nom_admin";
        }

        for(int i=0; i<numItems; i++){
            users[i]=hashList.get(i).get(crida);
        }
        return users;
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
                    AddUserTask addUserTask = new AddUserTask();
                    if(mIsAdmin.isChecked()) {
                        addUserTask.execute(addUserHash(true));
                    }
                    else{
                        addUserTask.execute(addUserHash(false));
                    }
                } else {
                    Toast.makeText(UserManagement.this, "Introduir dades!", Toast.LENGTH_SHORT).show();
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
                    DeleteUserTask deleteUserTask = new DeleteUserTask();
                    if(mAdminsRb.isChecked()){
                        deleteUserTask.execute(deleteUserHash(true));
                    }
                    else{
                        deleteUserTask.execute(deleteUserHash(false));
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
            Toast.makeText(UserManagement.this, "Usuari no seleccionat", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * HashMap for adding user/admin
     * @param isAdmin admin or user
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

    /**
     * Method to generate HashMap to get list of users
     * @return hashMap completed
     */
    private HashMap<String, String> getListUsersHash(){
        HashMap<String, String> usersHash = new HashMap<String, String>();
        usersHash.put("accio", "llista_usuaris");
        usersHash.put("codi", String.valueOf(sessionCode));

        return usersHash;
    }

    /**
     * Method to generate HashMap to get list of admins
     * @return hashMap completed
     */
    private HashMap<String, String> getListAdminsHash(){
        HashMap<String, String> adminsHash = new HashMap<String, String>();
        adminsHash.put("accio", "llista_admins");
        adminsHash.put("codi", String.valueOf(sessionCode));

        return adminsHash;
    }

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
                    Toast.makeText(UserManagement.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    addUserDialog.dismiss();
                    Intent mainActivity = new Intent(UserManagement.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(UserManagement.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
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
                //TODO change back
                Log.e("E/TCP Client IO", "ex.getMessage()");
                return 0;
            } catch (ClassNotFoundException ex) {
                Log.e("E/TCP Client CNF", ex.getMessage());
                return 0;
            }
        }
        //Recorre el ResultSet i obté les dades
        @Override
        protected void onPostExecute(Integer response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();

            try{
                if(response==10){
                    Toast.makeText(UserManagement.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(UserManagement.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(UserManagement.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }

    /**
     * Execute task to get list of users (admin or not)
     */
    private class GetListTask extends AsyncTask<HashMap<String, String>, Void, ArrayList<HashMap<String, String>>> {
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
        protected ArrayList<HashMap<String, String>> doInBackground(HashMap<String, String>... values){
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
                ArrayList<HashMap<String, String>> received = (ArrayList) input.readObject();
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
        protected void onPostExecute(ArrayList<HashMap<String, String>> response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();

            try{
                if(response==null) {
                    Toast.makeText(UserManagement.this, "Error!", Toast.LENGTH_SHORT).show();
                    getListOk=false;
                }
                else{
                    if(response.get(0).get("codi_retorn").equals("0")){
                        Toast.makeText(UserManagement.this, "Error Servidor!", Toast.LENGTH_SHORT).show();
                        getListOk=false;
                    }
                    else if(response.get(0).get("codi_retorn").equals("1100") || response.get(0).get("codi_retorn").equals("1200")){
                        usersHashList = response;
                        getListOk=true;
                        listUsers = listUsers(usersHashList);
                        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, listUsers);
                        mUserAc.setAdapter(adapter);
                        mUserList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(UserManagement.this, "Error!", Toast.LENGTH_SHORT).show();
                        getListOk=false;
                    }
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                getListOk=false;
            }
        }
    }
}