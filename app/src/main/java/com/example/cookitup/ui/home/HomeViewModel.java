package com.example.cookitup.ui.home;

import android.os.Parcelable;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cookitup.model.Meal;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    public final ArrayList<String> ingredients = new ArrayList<>();
    public final MutableLiveData<List<Meal>> searchResults = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);

    // Preserve RecyclerView scroll position across config changes (dark mode switch)
    public Parcelable recyclerViewState;
}
