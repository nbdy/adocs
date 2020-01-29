package io.eberlein.adocs.objects;

import java.io.File;

public class Item {
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
