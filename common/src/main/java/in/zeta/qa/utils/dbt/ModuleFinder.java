package in.zeta.qa.utils.dbt;

import java.util.Arrays;
import java.util.List;

public class ModuleFinder {
    private static final List<String> MODULES = Arrays.asList(
            "edw_account_monthly_inc", "edw_account_inc", "edw_authorization_inc", "edw_card_inc",
            "edw_customer", "financial_activity_inc", "header", "metro2_report_data",
            "billing_details_inc", "edw_statement_inc", "edw_transaction_inc", "edw_account",
            "edw_authorization", "edw_card", "billing_details", "edw_statement", "edw_transaction",
            "billing_details_inc_standard", "statement_interest_breakup_inc_standard",
            "edw_statement_inc_standard", "edw_transaction_inc_standard", "accounts_as_of_date_daily_v2",
            "accounts_as_of_date_monthly_v2", "statement_interest_breakup_standard",
            "customer_as_of_date_standard","dispute_ageing_report_tachyon","dispute_report_tachyon",
            "dispute_ageing_report_grouping","statement_interest_breakup_inc","update_account_tags_inc",
            "delinquent_reage_report", "accounts_as_of_date"
    );

    public static String findModuleInLine(String line) {
        return MODULES.stream().filter(line::contains).findFirst().orElse(null);
    }
}

