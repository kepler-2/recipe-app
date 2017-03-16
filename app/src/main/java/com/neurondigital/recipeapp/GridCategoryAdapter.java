package com.neurondigital.recipeapp;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

/**
 * Recipe Adapter to show recipe cards in list
 */
public class GridCategoryAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<Category> categories;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;

    GridCategoryAdapter(List<Category> categories, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.categories = categories;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    public void addItems(List<Recipe> recipes) {
        this.categories.addAll(categories);
    }


    /**
     * Holds the recipe screen elements to avoid creating them multiple times
     */
    public class RecipeViewHolder extends ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView name;
        AspectRatioImageView image;
        IconicsTextView stats;

        RecipeViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.recipe_image);
            name = (TextView) itemView.findViewById(R.id.recipe_name);
            if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS)
                stats = (IconicsTextView) itemView.findViewById(R.id.stats);
            //set image on click listener
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_FULLWIDTH)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_card, viewGroup, false);
        else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_card_2column, viewGroup, false);
        ViewHolder rvh = new RecipeViewHolder(v);
        return rvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder recipeViewHolder, final int i) {
        //set recipe name
        ((RecipeViewHolder) recipeViewHolder).name.setText(categories.get(i).name);

        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            ((RecipeViewHolder) recipeViewHolder).image.setBox(true);
        }
        //load recipe image with picasso
        RequestCreator r = Picasso.with(context).load(categories.get(i).imageUrl).placeholder(R.drawable.loading);
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            r.fit().centerCrop();
        }
        r.into(((RecipeViewHolder) recipeViewHolder).image);

        //set stats
//        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
//            ((RecipeViewHolder) recipeViewHolder).stats.setText("{faw-eye} " + categories.get(i).viewed + "  {faw-star} " + recipes.get(i).favorited);
//        }

    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}