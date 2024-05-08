package com.picobase.persistence.repository;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class Page<T> implements Serializable {
    private  int page;
    private  int perPage;
    private  int totalItems;
    private  int totalPages;
    private  List<T> items = new ArrayList<>();

    public Page(){

    }
    public Page(int page, int perPage, int totalItems, int totalPages, List<T> items) {
        this.page = page;
        this.perPage = perPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
