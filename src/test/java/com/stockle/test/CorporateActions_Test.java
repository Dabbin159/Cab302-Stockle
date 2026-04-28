package com.stockle.test;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.CorporateAction;
import com.stockle.api.service.CorporateActionsService;

public class CorporateActions_Test {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private CorporateActionsService corporateActionsService;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        corporateActionsService = new CorporateActionsService(apiClient, objectMapper);
    }

    @Test
    public void testGetCorporateActions() {
        System.out.println("--- CorporateActions Test: getCorporateActions(10, \"asc\") ---");
        List<CorporateAction> actions = corporateActionsService.getCorporateActions(10, "asc");

        if (actions == null || actions.isEmpty()) {
            System.out.println("No corporate actions returned");
        } else {
            System.out.println("Fetched " + actions.size() + " corporate actions (showing up to 5):");
            for (int i = 0; i < Math.min(5, actions.size()); i++) {
                CorporateAction action = actions.get(i);
                System.out.println("  " + (i + 1) + ". "
                        + (action.symbol != null ? action.symbol : "N/A")
                        + " | type=" + (action.type != null ? action.type : action.corporate_action_type)
                        + " | ex_date=" + action.ex_date
                        + " | process_date=" + action.process_date);
            }
        }
        System.out.println();
    }

    @Test
    public void testGetCorporateActionsBySymbol() {
        System.out.println("--- CorporateActions Test: getCorporateActionsBySymbol(\"AAPL\", 10, \"asc\") ---");
        List<CorporateAction> actions = corporateActionsService.getCorporateActionsBySymbol("AAPL", 10, "asc");

        if (actions == null || actions.isEmpty()) {
            System.out.println("No corporate actions returned for AAPL");
        } else {
            System.out.println("Fetched " + actions.size() + " corporate actions for AAPL (showing up to 5):");
            for (int i = 0; i < Math.min(5, actions.size()); i++) {
                CorporateAction action = actions.get(i);
                System.out.println("  " + (i + 1) + ". "
                        + (action.symbol != null ? action.symbol : "N/A")
                        + " | type=" + (action.type != null ? action.type : action.corporate_action_type)
                        + " | ex_date=" + action.ex_date
                        + " | process_date=" + action.process_date);
            }
        }
        System.out.println();
    }
}
