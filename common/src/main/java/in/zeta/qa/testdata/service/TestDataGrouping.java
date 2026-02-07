package in.zeta.qa.testdata.service;

import in.zeta.qa.testdata.entity.TransactionTestData;
import in.zeta.qa.utils.fileUtils.PropertyFileReader;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TestDataGrouping {

    @SneakyThrows
    static Predicate<TransactionTestData> filterByActiveTestAndTestGroup() {
        Predicate<TransactionTestData> shouldRun = getRunPredicate();
        Predicate<TransactionTestData> applicableTestGroup = getApplicableTestGroupPredicate();
        Predicate<TransactionTestData> applicableFeatureGroup = getApplicableFeatureGroupPredicate();
        return shouldRun.and(applicableTestGroup).and(applicableFeatureGroup);
    }


    //##################################################################################################################
    //############################################ Common predicate methods ############################################
    //##################################################################################################################
    private static Predicate<TransactionTestData> getRunPredicate() {
        return data -> Objects.nonNull(data.getRun()) && data.getRun();
    }


    @SneakyThrows
    private static Predicate<TransactionTestData> getApplicableTestGroupPredicate() {
        String value = StringUtils.isEmpty(PropertyFileReader.getPropertyValue("groups")) ? "REGRESSION" : PropertyFileReader.getPropertyValue("groups");
        List<String> groups = Arrays.stream(value.split(",")).map(String::trim).map(String::toUpperCase).toList();
        return data -> groups.contains("REGRESSION") || (Objects.nonNull(data.getTestGroup()) && data.getTestGroup().stream().map(String::toUpperCase).anyMatch(groups::contains));
    }

    @SneakyThrows
    private static Predicate<TransactionTestData> getApplicableFeatureGroupPredicate() {
        String value = StringUtils.isEmpty(PropertyFileReader.getPropertyValue("features")) ? "ALL" : PropertyFileReader.getPropertyValue("features");
        List<String> features = Arrays.stream(value.split(",")).map(String::trim).map(String::toUpperCase).toList();
        return data -> features.contains("ALL") || (Objects.nonNull(data.getTestGroup()) && data.getTestGroup().stream().map(String::toUpperCase).anyMatch(features::contains));
    }


}
