package Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dumogo.dumo_go.R;

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
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLSocket;

import model.Comment;
import utilities.Utils;

/**
 * author Marçal Gonzalez Vilarrasa
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    //Dades conexio
    private static final String ADDRESS = Utils.ADDRESS;
    private static final int SERVERPORT = Utils.SERVERPORT;
    private static SSLSocket socket;
    private static InetSocketAddress serverAddr;
    private final List<Comment> commentList;
    private final Context context;
    private final String nameUser;
    private final int sessionCode;
    private final boolean isAdmin;
    private String idCommentToDelete;

    public CommentAdapter(List<Comment> commentList, Context context, String nameUser, int sessionCode, boolean isAdmin) {
        this.commentList = commentList;
        this.context = context;
        this.nameUser = nameUser;
        this.sessionCode = sessionCode;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        holder.mUserName.setText(commentList.get(position).getUser());
        holder.mDate.setText(commentList.get(position).getDate());
        holder.mComment.setText(commentList.get(position).getComment());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAdmin || nameUser.equals(commentList.get(holder.getAdapterPosition()).getUser())){
                    idCommentToDelete = commentList.get(holder.getAdapterPosition()).getId();
                    deleteCommentDialog();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView mUserName;
        private final TextView mDate;
        private final TextView mComment;

        private final View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserName = (TextView) itemView.findViewById(R.id.tv_cv_comments_user);
            mDate = (TextView) itemView.findViewById(R.id.tv_cv_comments_date);
            mComment = (TextView) itemView.findViewById(R.id.tv_cv_comments_comment);

            this.view = itemView;
        }
    }

    /**
     * Delete comment dialog
     */
    private void deleteCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar comentari");
        builder.setMessage("Eliminar el comentari seleccionat?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteCommentTask deletecommentTask = new DeleteCommentTask();
                deletecommentTask.execute(deleteCommentHash());
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
     * HashMap for deleting comment
     * @return hashMap fulfilled
     */
    private HashMap<String, String> deleteCommentHash(){
        HashMap<String, String> getInfoHash = new HashMap<>();
        getInfoHash.put("codi", String.valueOf(sessionCode));
        getInfoHash.put("accio", "elimina_comentari");
        getInfoHash.put("id_comentari", idCommentToDelete);

        return getInfoHash;
    }

    /**
     * Execute task to delete Book
     */
    private class DeleteCommentTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
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
            }catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException e) {
                e.printStackTrace();
                return null;
            }
        }
        //Recorre el ResultSet i obté les dades
        @Override
        protected void onPostExecute(Integer response){
            //Tanca el dialeg de carrega
            progressDialog.dismiss();

            try{
                Toast.makeText(context, Utils.feedbackServer(response), Toast.LENGTH_SHORT).show();
                if(response==2800){
                    Comment commentToRemove = commentList.stream().filter(p -> idCommentToDelete.equals(p.getId())).findAny().orElse(null);
                    commentList.remove(commentToRemove);
                    notifyDataSetChanged();
                }
            }catch (Exception e){
                Log.e("E/TCP Client onPost", e.getMessage());
            }
        }
    }

}
