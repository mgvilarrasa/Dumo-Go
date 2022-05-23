package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dumogo.dumo_go.R;

import java.util.List;

import model.Book;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder>{

    private List<Book> bookList;
    private Context context;


    public BookAdapter(List<Book> bookList, Context context) {
        this.bookList = bookList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTitle.setText(bookList.get(position).getTitle());
        holder.mAuthor.setText(bookList.get(position).getAuthor());
        holder.mGenre.setText(bookList.get(position).getGenre());
        holder.mRate.setText(bookList.get(position).getRate());
        //Go Book Activity
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Go Book Profile Activity
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView mCover;
        private TextView mTitle;
        private TextView mAuthor;
        private TextView mGenre;
        private TextView mRate;

        private View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.iv_cv_books_cover);
            mTitle = (TextView) itemView.findViewById(R.id.tv_cv_books_title);
            mAuthor = (TextView) itemView.findViewById(R.id.tv_cv_books_author);
            mGenre = (TextView) itemView.findViewById(R.id.tv_cv_books_genre);
            mRate = (TextView) itemView.findViewById(R.id.tv_cv_books_rate);
            this.view = itemView;
        }
    }

}
