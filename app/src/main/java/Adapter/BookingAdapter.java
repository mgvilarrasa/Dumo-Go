package Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dumogo.dumo_go.BookProfile;
import com.dumogo.dumo_go.R;
import java.util.List;
import model.Booking;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder>{

    private final List<Booking> bookingList;
    private final Context context;
    private final String nameUser;
    private final int sessionCode;
    private final boolean isAdmin;


    public BookingAdapter(List<Booking> bookingList, Context context, String nameUser, int sessionCode, boolean isAdmin) {
        this.bookingList = bookingList;
        this.context = context;
        this.nameUser = nameUser;
        this.sessionCode = sessionCode;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public BookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_card_view, parent, false);
        return new BookingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(bookingList.get(position).getBookName());
        holder.mStartDate.setText("Reservat: " + bookingList.get(position).getStartDate());
        holder.mEndDate.setText("Ultim dia: "+bookingList.get(position).getEndDate());
        holder.mBookedBy.setText("Usuari: "+bookingList.get(position).getUserName());
        holder.mReturned.setChecked(bookingList.get(position).getReturnDate() != null);
        holder.mOutOfTime.setChecked(bookingList.get(position).isLate());

        //Go Book Activity
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle d'informacio per enviar a la següent activity
                Bundle extrasBook = new Bundle();
                extrasBook.putString("NOM", String.valueOf(nameUser));
                extrasBook.putInt("CODI_SESSIO", sessionCode);
                extrasBook.putBoolean("IS_ADMIN", isAdmin);
                extrasBook.putString("LLIBRE", bookingList.get(holder.getAdapterPosition()).getBookId());
                //Intent per anar a la pantalla de llibres
                Intent intentProfile = new Intent(context, BookProfile.class);
                //S'afegeixen els extras
                intentProfile.putExtras(extrasBook);
                context.startActivity(intentProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView mTitle;
        private final TextView mStartDate;
        private final TextView mEndDate;
        private final TextView mBookedBy;
        private final CheckBox mReturned;
        private final CheckBox mOutOfTime;

        private final View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tv_cv_bookings_book);
            mBookedBy = (TextView) itemView.findViewById(R.id.tv_cv_bookings_user);
            mStartDate = (TextView) itemView.findViewById(R.id.tv_cv_bookings_iniDate);
            mEndDate = (TextView) itemView.findViewById(R.id.tv_cv_bookings_endDate);
            mReturned = (CheckBox) itemView.findViewById(R.id.cb_cv_bookings_returned);
            mOutOfTime = (CheckBox) itemView.findViewById(R.id.cb_cv_bookings_urgent);
            this.view = itemView;
        }
    }
}
