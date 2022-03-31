package dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.dumogo.dumo_go.R;

/**
 * Loading dialog
 */
public class Loading {
    //Variables
    Activity activity;
    AlertDialog dialog;

    //Contructor
    public Loading (Activity activity){
        this.activity=activity;
    }

    /**
     * Creates and shows dialog
     */
    public void startLoading(){
        //Crea l'alerta
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //Inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        //Crea el layout del diàleg
        builder.setView(inflater.inflate(R.layout.loading_layout, null));
        //No cancelable
        builder.setCancelable(false);
        //Crea el diàleg i el mostra
        dialog = builder.create();
        dialog.show();
    }

    /**
     * Dismiss dialog
     */
    public void close(){
        dialog.dismiss();
    }
}
