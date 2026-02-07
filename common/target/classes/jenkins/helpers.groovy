package jenkins

// --------------------------
// ✅ GLOBAL CONFIG
// --------------------------
user  = "chandug@zeta.tech"
token = "114343a5f3dd43c9af7e87a11a489f987d"
host  = "https://jenkins.internal.mum1-pp.zetaapps.in"

def getLatestBuildFromAPI(String jobName) {
    echo "Fetching latest build for job: ${jobName}"

    try {
        //  GET LATEST BUILD
        def jenkinsUrl = "${host}/job/${jobName}/lastSuccessfulBuild/api/json"
        def output = sh(script: """curl -sk --user ${user}:${token} ${jenkinsUrl}""", returnStdout: true).trim()
        def json = readJSON text: output
        def jobId = json.number
        echo "Latest Build ID for ${jobName}: ${jobId}"

        // Download Allure results
        def path = "past"
        sh "mkdir -p ${path}"

        def jenkinsArtifactUrl = "${host}/job/${jobName}/${jobId}/artifact/allure-results/*zip*/allure_archive.zip"
        def allureFile = "${path}/allure_archive.zip"

        sh """curl -sk --user ${user}:${token} --output ${allureFile} ${jenkinsArtifactUrl}"""
        sh "unzip -o ${allureFile} -d ${path}"
        sh "ls -la ${path}"

    } catch (Exception e) {
        echo "Failed to fetch or parse JSON response: ${e.getMessage()}"
    }
}

def updateAppProperties(params, module = "common") {
    def filePath = "${module}/src/main/resources/environments/application.properties"

    def props = fileExists(filePath) ? readProperties(file: filePath) : [:]

    // Update properties
    props['environment']       = params.Environment
    props['tenantId']          = params.IFI
    props['coa']               = params.COA
    props['billing.country']   = params.BILLING_COUNTRY ?: "IN"
    props['groups']            = params.GROUPS ?: "ALL"
    props['test.data.file.path'] = params.TEST_DATA_FILE_PATH

    // Write back to file
    writeFile file: filePath, text: props.collect { k, v -> "$k=$v" }.join("\n")
    println "Updated properties in ${filePath}"
}



static def buildTestCommand(String workspace, String moduleName, params) {
    def cmd = "MAVEN_OPTS=\"-Xmx4096m\" mvn -B clean test " +
            "-Dmaven.repo.local=/home/jenkins/.m2/repository " +
            "-f ${workspace}/pom.xml " +
            "-pl ${moduleName} -am " +
            "-s ${workspace}/common/src/main/resources/jenkins/settings.xml " +
            "-DsuiteXmlFile=${params.testNGxmlName}"

    return cmd
}

static def buildTestCommandWithTestSkip(String workspace, String moduleName, params, boolean skipTests) {
    def baseCmd = buildTestCommand(workspace, moduleName, params)
    baseCmd += " -DskipTests=${skipTests} "
    return baseCmd
}

static def buildTestCommandForUpiToken(String workspace, String moduleName, params) {
    def baseCmd = buildTestCommand(workspace, moduleName, params)

    def extraArgs = [
            "-DPHONE_NUMBER=${params.PHONE_NUMBER}",
            "-DPHONE_NUMBER_EXT=${params.PHONE_NUMBER_EXT}",
            "-DACTION=${params.ACTION}",
            "-DGENERATE_CHALLENGE=${params.GENERATE_CHALLENGE}"
    ].join(' ')

    return "${baseCmd} ${extraArgs}"
}


def sparseCheckout(String branch, String moduleName) {
    def modules = ["common", "shared-module", "qa-reporting-hub", moduleName]
    def sparseFileContent = "*\n" + modules.collect { "${it}/*" }.join("\n")

    sh """
        rm -rf .git
        git init
        git remote add origin git@bitbucket.org:zetaengg/itp-automation.git
        git config core.sparseCheckout true
        echo "${sparseFileContent}" > .git/info/sparse-checkout
        git fetch --depth=1 origin ${branch}
        git checkout FETCH_HEAD
    """
}

def refreshAllureReports(String moduleName, int delaySeconds = 120, int intervalSeconds = 10) {
    echo "Waiting ${delaySeconds}s before starting Allure refresh for ${moduleName}..."
    sleep time: delaySeconds, unit: 'SECONDS'

    while (!fileExists("${WORKSPACE}/tests.done")) {
        echo "Refreshing Allure report for ${moduleName}..."
        try {
            sh "rm -rf ${WORKSPACE}/allure-report/* || true"
            allure includeProperties: false, jdk: '', results: [[path: "${moduleName}/allure-results"]]
        } catch (Exception ex) {
            echo "Allure refresh failed: ${ex.message}"
        }
        sleep time: intervalSeconds, unit: 'SECONDS'
    }

    echo "Detected tests done — stopping Allure refresh."
}

