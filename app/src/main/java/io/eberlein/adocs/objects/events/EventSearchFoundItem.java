package io.eberlein.adocs.objects.events;

import io.eberlein.adocs.objects.Item;

public class EventSearchFoundItem {
    private Item item;

    public EventSearchFoundItem(Item item){
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
