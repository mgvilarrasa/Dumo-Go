package utilities;

import java.util.ArrayList;
import java.util.HashMap;

import model.User;

/**
 * author Marçal González
 */
public class Utils {
    public static String ADDRESS = "192.168.20.97";
    public static  int SERVERPORT = 7777;

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
}