/**
 * ✅ Triggers a post-build job asynchronously if provided.
 *
 * @param postBuildJobName  Name of the job to trigger after build.
 */
def triggerPostBuildJob(String postBuildJobName) {
    if (postBuildJobName?.trim()) {
        echo "✅ Triggering post-build job: ${postBuildJobName}"
        build job: postBuildJobName, wait: false
    } else {
        echo "ℹ️ No post-build job provided — skipping trigger."
    }
}

/**
 * ✅ Marks the current build to keep logs forever if the pipeline parameter
 *    KEEP_BUILD_FOREVER is set to 'true'. Uses Jenkins API to toggle keepLog.
 *
 * @param pipeline     The full Jenkins pipeline script context (this),
 *                     giving access to params, env, currentBuild, workspace, etc.
 * @param jobName      The name of the Jenkins job
 * @param buildNumber  The build number of the Jenkins job
 */
def KeepBuildForever(pipeline, String jobName, String buildNumber) {
    try {
        def toggleUrl = "${host}/job/${jobName}/${buildNumber}/toggleLogKeep"
        def cmd = """curl -s -X POST '${toggleUrl}' --user ${user}:${token}"""
        pipeline.echo "✅ Marking build #${buildNumber} to keep forever"
        pipeline.sh(script: cmd, returnStdout: true).trim()
    } catch (Exception e) {
        pipeline.echo "⚠️ Failed to mark build as keep forever: ${e.getMessage()}"
    }
}

/**
 * ✅ Executes common post-build actions, including:
 *    1️⃣ Marking the build to keep forever if the KEEP_BUILD_FOREVER parameter is true
 *    2️⃣ Triggering post-build job(s) if the build is successful
 *
 * @param pipeline  The full Jenkins pipeline script context (this),
 *                  giving access to params, env, currentBuild, workspace, etc.
 */
def postBuildActions(pipeline) {
    try {
        def params = pipeline.params
        def jobName = pipeline.env.JOB_NAME
        def buildNumber = pipeline.env.BUILD_NUMBER
        def buildResult = pipeline.currentBuild.result ?: 'SUCCESS'

        if (params.KEEP_BUILD) {
            def keepBuild = params.KEEP_BUILD.toString().trim()

            // If param is "true" → always keep; else treat as target state
            if (keepBuild.equalsIgnoreCase('true') || buildResult.equalsIgnoreCase(keepBuild)) {
                KeepBuildForever(pipeline, jobName, buildNumber)
            }
        }

        // ✅ 2️⃣ Trigger post-build job if provided and build succeeded
        if (pipeline.currentBuild.result != 'ABORTED' && pipeline.currentBuild.result != 'FAILURE') {
            def postBuildJobName = params.POST_BUILD_TRIGGER_JOB?.trim()
            if (postBuildJobName) {
                pipeline.echo "✅ Post-build job parameter found: ${postBuildJobName}"
                triggerPostBuildJob(postBuildJobName)
            } else {
                pipeline.echo "ℹ️ No post-build job specified in parameters — skipping trigger."
            }
        }

    } catch (Exception e) {
        pipeline.echo "Post-build actions failed: ${e.getMessage()}"
    }
}

/**
 * ✅ Sends an HTML email summary using Jenkins' Email Extension plugin.
 *
 * @param pipeline  The Jenkins pipeline context (this)
 * @param htmlPath  Path to the HTML file to send (e.g. "${WORKSPACE}/email_summary_report.html")
 * @param recipients Comma-separated list of recipients
 */
def sendHtmlEmail(pipeline, String htmlPath, String saas, String recipients) {
    try {
        pipeline.echo "📧 Preparing to send HTML email from ${htmlPath}"

        if (!pipeline.fileExists(htmlPath)) {
            pipeline.echo "⚠️ HTML file not found: ${htmlPath}"
            return
        }

        def htmlContent = pipeline.readFile(htmlPath)
        def currentDate = new Date().format('dd-MMM-yyyy')

        pipeline.emailext(
                from: 'cicdnotifications@zeta.tech',
                subject: "📊 ${saas} Automation Test Execution Summary - (${currentDate})",
                body: htmlContent,
                mimeType: 'text/html',
                to: recipients
        )

        pipeline.echo "✅ Email successfully sent to ${recipients}"

    } catch (Exception e) {
        pipeline.echo "❌ Failed to send email: ${e.getMessage()}"
    }
}

return this