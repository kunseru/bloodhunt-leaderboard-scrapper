package com.github.kunseru;

import java.util.List;

public class Progress {
    private int currentPage;
    private List<Integer> killsList;

    public Progress(int currentPage, List<Integer> killsList) {
        this.currentPage = currentPage;
        this.killsList = killsList;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public List<Integer> getKillsList() {
        return killsList;
    }

    public void setKillsList(List<Integer> killsList) {
        this.killsList = killsList;
    }
}