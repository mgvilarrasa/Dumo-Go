package com.dumogo.dumo_go;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
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

import model.Book;
import utilities.Utils;

public class BookProfile extends AppCompatActivity {

    //Variables d'us
    private static String nameUser;
    private static int sessionCode;
    private static boolean isAdmin;
    private static String bookId;
    private final static String[] ratings = {"1", "2", "3", "4", "5"};
    private final Context context = this;
    //Objecte llibre
    Book book;
    //Array temporal de generes

    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static Socket socket;
    private static InetSocketAddress serverAddr;
    //Variables view
    TextView mTitle;
    TextView mAuthor;
    TextView mPubdate;
    TextView mGenre;
    TextView mDescription;
    RatingBar mRating;
    TextView mBookedBy;
    RecyclerView mRecyclerComments;
    //Text update book
    String newTitle;
    String newAuthor;
    String newPubDate;
    String newDesc;
    String newGenre;
    //Dialegs
    Dialog updateBookDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_profile);
        //Agafa les dades passades a l'activity anterior
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
            isAdmin = extras.getBoolean("IS_ADMIN");
            bookId = extras.getString("LLIBRE");
        }
        //Inicialitza variables view
        mTitle = findViewById(R.id.tv_bp_title);
        mAuthor = findViewById(R.id.tv_bp_author);
        mPubdate = findViewById(R.id.tv_bp_pubDate);
        mGenre = findViewById(R.id.tv_bp_genre);
        mDescription = findViewById(R.id.tv_bp_desc);
        mBookedBy = findViewById(R.id.tv_bp_booked);
        mRating = findViewById(R.id.bp_rateBook);
        mRecyclerComments = findViewById(R.id.rv_bp_comments);

        GetBookInfoTask getBookInfoTask = new GetBookInfoTask();
        getBookInfoTask.execute(getBookInfoHash());
    }

    //Menu superior per tenir opcions d'afegir o eliminar llibres si admin
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(isAdmin){
            getMenuInflater().inflate(R.menu.admin_book_menu, menu);
            return true;
        }
        else{
            getMenuInflater().inflate(R.menu.user_book_menu, menu);
            return true;
        }
    }

    //Opcions llibre si admin
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //ADMIN
        if(item.getItemId()==R.id.bm_delete){
            deleteBookDialog();
        }
        if(item.getItemId()==R.id.bm_update){
            updateBookDialog();
        }
        //USER
        if(item.getItemId()==R.id.bm_user_booking){
            //TODO Update book
        }
        if(item.getItemId()==R.id.bm_user_comment){
            //TODO Comment to book
        }
        if(item.getItemId()==R.id.bm_user_rate){
            //TODO rate book
        }
        return true;
    }

    /**
     * Delete book (only Admin)
     */
    private void deleteBookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar llibre");
        builder.setMessage("Eliminar el llibre " + book.getTitle() + "?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteBookTask deleteBookTask = new DeleteBookTask();
                deleteBookTask.execute(deleteBookHash());
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

    private void updateBookDialog(){
        //Inicialitza dialeg
        updateBookDialog = new Dialog(context);
        updateBookDialog.setContentView(R.layout.update_book_dialog);
        //Inicialitza variables View
        TextView mUbTitle = (TextView) updateBookDialog.findViewById(R.id.tv_ub_bookName);
        EditText mUbNewTitle = (EditText) updateBookDialog.findViewById(R.id.et_ub_newName);
        EditText mUbAuthor = (EditText) updateBookDialog.findViewById(R.id.et_ub_author);
        EditText mUbPubDate = (EditText) updateBookDialog.findViewById(R.id.et_ub_pubDate);
        EditText mUbDesc = (EditText) updateBookDialog.findViewById(R.id.et_ub_desc);
        Spinner mUbGenre = (Spinner) updateBookDialog.findViewById(R.id.sp_ub_genre);
        Button mUbUpdate = (Button) updateBookDialog.findViewById(R.id.bt_ub_save);
        Button mUbExit = (Button) updateBookDialog.findViewById(R.id.bt_ub_exit);
        //Mostra dades llibre a modificar
        mUbTitle.setText(book.getTitle());
        mUbAuthor.setText(book.getAuthor());
        mUbPubDate.setText(book.getPublishDate());
        mUbDesc.setText(book.getDescription());

        //Adapter valoracio
        ArrayAdapter<String> adapterUpdateRate = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Utils.topicsNonAll);
        adapterUpdateRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUbGenre.setAdapter(adapterUpdateRate);
        mUbGenre.setSelection(adapterUpdateRate.getPosition(book.getGenre()));
        //Listeners botons
        mUbExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBookDialog.dismiss();
            }
        });
        mUbUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUbAuthor.getText().toString().trim().length()>0 && mUbDesc.getText().toString().trim().length()>0 &&
                        mUbPubDate.getText().toString().trim().length()>0 && mUbGenre.getSelectedItem().toString().trim().length()>0){
                    if(mUbNewTitle.getText().toString().trim().length()>0){
                        newTitle = mUbNewTitle.getText().toString();
                    }else{
                        newTitle = mUbTitle.getText().toString();
                    }
                    newAuthor = mUbAuthor.getText().toString();
                    newDesc = mUbDesc.getText().toString();
                    newPubDate = mUbPubDate.getText().toString();
                    newGenre = mUbGenre.getSelectedItem().toString();
                    UpdateBookTask updateBookTask = new UpdateBookTask();
                    updateBookTask.execute(updateBookHash());
                }
                else{
                    Toast.makeText(BookProfile.this, "Omplir camps obligatoris", Toast.LENGTH_SHORT).show();
                }
            }
        });
        updateBookDialog.show();
    }

    /**
     * Shows data on View
     */
    private void loadData() {
        mTitle.setText(book.getTitle());
        mAuthor.setText(book.getAuthor());
        mPubdate.setText(book.getPublishDate());
        mGenre.setText(book.getGenre());
        mDescription.setText(book.getDescription());
        if(book.getBookedBy().equals("LLIURE")){
            mBookedBy.setText("Llibre no reservat");
        }
        else{
            mBookedBy.setText("Reservat per: " + book.getBookedBy());
        }
        mRating.setRating(Float.parseFloat(book.getRate()));
    }

    /**
     * Creates hashMap for getting Book's info
     * @return HashMap fulfilled
     */
    private HashMap<String, String> getBookInfoHash(){
        HashMap<String, String> getInfoHash = new HashMap<>();
        getInfoHash.put("codi", String.valueOf(sessionCode));
        getInfoHash.put("accio", "mostra_llibre");
        getInfoHash.put("id", bookId);

        return getInfoHash;
    }
    /**
     * Creates hashMap for deleting book
     * @return HashMap fulfilled
     */
    private HashMap<String, String> deleteBookHash(){
        HashMap<String, String> getInfoHash = new HashMap<>();
        getInfoHash.put("codi", String.valueOf(sessionCode));
        getInfoHash.put("accio", "esborrar_llibre");
        getInfoHash.put("id", book.getId());

        return getInfoHash;
    }

    /**
     * Creates hashMap for updating book
     * @return HashMap fulfilled
     */
    private HashMap<String, String> updateBookHash(){
        HashMap<String, String> updateBookHash = new HashMap<>();
        updateBookHash.put("codi", String.valueOf(sessionCode));
        updateBookHash.put("accio", "modifica_llibre");
        updateBookHash.put("nom", book.getTitle());
        updateBookHash.put("nou_nom", newTitle);
        updateBookHash.put("autor", newAuthor);
        updateBookHash.put("any_publicacio", newPubDate);
        updateBookHash.put("tipus", newGenre);
        updateBookHash.put("descripcio", newDesc);
        updateBookHash.put("caratula", null);

        return updateBookHash;
    }

    /**
     * Task for getting book's information
     */
    private class GetBookInfoTask extends AsyncTask<HashMap<String, String>, Void, HashMap<String, String>> {
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
                //TODO change back
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
                    Toast.makeText(BookProfile.this, "Error consultant dades", Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(BookProfile.this, BooksActivity.class);
                    startActivity(mainActivity);
                    finish();
                }
                else{
                    if(response.get("codi_retorn").equals("1600")){
                        book = Utils.hashToBook(response);
                        loadData();
                    }
                    else if(response.get("codi_retorn").equals("0")){
                        Toast.makeText(BookProfile.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                        Intent mainActivity = new Intent(BookProfile.this, BooksActivity.class);
                        startActivity(mainActivity);
                        finish();
                    }
                    else{
                        Toast.makeText(BookProfile.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                        Intent mainActivity = new Intent(BookProfile.this, BooksActivity.class);
                        startActivity(mainActivity);
                        finish();
                    }
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                Intent mainActivity = new Intent(BookProfile.this, BooksActivity.class);
                startActivity(mainActivity);
                finish();
            }
        }
    }

    /**
     * Execute task to delete Book
     */
    private class DeleteBookTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
                return null;
            } catch (SocketTimeoutException ex){
                Log.e("E/TCP Client TimeOut", ex.getMessage());
                return 1;
            } catch (IOException ex) {
                Log.e("E/TCP Client IO", "ex.getMessage()");
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
                    Toast.makeText(BookProfile.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(BookProfile.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(BookProfile.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    Intent booksActivity = new Intent(BookProfile.this, BooksActivity.class);
                    startActivity(booksActivity);
                    finish();
                }

            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }

    /**
     * Update book task
     */
    private class UpdateBookTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
                return null;
            } catch (SocketTimeoutException ex){
                Log.e("E/TCP Client TimeOut", ex.getMessage());
                return 1;
            } catch (IOException ex) {
                Log.e("E/TCP Client IO", "ex.getMessage()");
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
                    Toast.makeText(BookProfile.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(BookProfile.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(BookProfile.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    updateBookDialog.dismiss();
                    GetBookInfoTask getBookInfoTask = new GetBookInfoTask();
                    getBookInfoTask.execute(getBookInfoHash());
                }

            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }
}