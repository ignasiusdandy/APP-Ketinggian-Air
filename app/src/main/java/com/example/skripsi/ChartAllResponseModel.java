package com.example.skripsi;

import java.util.List;

public class ChartAllResponseModel {
    private boolean status;
    private DataAllChart data;

    public DataAllChart getDataChartAll(){
        return data;
    }

    public static class DataAllChart {
        private List<ChartItem> jalandatang;
        private List<ChartItem> jalanpulang;

        public List<ChartItem> getJalandatang(){
            return jalandatang;
        }

        public List<ChartItem> getJalanpulang(){
            return jalanpulang;
        }
    }
}
