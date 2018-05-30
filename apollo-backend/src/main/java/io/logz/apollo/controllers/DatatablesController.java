package io.logz.apollo.controllers;

import com.google.common.collect.ImmutableMap;
import io.logz.apollo.dao.DeploymentDao;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.http.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Controller
public class DatatablesController {

    private static final Logger logger = LoggerFactory.getLogger(DatatablesController.class);

    private final DeploymentDao deploymentDao;

    @Inject
    public DatatablesController(DeploymentDao deploymentDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
    }

    @GET("/deployment/datatables")
    public DataTablesResponseObject getDataTableDeploymentResponse(Req req) {

        System.out.println(req.query());
        DataTablesResponseObject dataTablesResponseObject = new DataTablesResponseObject();
        dataTablesResponseObject.draw = 1;
        dataTablesResponseObject.recordsFiltered = 1;
        dataTablesResponseObject.recordsTotal = 1;

        List<Map<String, Object>> data = new ArrayList<>();

        ImmutableMap<String, Object> map = ImmutableMap.<String,Object> builder()
                .put("id", 1)
                .put("lastUpdated", "2018-04-24 09:54:32")
                .put("serviceId", 1)
                .put("environmentId", 1)
                .put("groupName", "bla")
                .put("userName", "roi@logz")
                .put("status", "DONE")
                .build();

        data.add(map);

        dataTablesResponseObject.data = data;

        return dataTablesResponseObject;
    }

    // As defined in https://datatables.net/manual/server-side
    private static class DataTablesResponseObject {
        public int draw;
        public int recordsTotal;
        public int recordsFiltered;
        public List<Map<String, Object>> data;

        public DataTablesResponseObject() {
        }

        public int getDraw() {
            return draw;
        }

        public void setDraw(int draw) {
            this.draw = draw;
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

        public List<Map<String, Object>> getData() {
            return data;
        }

        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }
    }
}
