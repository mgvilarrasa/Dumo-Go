package utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;
import java.util.HashMap;

import model.Book;
import model.User;

/**
 * author Marçal González
 */
public class Utils {
    //Acces xarxa
    public static String ADDRESS = "192.168.1.133";
    public static  int SERVERPORT = 7777;
    //Llistes estatiques
    public final static String[] topics = {"Tots", "Fantasia", "Suspens", "Terror", "Aventures", "Romantica", "Historia", "Ciencia"};
    public final static String[] topicsNonAll = {"Fantasia", "Suspens", "Terror", "Aventures", "Romantica", "Historia", "Ciencia"};

    /**
     * Converts user to hashMap to send to Server
     * @param user user class
     * @return hashMap with user data
     */
    public static HashMap<String, String> userToHash(User user){
        HashMap<String, String> userHash = new HashMap<>();
        //Omple el hashMap amb els valors d'usuari
        userHash.put("user_name", user.getUserName());
        userHash.put("nom", user.getName());
        userHash.put("cognoms", user.getLastName());
        userHash.put("dni", user.getId());
        userHash.put("data_naixement", user.getBirthDate());
        userHash.put("direccio", user.getAddress());
        userHash.put("pais", user.getCountry());
        userHash.put("data_alta", user.getCreateDate());
        userHash.put("telefon", user.getPhoneNum());
        userHash.put("correu", user.getMail());
        return userHash;
    }

    /**
     * Converts hashMap to User to use on App
     * @param userHash hashMap with user data
     * @return user
     */
    public static User hashToUser(HashMap<String, String> userHash){
        //Crea un usuari a partir del hashmap
        User user = new User(
                userHash.get("numero_soci"),
                userHash.get("user_name"),
                userHash.get("nom"),
                userHash.get("cognoms"),
                userHash.get("dni"),
                userHash.get("data_naixement"),
                userHash.get("direccio"),
                userHash.get("pais"),
                userHash.get("data_alta"),
                userHash.get("telefon"),
                userHash.get("correu")
        );

        return user;
    }

    /**
     * Converts user to hashMap to send to Server
     * @param admin admin class
     * @return hashMap for admin
     */
    public static HashMap<String, String> adminToHash(User admin){
        HashMap<String, String> userHash = new HashMap<>();
        //Omple el hashMap amb els valors d'usuari
        userHash.put("admin_name", admin.getUserName());
        userHash.put("nom", admin.getName());
        userHash.put("cognoms", admin.getLastName());
        userHash.put("dni", admin.getId());
        userHash.put("data_naixement", admin.getBirthDate());
        userHash.put("direccio", admin.getAddress());
        userHash.put("pais", admin.getCountry());
        userHash.put("data_alta", admin.getCreateDate());
        userHash.put("telefon", admin.getPhoneNum());
        userHash.put("correu", admin.getMail());
        return userHash;
    }

    /**
     * Converts hashMap to User to use on App
     * @param adminHash hashMap with admin data
     * @return admin
     */
    public static User hashToAdmin(HashMap<String, String> adminHash){
        //Crea un usuari a partir del hashmap
        User admin = new User(
                adminHash.get("numero_soci"),
                adminHash.get("nom_admin"),
                adminHash.get("nom"),
                adminHash.get("cognoms"),
                adminHash.get("dni"),
                adminHash.get("data_naixement"),
                adminHash.get("direccio"),
                adminHash.get("pais"),
                adminHash.get("telefon"),
                adminHash.get("correu")
        );

        return admin;
    }

    /**
     * List of users from list of hashMaps
     * @param hashList list of hashMaps with user's information
     * @return list of users
     */
    public static ArrayList<User> userList(ArrayList<HashMap<String, String>> hashList){
        ArrayList<User> userList = new ArrayList<>();
        //Crea array d'usuaris a partir dels hashMaps amb la seva informacio
        for(HashMap<String, String> userHash : hashList){
            userList.add(hashToUser(userHash));
        }
        return userList;
    }

    /**
     * List of admins from list of hashMaps
     * @param hashList list of hashMaps with user's information
     * @return list of admins
     */
    public static ArrayList<User> adminList(ArrayList<HashMap<String, String>> hashList){
        ArrayList<User> adminList = new ArrayList<>();
        //Crea array d'administradors a partir dels hashMaps amb la seva informacio
        for(HashMap<String, String> adminHash : hashList){
            adminList.add(hashToAdmin(adminHash));
        }
        return adminList;
    }

