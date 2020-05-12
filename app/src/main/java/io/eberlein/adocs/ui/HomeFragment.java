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
    private boolean isDownloading = false;
    private BaseDownloadTask dlTask;
    private Documentation documentation;

    @BindView(R.id.btn_download) Button btn_download;
    @BindView(R.id.pb_download) ProgressBar pb_download;
    @BindView(R.id.tv_speed) TextView tv_speed;
    @BindView(R.id.tv_min) TextView tv_min;
    @BindView(R.id.tv_max) TextView tv_max;
    @BindView(R.id.recycler) RecyclerView recycler;

    private void hideDL(){
        pb_download.setVisibility(View.GONE);
        tv_speed.setVisibility(View.GONE);
        tv_min.setVisibility(View.GONE);
        tv_max.setVisibility(View.GONE);
    }

    private void showDL(){
        pb_download.setVisibility(View.VISIBLE);
        tv_speed.setVisibility(View.VISIBLE);
        tv_min.setVisibility(View.VISIBLE);
        tv_max.setVisibility(View.VISIBLE);
    }

    class DownloadListener extends FileDownloadSampleListener {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            btn_download.setVisibility(View.GONE);
            hideDL();
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
            hideDL();
            Toast.makeText(getContext(), "could not download archive", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            showDL();
            Toast.makeText(getContext(), "started download", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            if(totalBytes == -1) {
                pb_download.setIndeterminate(true);
                tv_max.setText(R.string.unknown);
            }
            else {
                tv_max.setText(String.format(Locale.getDefault(), "%dMB", totalBytes / 1000000));
                pb_download.setMax(totalBytes);
                pb_download.setProgress(soFarBytes);
                tv_min.setText(String.format(Locale.getDefault(), "%dMB", soFarBytes / 1000000));
            }
            tv_speed.setText(String.format(Locale.getDefault(), "%dkB", task.getSpeed()));
        }
    }

    @OnClick(R.id.btn_download)
    void btnDownloadClicked(){
        if(!isDownloading) {
            if(dlTask == null) {
                dlTask = FileDownloader.getImpl()
                        .create(documentation.getUrl())
                        .setPath(documentation.getZipFile().getAbsolutePath())
                        .setCallbackProgressTimes(300)
                        .setMinIntervalUpdateSpeed(420)
                        .setTag(documentation.getName())
                        .setListener(new DownloadListener());
            }
            dlTask.start();
            isDownloading = true;
            btn_download.setText(getString(R.string.cancel));
        } else {
            dlTask.pause();
            isDownloading = false;
            btn_download.setText(getText(R.string.resume));
        }
    }

    public HomeFragment(){

    }

    class IViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name) TextView tv_name;

        @OnClick
        void onClick(){
            FragmentUtils.replace(getActivity().getSupportFragmentManager(), new ViewDocFragment(documentation, tv_name.getText().toString()), R.id.nav_host_fragment, true);
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
        getActivity().setTitle(R.string.home);
        documentation = new Documentation(getString(R.string.d24_r01));
        if (documentation.getDirectory().exists()) {
            btn_download.setVisibility(View.GONE);
            initRecycler();
        }
        return v;
    }
}
