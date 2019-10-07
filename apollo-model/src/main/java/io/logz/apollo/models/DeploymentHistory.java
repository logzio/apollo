package io.logz.apollo.models;

import java.util.List;

public class DeploymentHistory {
    public int recordsTotal;
    public int recordsFiltered;
    public int pageNumber;
    public int pageSize;
    public List<DeploymentHistoryDetails> data;

    public DeploymentHistory() {
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<DeploymentHistoryDetails> getData() {
        return data;
    }

    public void setData(List<DeploymentHistoryDetails> data) {
        this.data = data;
    }
}