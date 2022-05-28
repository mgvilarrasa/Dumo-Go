package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    private ImageButton mBooks;
    private ImageButton mBookings;
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
        //Boto llibres
        mBooks = (ImageButton) findViewById(R.id.ibt_books_admin);
        mBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle d'informacio per enviar a la següent activity
                Bundle extrasBook = new Bundle();
                extrasBook.putString("NOM", String.valueOf(nameUser));
                extrasBook.putInt("CODI_SESSIO", sessionCode);
                extrasBook.putBoolean("IS_ADMIN", true);
                //Intent per anar a la pantalla de llibres
                Intent intentProfile = new Intent(AdminMain.this, BooksActivity.class);
                //S'afegeixen els extras
                intentProfile.putExtras(extrasBook);
                startActivity(intentProfile);
            }
        });
        //Boto reserves
        mBookings = (ImageButton) findViewById(R.id.ibt_bookings);
        mBookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle d'informacio per enviar a la següent activity
                Bundle extrasBook = new Bundle();
                extrasBook.putString("NOM", String.valueOf(nameUser));
                extrasBook.putInt("CODI_SESSIO", sessionCode);
                extrasBook.putBoolean("IS_ADMIN", true);
                //Intent per anar a la pantalla de llibres
                Intent intentBookings = new Intent(AdminMain.this, BookingsActivity.class);
                //S'afegeixen els extras
                intentBookings.putExtras(extrasBook);
                startActivity(intentBookings);
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
            logout();
        }
        else{
            return super.onContextItemSelected(item);
        }
        return true;
    }

    /**
     * Logout app
     */
    private void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Logout");
        builder.setMessage("Sortir de l'aplicacio?");
        builder.setPositiveButton("Sortir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Crea hash de logout
                HashMap<String, String> hashLogout = new HashMap<>();
                hashLogout.put("codi", String.valueOf(sessionCode));
                hashLogout.put("accio", "tancar_sessio");
                //Crida tasca per tancar sessio
                LogoutTask logoutTask = new LogoutTask();
                logoutTask.execute(hashLogout);
            }
        });
        builder.setNegativeButton("Cancela", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
            //Crea hash de logout
            HashMap<String, String> hashLogout = new HashMap<>();
            hashLogout.put("codi", String.valueOf(sessionCode));
            hashLogout.put("accio", "tancar_sessio");
            //Crida tasca per tancar sessio
            LogoutTask logoutTask = new LogoutTask();
            logoutTask.execute(hashLogout);

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

    /**
     * Execute task to Send HashMap and receive Integer
     */
    private class LogoutTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
                exitProgram(response);
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                exitProgram(response);
            }
        }
    }
}