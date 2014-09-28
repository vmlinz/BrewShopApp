package com.brew.brewshop.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.brew.brewshop.IRecipeManager;
import com.brew.brewshop.R;
import com.brew.brewshop.storage.RecipeListAdapter;
import com.brew.brewshop.storage.RecipeStorage;

public class RecipeListFragment extends Fragment implements AdapterView.OnItemClickListener, ListView.MultiChoiceModeListener, ListView.OnItemLongClickListener {
    private static final String TAG = RecipeListFragment.class.getName();
    private static final String ACTION_MODE = "ActionMode";
    private static final String SELECTED_INDEXES = "Selected";

    private RecipeStorage mRecipeStorage;
    private IRecipeManager mRecipeManager;
    private ActionMode mActionMode;
    private View mMessageView;

    private ListView mRecipeList;
    private RecipeListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipes, container, false);

        mRecipeList = (ListView) rootView.findViewById(R.id.recipes_list);
        mRecipeList.setOnItemClickListener(this);
        mRecipeList.setMultiChoiceModeListener(this);
        mRecipeList.setOnItemLongClickListener(this);

        mMessageView = rootView.findViewById(R.id.message_layout);

        mRecipeStorage = new RecipeStorage();
        Context context = getActivity().getApplicationContext();
        mAdapter = new RecipeListAdapter(context, mRecipeStorage.getRecipes(), mRecipeList);
        mRecipeList.setAdapter(mAdapter);

        checkEmpty();
        checkResumeActionMode(savedInstanceState);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(ACTION_MODE, mActionMode != null);
        bundle.putIntArray(SELECTED_INDEXES, getSelectedIndexes());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRecipeManager = (IRecipeManager) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + IRecipeManager.class.getName());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mActionMode != null) {
            setSelected(i, mRecipeList.isItemChecked(i));
            updateActionBar();
        } else {
            if (mRecipeManager != null) {
                mRecipeManager.OnCreateNewRecipe();
            } else {
                Log.d(TAG, "Recipe manager is not set");
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode != null) {
            updateActionBar();
            return false;
        } else {
            startActionMode(new int[] {position});
        }
        return true;
    }

    private int[] getSelectedIndexes() {
        int[] indexes = new int[mRecipeList.getCheckedItemCount()];
        int indexOffset = 0;
        for (int i = 0; i < mRecipeList.getCount(); i++) {
            if (mRecipeList.isItemChecked(i)) {
                indexes[indexOffset] = i;
                indexOffset++;
            }
        }
        return indexes;
    }

    private void checkResumeActionMode(Bundle bundle) {
        if (bundle != null) {
            if (bundle.getBoolean(ACTION_MODE)) {
                int[] selected = bundle.getIntArray(SELECTED_INDEXES);
                startActionMode(selected);
            }
        }
    }

    private void startActionMode(int[] selectedIndexes) {
        mRecipeList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        for (int i : selectedIndexes) {
            setSelected(i, true);
        }
        getActivity().startActionMode(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.recipes_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_new_recipe) {
            mRecipeManager.OnCreateNewRecipe();
            return true;
        }
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        Log.d(TAG, "onItemCheckedStateChanged");
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mActionMode = actionMode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        menu.clear();
        int checked = mRecipeList.getCheckedItemCount();
        mActionMode.setTitle(getResources().getString(R.string.select_recipes));
        mActionMode.setSubtitle(checked + " " + getResources().getString(R.string.selected));

        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        boolean itemsChecked = (mRecipeList.getCheckedItemCount() > 0);
        mActionMode.getMenu().findItem(R.id.action_delete).setVisible(itemsChecked);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_select_all:
                if (areAllSelected()) {
                    setAllSelected(false);
                } else {
                    setAllSelected(true);
                }
                updateActionBar();
                return true;
            case R.id.action_delete:
                int deleted = mAdapter.deleteSelected();
                actionMode.finish();
                checkEmpty();
                toastDeleted(deleted);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        setAllSelected(false);
        mRecipeList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        mActionMode = null;
    }

    private boolean areAllSelected() {
        if (mRecipeList.getCount() == mRecipeList.getCheckedItemCount()) {
            return true;
        }
        return false;
    }

    private void setAllSelected(boolean selected) {
        for (int i = 0; i < mRecipeList.getCount(); i++) {
            setSelected(i, selected);
        }
    }

    private void setSelected(int position, boolean selected) {
        mRecipeList.setItemChecked(position, selected);
    }

    private void updateActionBar() {
        if (mActionMode != null) {
            mActionMode.invalidate();
        }
    }

    private void checkEmpty() {
        if (mAdapter.getCount() == 0) {
            mMessageView.setVisibility(View.VISIBLE);
        } else {
            mMessageView.setVisibility(View.GONE);
        }
    }

    private void toastDeleted(int deleted) {
        Context context = getActivity();
        String message;
        if (deleted > 1) {
            message = String.format(context.getResources().getString(R.string.deleted_recipes), deleted);
        } else {
            message = context.getResources().getString(R.string.deleted_recipe);
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}