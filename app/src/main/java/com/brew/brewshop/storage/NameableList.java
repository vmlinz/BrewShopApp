package com.brew.brewshop.storage;

import com.brew.brewshop.storage.Nameable;

import java.util.ArrayList;

public class NameableList<T extends Nameable> extends ArrayList<T> {

    public T findById(int id) {
        for(T info : this) {
            if (info.getId() == id) {
                return info;
            }
        }
        return null;
    }
}