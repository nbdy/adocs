package io.eberlein.adocs.objects;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.eberlein.adocs.Static;

public class Documentation {
    private String key;
    private String url;
    private String name;
    private File directory;
    private File zipFile;

    public Documentation(String url){
        key = UUID.randomUUID().toString();
        this.url = url;
        String[] su = url.split("/");
        name = su[su.length - 1].split("-")[1].replace(".zip", "");
        directory = new File(SDCardUtils.getSDCardPathByEnvironment() + Static.BASE_DIRECTORY + "/" + name);
        zipFile = new File(directory.getAbsolutePath() + "/" + name + ".zip");
        Log.d("Documentation", name);
        Log.d("Documentation", url);
        Log.d("Documentation", directory.getAbsolutePath());
    }

    public void decompress(){
        try {
            ZipUtils.unzipFile(zipFile, directory);
            FileUtils.delete(zipFile);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public File getZipFile() {
        return zipFile;
    }

    public boolean delete(){
        return directory.delete();
    }

    public Documentation(File directory){
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public File getDirectory() {
        return directory;
    }

    public String getIndex(){
        return "file://" + directory.getAbsolutePath() + "/docs/index.html";
    }

    public String getGuide(){
        return "file://" + directory.getAbsolutePath() + "/docs/guide/index.html";
    }

    public String getSub(String sub){
        return "file://" + directory.getAbsolutePath() + "/docs/" + sub + "/index.html";
    }

    public List<String> getItems(){
        List<String> items = new ArrayList<>();
        File docs = new File(directory.getAbsolutePath() + "/docs/");
        for(File d : docs.listFiles()){
            if(d.isDirectory()){
                for(File f : d.listFiles()){
                    if(f.isFile() && f.getName().equals("index.html")) items.add(d.getName());
                }
            }
        }
        return items;
    }
}
