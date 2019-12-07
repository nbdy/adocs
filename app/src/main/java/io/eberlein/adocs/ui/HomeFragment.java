package io.eberlein.adocs.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FragmentUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eberlein.adocs.R;
import io.eberlein.adocs.objects.Documentation;

// todo cancel download

public class HomeFragment extends Fragment {
    private Fragment thisRef;
    private BaseDownloadTask dlTask;
    private Documentation documentation;

    @BindView(R.id.btn_download) Button btn_download;
    @BindView(R.id.pb_download) ProgressBar pb_download;
    @BindView(R.id.tv_speed) TextView tv_speed;
    @BindView(R.id.recycler) RecyclerView recycler;

    class DownloadListener extends FileDownloadSampleListener {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            pb_download.setVisibility(View.GONE);
            tv_speed.setVisibility(View.GONE);
            Toast.makeText(getContext(), "decompressing archive", Toast.LENGTH_SHORT).show();
            documentation.decompress();
            initRecycler();
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            e.printStackTrace();
            pb_download.setVisibility(View.GONE);
            tv_speed.setVisibility(View.GONE);
            Toast.makeText(getContext(), "could not download archive", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            pb_download.setVisibility(View.VISIBLE);
            tv_speed.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "started download", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            if(totalBytes == -1) pb_download.setIndeterminate(true);
            else {
                pb_download.setMax(totalBytes);
                pb_download.setProgress(soFarBytes);
            }
            tv_speed.setText(String.format(Locale.getDefault(), "%dkB", task.getSpeed()));
        }
    }

    @OnClick(R.id.btn_download)
    void btnDownloadClicked(){
        dlTask = FileDownloader.getImpl()
                .create(documentation.getUrl())
                .setPath(documentation.getZipFile().getAbsolutePath())
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(420)
                .setTag(documentation.getName())
                .setListener(new DownloadListener());
        dlTask.start();
    }

    public HomeFragment(){
        thisRef = this;
    }

    class IViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name) TextView tv_name;

        @OnClick
        void onClick(){
            FragmentUtils.replace(thisRef, new ViewDocFragment(documentation, tv_name.getText().toString()));
        }

        IViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    private void initRecycler(){
        List<String> items = documentation.getItems();
        recycler.setVisibility(View.VISIBLE);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new RecyclerView.Adapter<IViewHolder>() {
            @NonNull
            @Override
            public IViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new IViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_recycler, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull IViewHolder holder, int position) {
                holder.tv_name.setText(items.get(position));
            }

            @Override
            public int getItemCount() {
                return items.size();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, v);
        documentation = new Documentation(getString(R.string.url));
        if(documentation.getDirectory().exists()) {
            btn_download.setVisibility(View.GONE);
            initRecycler();
        }
        else recycler.setVisibility(View.GONE);
        return v;
    }
}