    /**
     * Converts Book to hashMap to send to Server
     * @param book Book class
     * @return hashMap for book
     */
    public static HashMap<String, String> bookToHash(Book book){
        HashMap<String, String> bookHash = new HashMap<>();
        //Omple el hashMap amb els valors del llibre
        bookHash.put("nom", book.getTitle());
        bookHash.put("autor", book.getAuthor());
        bookHash.put("any_publicacio", book.getPublishDate());
        bookHash.put("tipus", book.getGenre());
        bookHash.put("data_alta", book.getCreateDate());
        bookHash.put("caratula", book.getCover());
        bookHash.put("descripcio", book.getDescription());
        bookHash.put("valoracio", book.getRate());
        bookHash.put("id", book.getId());

        return bookHash;
    }

    /**
     * Converts HashMap with book info to Book class
     * @param bookHash with info from book
     * @return book class
     */
    public static Book hashToBook(HashMap<String, String> bookHash){
        Book book = new Book(
                bookHash.get("nom"),
                bookHash.get("autor"),
                bookHash.get("any_publicacio"),
                bookHash.get("tipus"),
                bookHash.get("data_alta"),
                bookHash.get("caratula"),
                bookHash.get("descripcio"),
                bookHash.get("valoracio"),
                bookHash.get("id")
        );

        book.setBookedBy(bookHash.get("user_name"));

        return book;
    }

    /**
     * List of books from list of hashMaps
     * @param hashList list of hashMaps with book's information
     * @return list of books
     */
    public static ArrayList<Book> bookList(ArrayList<HashMap<String, String>> hashList){
        ArrayList<Book> bookList = new ArrayList<>();
        //Crea array de llibres a partir dels hashMaps amb la seva informacio
        for(HashMap<String, String> bookHash : hashList){
            bookList.add(hashToBook(bookHash));
        }
        return bookList;
    }

    /**
     * Decodes Base64 string and converts to Bitmap
     * @param imageString Base64 String
     * @return bitmap image
     */
    public static Bitmap decodeToImage(String imageString) {
        //Decodifica a byteArray
        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        //Transforma a bitmap i retorna imatge
        Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return image;
    }

    public static String feedbackServer(int code){
        String message = "";
        //Afegir usuari/admin
        if(code == 1000 || code == 2000){
            message = "Usuari afegit";
        }else if(code == 1010 || code == 2010){
            message = "Usuari no valid";
        }else if(code == 1020 || code == 2020){
            message = "Contrassenya no valida";
        }else if(code == 1030 || code == 2030){
            message = "Format DNI incorrecte";
        }else if(code == 1031 || code == 2031){
            message = "DNI ja existeix";
        }else if(code == 1040 || code == 2040){
            message = "Correu incorrecte";
        }else if(code == 1041 || code == 2041){
            message = "Correu repetit";
        }
        //Esborrar usuari/admin
        else if(code == 3000 || code == 4000){
            message = "Usuari esborrat";
        }else if(code == 3010 || code == 4010){
            message = "Usuari inexistent";
        }
        //Mostra usuari/admin
        else if(code == 5000 || code == 6000){
            message = "Mostrant usuari";
        }else if(code == 5010 || code == 6010){
            message = "Usuari no valid";
        }
        //Comprobar usuari/admin
        else if(code == 7010 || code == 8010){
            message = "Usuari no existeix";
        }else if(code == 7020 || code == 8020){
            message = "Contrassenya incorrecta";
        }else if(code == 7030 || code == 8030){
            message = "Usuari ja conectat";
        }
        //Canvia password
        else if(code == 9000){
            message = "Contrassenya modificada";
        }else if(code == 9010){
            message = "Contrassenya no valida";
        }
        //Modifica usuari/admin
        else if(code == 1300){
            message = "Usuari modificat";
        }else if(code == 1310){
            message = "Correu incorrecte";
        }else if(code == 1320){
            message = "DNI incorrecte";
        }else if(code == 1330){
            message = "Contrassenya incorrecta";
        }
        //Afegir llibre
        else if(code == 1400){
            message = "Llibre afegit";
        }
        //Esborrar llibre
        else if(code == 1500){
            message = "Llibre esborrat";
        }else if(code == 1510){
            message = "Llibre inexistent";
        }
        //Modifica llibre
        else if(code == 1800){
            message = "Llibre modificat";
        }else if(code == 1810){
            message = "Llibre no trobat";
        }
        //Mostra llibre
        else if(code == 1600){
            message = "Llibre mostrat";
        }else if(code == 1610){
            message = "Llibre no trobat";
        }
        //Llista llibres
        else if(code == 1700){
            message = "Llista tornada";
        }
        //Puntua llibre
        else if(code == 1900){
            message = "Valoracio afegida";
        }
        //Reserva llibre
        else if(code == 2100){
            message = "Llibre reservat";
        }
        //Generics
        else if(code == 0){
            message = "Error del servidor";
        }else if(code == 10){
            message = "Sessio caducada";
        }else if(code == 1){
            message = "Error conectant al servidor";
        }else{
            message = "Error!";
        }
        return message;
    }
}
