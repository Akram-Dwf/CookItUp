package com.example.cookitup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookitup.R;
import com.example.cookitup.model.Meal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(Meal data);
    }
}
