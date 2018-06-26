package io.logz.apollo.controllers;

import io.logz.apollo.common.QueryStringParser;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.database.OrderDirection;
import io.logz.apollo.models.DeploymentHistoryDetails;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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

    @LoggedIn
    @GET("/deployment/datatables")
    public DataTablesResponseObject getDataTableDeploymentResponse(Req req) {

        Map<String, String> queryStringMap = QueryStringParser.getQueryStringMap(req.query());

        int draw = Integer.parseInt(queryStringMap.get("draw"));
        int length = Integer.parseInt(queryStringMap.get("length"));
        int start = Integer.parseInt(queryStringMap.get("start"));
        String search = "%" + queryStringMap.get("search[value]") + "%";
        String orderDirectionString = queryStringMap.get("order[0][dir]");


        DataTablesResponseObject dataTablesResponseObject = new DataTablesResponseObject();
        dataTablesResponseObject.draw = draw;
        dataTablesResponseObject.recordsFiltered = deploymentDao.getFilteredDeploymentHistoryCount(search);
        dataTablesResponseObject.recordsTotal = deploymentDao.getTotalDeploymentsCount();

        dataTablesResponseObject.data = deploymentDao.filterDeploymentHistoryDetails(search, OrderDirection.valueOf(orderDirectionString.toUpperCase()), start, length);

        return dataTablesResponseObject;
    }

    // As defined in https://datatables.net/manual/server-side
    private static class DataTablesResponseObject {
        public int draw;
        public int recordsTotal;
        public int recordsFiltered;
        public List<DeploymentHistoryDetails> data;

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

        public List<DeploymentHistoryDetails> getData() {
            return data;
        }

        public void setData(List<DeploymentHistoryDetails> data) {
            this.data = data;
        }
    }
}
