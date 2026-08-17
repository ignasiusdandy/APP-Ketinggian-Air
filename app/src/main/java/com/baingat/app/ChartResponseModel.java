package com.baingat.app;

import java.util.List;

public class ChartResponseModel {
    private boolean status;
    private List<ChartItem> data;

    public boolean isStatus() {
        return status;
    }

    public List<ChartItem> getData() {
        return data;
    }
}
