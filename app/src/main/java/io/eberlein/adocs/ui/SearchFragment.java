package io.eberlein.adocs.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SDCardUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.adocs.R;
import io.eberlein.adocs.Static;

public class SearchFragment extends Fragment {
    @BindView(R.id.et_search) EditText search;
    @BindView(R.id.recycler) RecyclerView recycler;

    private List<Item> searchResults;

    private static boolean fileContains(File target, String contains){
        return FileIOUtils.readFile2String(target).contains(contains);
    }

    private List<Item> findString(String contains, File directory, List<Item> r){
        if(!directory.isDirectory()) return null;
        for(File f : directory.listFiles()){
            if(f.isDirectory()) findString(contains, f, r);
            else if(f.isFile() && fileContains(f, contains)) r.add(new Item(f));
        }
        return r;
    }

    @OnClick(R.id.btn_search)
    void btnSearchClicked(){
        searchResults.clear();
        searchResults = findString(search.getText().toString(), new File(SDCardUtils.getSDCardPathByEnvironment() + Static.BASE_DIRECTORY), searchResults);
    }

    public SearchFragment(){
        searchResults = new ArrayList<>();
    }

    private class Item {
        private File file;
        private String name;
        private String found;

        public File getFile() {
            return file;
        }

        public String getName() {
            return name;
        }

        public String getFound() {
            return found;
        }

        public Item(File file){
            this.file = file;
            name = file.getName();
            found = ""; // todo wrapping words
        }
    }

    class IViewHolder extends RecyclerView.ViewHolder {
        private Item item;

        IViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }

        void setItem(Item item) {
            this.item = item;

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, v);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.Adapter<IViewHolder> adapter = new RecyclerView.Adapter<IViewHolder>() {
            @NonNull
            @Override
            public IViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new IViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_recycler, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull IViewHolder holder, int position) {
                holder.setItem(searchResults.get(position));
            }

            @Override
            public int getItemCount() {
                return searchResults.size();
            }
        };
        recycler.setAdapter(adapter);
        return v;
    }
}
