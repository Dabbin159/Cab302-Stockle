package com.stockle.api.data;

import java.util.Map;

/**
 * Data model for Alpaca corporate action records.
 *
 * Fields are intentionally aligned to common API keys and may be null depending on action type.
 */
public class CorporateAction {
    public String id;
    public String symbol;
    public String type;
    public String corporate_action_type;

    public String process_date;
    public String ex_date;
    public String record_date;
    public String payable_date;
    public String declared_date;

    public String old_symbol;
    public String new_symbol;

    public Double old_rate;
    public Double new_rate;

    public Map<String, Object> raw;

    public CorporateAction() {
    }
}
