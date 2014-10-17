package com.brew.brewshop.storage;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.brew.brewshop.R;
import com.brew.brewshop.storage.recipes.Recipe;
import com.brew.brewshop.storage.style.StyleInfoList;
import com.brew.brewshop.storage.style.StyleStorage;
import com.brew.brewshop.util.Util;

import java.util.List;

public class RecipeListAdapter extends BaseAdapter {
    private Context mContext;
    SparseBooleanArray mSparseBooleanArray;
    ListView mView;
    BrewStorage mStorage;

    public RecipeListAdapter(Context context, ListView view) {
        mContext = context;
        mStorage = new BrewStorage(context);
        mSparseBooleanArray = new SparseBooleanArray();
        mView = view;
    }

    @Override
    public int getCount() {
        return mStorage.retrieveRecipes().size();
    }

    @Override
    public Object getItem(int position) {
        return mStorage.retrieveRecipes().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_recipe, parent, false);
        }

        View container = rowView.findViewById(R.id.item_container);

        if (mView.isItemChecked(position)) {
            container.setBackgroundResource(R.color.color_accent);
        } else {
            container.setBackgroundResource(R.drawable.touchable);
        }

        ImageView iconView = (ImageView) rowView.findViewById(R.id.recipe_icon);
        TextView nameView = (TextView) rowView.findViewById(R.id.recipe_name);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.recipe_style);

        StyleInfoList styleInfoList = new StyleStorage(mContext).getStyles();

        Recipe recipe = mStorage.retrieveRecipes().get(position);
        iconView.setBackgroundColor(Util.getColor(recipe.getSrm()));
        nameView.setText(recipe.getName());
        descriptionView.setText(styleInfoList.findById(recipe.getStyle().getId()).getName());
        return rowView;
    }

    public int deleteSelected() {
        int deleted = 0;
        List<Recipe> recipes = getRecipes();
        for (int i = recipes.size() - 1; i >= 0; i--) {
            if (mView.isItemChecked(i)) {
                mStorage.deleteRecipe(recipes.get(i));
                deleted++;
            }
        }
        return deleted;
    }

    private List<Recipe> getRecipes() {
        return mStorage.retrieveRecipes();
    }
}