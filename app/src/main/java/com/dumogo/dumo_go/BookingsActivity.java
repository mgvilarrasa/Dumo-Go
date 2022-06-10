package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLSocket;

import Adapter.BookingAdapter;
import Adapter.CommentAdapter;
import model.Booking;
import model.Comment;
import utilities.Utils;

public class BookingsActivity extends AppCompatActivity {

    //Variables d'us
    private static String nameUser;
    private static int sessionCode;
    private static boolean isAdmin;
    private static String bookId;
    private final Context context = this;
    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static SSLSocket socket;
    private static InetSocketAddress serverAddr;
    private RecyclerView mRecyclerBookings;
    private BookingAdapter bookingAdapter;
    //Llistes
    private List<Booking> listBookings;
    private ArrayList<HashMap<String, String>> bookingHashList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);
        //Agafa les dades passades a l'activity anterior
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
            isAdmin = extras.getBoolean("IS_ADMIN");
            bookId = extras.getString("LLIBRE");
        }
        mRecyclerBookings = findViewById(R.id.rv_ba_bookings);
        mRecyclerBookings.setLayoutManager(new LinearLayoutManager(this));
        if(isAdmin){
            GetBookingsTask getBookingsTask = new GetBookingsTask();
            getBookingsTask.execute(getAllBookingsHash());
        }
        else{
            GetBookingsTask getBookingsTask = new GetBookingsTask();
            getBookingsTask.execute(getBookingsByUserHash());
        }
    }

    private void loadBookingCards(List<Booking> updatedListBookings){
        bookingAdapter = new BookingAdapter(updatedListBookings, context, nameUser, sessionCode, isAdmin);
        mRecyclerBookings.setAdapter(bookingAdapter);
    }

    private HashMap<String, String> getBookingsByUserHash() {
        HashMap<String, String> getBookingsByUserHash = new HashMap<>();
        getBookingsByUserHash.put("codi", String.valueOf(sessionCode));
        getBookingsByUserHash.put("accio", "llista_prestecs_usuari");

        return getBookingsByUserHash;
    }

    private HashMap<String, String> getAllBookingsHash() {
        HashMap<String, String> getAllBookingsHash = new HashMap<>();
        getAllBookingsHash.put("codi", String.valueOf(sessionCode));
        getAllBookingsHash.put("accio", "llista_prestecs");

        return getAllBookingsHash;
    }

    /**
     * Execute task to get list of bookings (by user or all)
     */
    private class GetBookingsTask extends AsyncTask<HashMap<String, String>, Void, ArrayList<HashMap<String, String>>> {
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
                KeyStore ks = KeyStore.getInstance("BKS");
                // Load the keystore file
                InputStream keyin = context.getResources().openRawResource(R.raw.clienttruststore);
                ks.load(keyin, "dumogo2022".toCharArray());
                //SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocketFactory socketFactory = new SSLSocketFactory(ks);
                socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                socket = (SSLSocket) socketFactory.createSocket(new Socket(ADDRESS, SERVERPORT),ADDRESS, SERVERPORT, false);
                socket.setSoTimeout(5000);
                socket.startHandshake();
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
            }catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();

            try{
                if(response==null) {
                    Toast.makeText(BookingsActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(response.get(0).get("codi_retorn").equals("0")){
                        Toast.makeText(BookingsActivity.this, "Error Servidor!", Toast.LENGTH_SHORT).show();
                    }
                    else if(response.get(0).get("codi_retorn").equals("2300") || response.get(0).get("codi_retorn").equals("2400")){
                        if(response.get(0).get("id_reserva")!=null){
                            bookingHashList = response;
                            //Llistes
                            listBookings = Utils.bookingList(bookingHashList);
                            //Carrega RecyclerView
                            loadBookingCards(listBookings);
                        }
                        else{
                            Toast.makeText(BookingsActivity.this, "No hi ha prestecs", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(BookingsActivity.this, "No hi ha prestecs", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }
}