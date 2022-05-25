package com.dumogo.dumo_go;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.List;
import java.util.stream.Collectors;

import Adapter.BookAdapter;
import model.Book;
import utilities.DatePickerFragment;
import utilities.Utils;

public class BooksActivity extends AppCompatActivity {

    //Variables d'us
    private static String nameUser;
    private static int sessionCode;
    private static boolean isAdmin;
    private final static String[] ratings = {"1", "2", "3", "4", "5"};
    private static String selectedRate;
    private static String selectedTopic;
    private Context context = this;
    private boolean getListOk;
    private String[] listTitles;
    private String[] listAuthors;
    private List<Book> listBooks;
    private ArrayList<HashMap<String, String>> booksHashList;
    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static Socket socket;
    private static InetSocketAddress serverAddr;
    //Variables view
    private Spinner mByType;
    private Spinner mByRate;
    private AutoCompleteTextView mByAuthor;
    private AutoCompleteTextView mByTitle;
    //CardView Books
    private RecyclerView mRecyclerBooks;
    private BookAdapter bookAdapter;
    //Adapter
    ArrayAdapter<String> authorAdapter;
    ArrayAdapter<String> titleAdapter;
    //Text add book
    private String titol;
    private String autor;
    private String data_publicacio;
    private String tipus;
    private String data_alta;
    private String caratula;
    private String descripcio;
    private String valoracio;
    //Dialegs
    private Dialog addBookDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        //Agafa les dades passades a l'activity anterior
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nameUser = extras.getString("NOM");
            sessionCode = extras.getInt("CODI_SESSIO");
            isAdmin = extras.getBoolean("IS_ADMIN");
        }
        //Inicialitza variables view
        mByType = (Spinner) findViewById(R.id.sp_books_topic);
        mByRate = (Spinner) findViewById(R.id.sp_books_rate);
        mByAuthor = (AutoCompleteTextView) findViewById(R.id.ac_books_author);
        mByTitle = (AutoCompleteTextView) findViewById(R.id.ac_books_title);
        mRecyclerBooks = findViewById(R.id.recyclerView_books);
        mRecyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        //Adapter valoracio
        ArrayAdapter<String> adapterRate = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ratings);
        adapterRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mByRate.setAdapter(adapterRate);
        mByRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedRate = adapterRate.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedRate = "null";
            }
        });
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Utils.topics);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mByType.setAdapter(adapterType);
        mByType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedTopic = adapterType.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedTopic = "null";
            }
        });
        GetListTask getListTask = new GetListTask();
        getListTask.execute(getListBooksHash());
    }

    //Menu superior per tenir opcions d'afegir o eliminar llibres si admin
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(isAdmin){
            getMenuInflater().inflate(R.menu.books_admin_menu, menu);
            return true;
        }
        else{
            getMenuInflater().inflate(R.menu.books_user_menu, menu);
            return true;
        }

    }
    //Opcions llibre si admin
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.add_book){
            addBookDialog();
        }
        else if(item.getItemId()==R.id.filter_book || item.getItemId()==R.id.filter_book_user){
            loadBookCards(sortBooks());
        }
        else{
            return super.onContextItemSelected(item);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<Book> sortBooks(){
        List<Book> sortedList = new ArrayList<>();
        //Check for filters
        String typeFilter = mByType.getSelectedItem().toString();
        String rateFilter = mByRate.getSelectedItem().toString();
        String titleFilter = mByTitle.getText().toString();
        String authorFilter = mByAuthor.getText().toString();

        if(typeFilter.equals("Tots")){
            if(titleFilter.equals("")){
                if(authorFilter.equals("")){
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter)).collect(Collectors.toList());
                }else{
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getAuthor().equals(authorFilter)).collect(Collectors.toList());
                }
            }else{
                if(authorFilter.equals("")){
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getTitle().equals(titleFilter)).collect(Collectors.toList());
                }else{
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getTitle().equals(titleFilter) &&
                            p.getAuthor().equals(authorFilter)).collect(Collectors.toList());
                }
            }
        }
        else{
            if(titleFilter.equals("")){
                if(authorFilter.equals("")){
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getGenre().equals(typeFilter)).collect(Collectors.toList());
                }else{
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getGenre().equals(typeFilter) &&
                            p.getAuthor().equals(authorFilter)).collect(Collectors.toList());
                }
            }else{
                if(authorFilter.equals("")){
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getGenre().equals(typeFilter) &&
                            p.getTitle().equals(titleFilter)).collect(Collectors.toList());
                }else{
                    sortedList = listBooks.stream().filter(p -> Integer.parseInt(p.getRate())>Integer.parseInt(rateFilter) &&
                            p.getGenre().equals(typeFilter) &&
                            p.getTitle().equals(titleFilter) &&
                            p.getAuthor().equals(authorFilter)).collect(Collectors.toList());
                }
            }
        }

        return sortedList;
    }

    /**
     * HashMap for adding book
     * @return HashMap fulfilled
     */
    private HashMap<String, String> addBookHash() {
        HashMap<String, String> addBookHash = new HashMap<String, String>();
        addBookHash.put("accio", "afegir_llibre");
        addBookHash.put("codi", String.valueOf(sessionCode));
        addBookHash.put("nom", titol);
        addBookHash.put("autor", autor);
        addBookHash.put("data_alta", data_alta);
        addBookHash.put("data_publicacio", data_publicacio);
        addBookHash.put("admin_alta", nameUser);
        addBookHash.put("tipus", tipus);
        addBookHash.put("caratula", caratula);
        addBookHash.put("descripcio", descripcio);
        addBookHash.put("valoracio", valoracio);

        return addBookHash;
    }

    /**
     * HashMap for getting list of books
     * @return HashMap fulfilled
     */
    private HashMap<String, String> getListBooksHash(){
        HashMap<String, String> getListBooksHash = new HashMap<>();
        getListBooksHash.put("codi", String.valueOf(sessionCode));
        getListBooksHash.put("accio", "llista_llibres");

        return getListBooksHash;
    }

    /**
     * Populates array of books titles on autocomplete EditText
     * @param hashList ArrayList with books info in hashMaps
     * @return array to display on autocomplete EditText
     */
    private String[] getListTitles(ArrayList<HashMap<String, String>> hashList) {
        int numItems = hashList.size();
        String[] titles = new String[numItems];

        for(int i=0; i<numItems; i++){
            titles[i]=hashList.get(i).get("nom");
        }
        return titles;
    }

    /**
     * Populates array of books authors on autocomplete EditText
     * @param hashList ArrayList with books info in hashMaps
     * @return array to display on autocomplete EditText
     */
    private String[] getListAuthors(ArrayList<HashMap<String, String>> hashList) {
        int numItems = hashList.size();
        String[] authors = new String[numItems];

        for(int i=0; i<numItems; i++){
            authors[i]=hashList.get(i).get("autor");
        }
        return authors;
    }

    /**
     * Inflates recycler view with Book cards
     * @param updatedListBooks list of current books to show
     */
    private void loadBookCards(List<Book> updatedListBooks){
        bookAdapter = new BookAdapter(updatedListBooks, context, nameUser, sessionCode, isAdmin);
        mRecyclerBooks.setAdapter(bookAdapter);
    }

    /**
     * Dialog to add a book
     */
    private void addBookDialog(){
        //Crea el diàleg de canvi de contrassenya
        addBookDialog = new Dialog(context);
        addBookDialog.setContentView(R.layout.add_book_dialog);
        //Camps de text
        EditText mTitle = (EditText) addBookDialog.findViewById(R.id.et_ab_name);
        EditText mAuthor = (EditText) addBookDialog.findViewById(R.id.et_ab_author);
        EditText mPublishDate = (EditText) addBookDialog.findViewById(R.id.et_ab_publish_date);
        Spinner mType = (Spinner) addBookDialog.findViewById(R.id.et_ab_type);
        EditText mCreateDate = (EditText) addBookDialog.findViewById(R.id.et_ab_create_date);
        EditText mCover = (EditText) addBookDialog.findViewById(R.id.et_ab_cover);
        EditText mDescription = (EditText) addBookDialog.findViewById(R.id.et_ab_description);
        Spinner mRate = (Spinner) addBookDialog.findViewById(R.id.et_ab_rate);
        //Adapter valoracio
        ArrayAdapter<String> adapterAddRate = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ratings);
        adapterAddRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRate.setAdapter(adapterAddRate);
        mRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedRate = adapterAddRate.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedRate = "null";
            }
        });
        //Adapter genere
        ArrayAdapter<String> adapterAddType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Utils.topicsNonAll);
        adapterAddType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapterAddType);
        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedTopic = adapterAddType.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedTopic = "null";
            }
        });
        //Date picker
        mCreateDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        //Popula la data al editText
                        DecimalFormat mFormat = new DecimalFormat("00");
                        final String selectedDate = year + "-" + mFormat.format(Double.valueOf(month + 1)) + "-" + mFormat.format(Double.valueOf(day));
                        mCreateDate.setText(selectedDate);
                    }
                });

                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        addBookDialog.show();
        //Botó tancar diàleg
        Button closeDialog = addBookDialog.findViewById(R.id.bt_ab_exit);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBookDialog.dismiss();
            }
        });
        //Botó guardar
        Button addUser = addBookDialog.findViewById(R.id.bt_ab_add);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTitle.getText().toString().trim().length() > 0 && mAuthor.getText().toString().trim().length() > 0 && mPublishDate.getText().toString().trim().length() > 0
                        && mType.getSelectedItem().toString().trim().length() > 0 && mCreateDate.getText().toString().trim().length() > 0 && mCover.getText().toString().trim().length() > 0
                        && mDescription.getText().toString().trim().length() > 0 && mRate.getSelectedItem().toString().trim().length() > 0) {
                    //Guarda dades per hashMap
                    titol = mTitle.getText().toString();
                    autor = mAuthor.getText().toString();
                    data_publicacio = mPublishDate.getText().toString();
                    tipus = mType.getSelectedItem().toString();
                    data_alta = mCreateDate.getText().toString();
                    caratula = mCover.getText().toString();
                    descripcio = mDescription.getText().toString();
                    valoracio = mRate.getSelectedItem().toString();
                    //Tasca per afegir llibre
                    AddBookTask addBookTask = new AddBookTask();
                    addBookTask.execute(addBookHash());
                } else {
                    Toast.makeText(BooksActivity.this, "Introduir dades!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Execute task to add Book
     */
    private class AddBookTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
                    Toast.makeText(BooksActivity.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    addBookDialog.dismiss();
                    Intent mainActivity = new Intent(BooksActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                }else{
                    Toast.makeText(BooksActivity.this, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                    addBookDialog.dismiss();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
                addBookDialog.dismiss();
            }
        }
    }

    /**
     * Execute task to get list of books
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
                    Toast.makeText(BooksActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    getListOk=false;
                }
                else{
                    if(response.get(0).get("codi_retorn").equals("0")){
                        Toast.makeText(BooksActivity.this, "Error Servidor!", Toast.LENGTH_SHORT).show();
                        getListOk=false;
                    }
                    else if(response.get(0).get("codi_retorn").equals("1700")){
                        booksHashList = response;
                        getListOk=true;
                        //Llistes
                        listBooks = Utils.bookList(booksHashList);
                        listTitles = getListTitles(booksHashList);
                        listAuthors = getListAuthors(booksHashList);
                        //Adapters
                        titleAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, listTitles);
                        mByTitle.setAdapter(titleAdapter);
                        authorAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, listAuthors);
                        mByAuthor.setAdapter(authorAdapter);

                        loadBookCards(listBooks);

                        titleAdapter.notifyDataSetChanged();
                        authorAdapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(BooksActivity.this, "Error!", Toast.LENGTH_SHORT).show();
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