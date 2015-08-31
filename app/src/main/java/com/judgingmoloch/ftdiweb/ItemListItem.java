package com.judgingmoloch.ftdiweb;

import com.judgingmoloch.ftdiweb.connection.Instructions;

public class ItemListItem {
    private String itemTitle, itemValue;
    public Instructions.InstructionOverview itemOverview;

    public String getTitle() {
        return (itemTitle == null) ? itemOverview.name : itemTitle;
    }
    public String getValue() {
        return (itemValue == null) ? itemOverview.description : itemValue;
    }
    public void setTitle(Object title) {
        itemTitle = String.valueOf(title);
    }
    public void setValue(Object value) {
        itemValue = String.valueOf(value);
    }

    public ItemListItem(Instructions.InstructionOverview overview) {
        itemOverview = overview;
    }

    public ItemListItem(String title, Object value) {
        itemTitle = title;
        itemValue = String.valueOf(value);
    }
}
