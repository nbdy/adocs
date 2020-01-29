package io.eberlein.adocs.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.GsonUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.adocs.R;
import io.eberlein.adocs.Static;
import io.eberlein.adocs.objects.events.EventViewUrl;

public class FavouritesFragment extends Fragment {
    private static final String TAG = "FavouritesFragment";

    private List<String> favourites = new ArrayList<>();

    @BindView(R.id.rv_fav) RecyclerView recyclerView;

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView name;

        @OnClick
        void onClick(){
            FragmentUtils.replace(getActivity().getSupportFragmentManager(), new ViewDocFragment(name.getText().toString()), R.id.nav_host_fragment, true);
        }

        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favourites.addAll(Static.getFavouriteSP().getAll().keySet());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_favourites, container, false);
        ButterKnife.bind(this, v);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerView.Adapter<ViewHolder>(){
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.name.setText(favourites.get(position));
            }

            @Override
            public int getItemCount() {
                return favourites.size();
            }
        });
        return v;
    }
}
