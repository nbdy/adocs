package io.eberlein.adocs.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SDCardUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.adocs.R;
import io.eberlein.adocs.Static;
import io.eberlein.adocs.objects.Item;
import io.eberlein.adocs.objects.events.EventSearchFoundItem;
import io.eberlein.adocs.objects.events.EventSearchStarted;
import io.eberlein.adocs.objects.events.EventSearchStopped;

public class SearchFragment extends Fragment {
    @BindView(R.id.et_search) EditText search;
    @BindView(R.id.rv_results) RecyclerView recyclerView;
    @BindView(R.id.btn_search) Button btnSearch;

    private List<Item> searchResults;
    private SearchTask searchTask;

    private static class SearchTask extends AsyncTask<Void, Void, Void> {
        private List<Item> results = new ArrayList<>();
        private String searchWord;
        private File baseDirectory;
        private boolean running = false;
        private boolean notifyOnNewItem = false;

        SearchTask(String searchWord, File baseDirectory, boolean notifyOnNewItem){
            this.searchWord = searchWord;
            this.baseDirectory = baseDirectory;
            this.notifyOnNewItem = notifyOnNewItem;
        }

        private List<Item> findString(String contains, File directory, List<Item> r){
            if(!directory.isDirectory()) return null;
            for(File f : directory.listFiles()){
                if(f.isDirectory()) findString(contains, f, r);
                else if(f.isFile() && fileContains(f, contains)) {
                    Item i = new Item(f);
                    if(notifyOnNewItem) EventBus.getDefault().post(new EventSearchFoundItem(i));
                    r.add(i);
                }
            }
            return r;
        }

        private boolean fileContains(File target, String contains){
            if(!target.getName().endsWith(".html")) return false;
            return FileIOUtils.readFile2String(target).contains(contains);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            running = true;
            EventBus.getDefault().post(new EventSearchStarted());
            results = findString(searchWord, baseDirectory, results);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            EventBus.getDefault().post(new EventSearchStopped());
            running = false;
        }

        @Override
        protected void onCancelled() {
            running = false;
            EventBus.getDefault().post(new EventSearchStopped());
        }

        public List<Item> getResults() {
            return results;
        }

        boolean isRunning() {
            return running;
        }

        public boolean isNotifyOnNewItem() {
            return notifyOnNewItem;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSearchStarted(EventSearchStarted e){
        btnSearch.setText(R.string.cancel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSearchStopped(EventSearchStopped e){
        if(!searchTask.isNotifyOnNewItem()) searchResults.addAll(searchTask.getResults());
        btnSearch.setText(R.string.search);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSearchFoundItem(EventSearchFoundItem e){
        searchResults.add(e.getItem());
    }

    @OnClick(R.id.btn_search)
    void btnSearchClicked(){
        if(searchTask.isRunning()){
            searchTask.cancel(true);
        } else {
            searchResults.clear();
            searchTask = new SearchTask(search.getText().toString(), new File(SDCardUtils.getSDCardPathByEnvironment() + Static.BASE_DIRECTORY), true); // todo set by sharedprefs
            searchTask.execute();
        }
    }

    public SearchFragment(){
        searchResults = new ArrayList<>();
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
        getActivity().setTitle(R.string.search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
        recyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
