package com.example.cookitup.adapter;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookitup.R;
import com.example.cookitup.database.DatabaseContract;
import com.example.cookitup.database.MealHelper;
import com.example.cookitup.model.Meal;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private ArrayList<Meal> meals = new ArrayList<>();
    private OnItemClickCallback onItemClickCallback;

    public void setData(ArrayList<Meal> items) {
        meals.clear();
        meals.addAll(items);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.tvName.setText(meal.getStrMeal());
        Picasso.get().load(meal.getStrMealThumb()).into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickCallback != null) {
                onItemClickCallback.onItemClicked(meal);
            }
        });

        // Un-favorite: remove from SQLite and list on heart icon click
        holder.btnRemoveFavorite.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                MealHelper mealHelper = MealHelper.getInstance(v.getContext());
                mealHelper.open();
                long result = mealHelper.deleteById(meal.getIdMeal());
                mealHelper.close();

                handler.post(() -> {
                    if (result > 0) {
                        meals.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(adapterPosition, meals.size());

                        // Snackbar with UNDO to re-insert
                        Snackbar.make(v, v.getContext().getString(R.string.msg_removed), Snackbar.LENGTH_LONG)
                                .setAction(v.getContext().getString(R.string.snackbar_undo), undoView -> {
                                    // Re-insert on UNDO
                                    executor.execute(() -> {
                                        ContentValues values = new ContentValues();
                                        values.put(DatabaseContract.MealColumns.MEAL_ID, meal.getIdMeal());
                                        values.put(DatabaseContract.MealColumns.MEAL_NAME, meal.getStrMeal());
                                        values.put(DatabaseContract.MealColumns.THUMBNAIL, meal.getStrMealThumb());

                                        MealHelper undoHelper = MealHelper.getInstance(undoView.getContext());
                                        undoHelper.open();
                                        long undoResult = undoHelper.insert(values);
                                        undoHelper.close();

                                        handler.post(() -> {
                                            if (undoResult > 0) {
                                                meals.add(adapterPosition, meal);
                                                notifyItemInserted(adapterPosition);
                                            }
                                        });
                                    });
                                })
                                .show();
                    }
                });
            });
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivThumbnail;
        View btnRemoveFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            btnRemoveFavorite = itemView.findViewById(R.id.btn_remove_favorite);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(Meal data);
    }
}
