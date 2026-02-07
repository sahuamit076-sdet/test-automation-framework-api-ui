package in.zeta.qa.constants.endpoints;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JenkinsEndpoints implements ApiEndpoint {
    BUILD_WITH_PARAM("/job/{PATH}/buildWithParameters?token={TOKEN}"),
    GET_BUILD_INFO("/job/{PATH}/api/json?token={TOKEN}"),
    GET_BUILD_CONSOLE_TEXT("/job/{PATH}/{JOB_ID}/consoleText?token={TOKEN}"),
    GET_BUILDS("/job/{JOB_NAME}/api/json?tree=builds[number,result,timestamp,duration,actions[causes[shortDescription]]]"),
    GET_ALLURE_REPORTS_BY_JOB_ID("/job/{JOB_NAME}/{BUILD_ID}/allure/widgets/summary.json");

    private final String path;

}
