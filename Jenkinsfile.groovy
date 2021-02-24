pipeline {
  // Run on a build agent where we have an Android SDK installed
  agent { label 'android' }
  
  // Environment Variables
  environment {
    JAVA_HOME = 'C:/Java/jdk-11.0.2'
    ANDROID_SDK_ROOT = 'C:/android-sdk'
    LAST_COMMIT = ''
  }
  
  options {
    // Stop the build early in the case of compile/test failures
    skipStagesAfterUnstable()
    // Force parallel stages to all be aborted when any of them fails
    parallelAlwaysFailFast()
  }
  
  stages {
    stage('Prepare') {
      steps {
        script {
          def lastCommit = getLastCommit()
          LAST_COMMIT = "${lastComit}"
        }
        echo 'Printing environments'
        bat 'gradlew clean'
      }
      post {
        always {
          updatedGitlabCommitStatus name: 'jenkins-clean', state: 'running'
        }
      }
    }
    stage('Compile') {
      steps {
        // Compile the app and it's dependencies
        bat 'gradlew compileDebugSources'
      }
    }
    stage('Unit test') {
      steps {
        // Compile and run the unit tests for the app and it's dependencies
        bat 'gradlew testDebugUnitTest testDebugUnitTest'
        
        // Analyse the test results and update the build result as appropriate
        junit '**/TEST-*.xml'
        recordIssues enabledForFailure: true, aggregatingResults: true, tools: junitParser(pattern: '**/TEST-*.xml', reportEncoding: 'UTF-8')
      }
      post {
        always {
          publishHTML([
            allowMissing: false
            alwaysLinkToLastBuild: false,
            keepAll: false,
            reportDir: '',
            reportFiles: 'index.html',
            reportName: 'junit Report'
          ])
          
          archiveArtifacts artifacts: 'junit.pdf'
        }
      }
    }
    stage('Build APK') {
      steps {
        //finish building and packaging the APK
        bat 'gradlew assembleDebug'
        
        //Archive the APKs so that they can be downloaded from Jenkins
        archiveArtifacts '**/*.apk'
      }
    }
    stage('Static Analysis') {
      parallel {
        stage('Checkstyle') {
          steps {
            bat 'gradlew checkstyle'
            recordIssues enabledForFailure: true, aggregatingResults: true, tool: checkstyle(pattern: '**/checkstyle.xml', reportEncoding: 'UTF-8')
          }
        }
        stage('PMD') {
          steps {
            bat 'gradlew pmd'
            recordIssues enabledForFailure: true, aggregatingResults: true, tool: pmdParser(pattern: '**/pmd.xml', reportEncoding: 'UTF-8')
          }
        }
        stage('Lint') {
          steps {
            bat 'gradlew lint'
            recordIssues enabledForFailure: true, aggregatingResults: true, tool: androidLintParser(pattern: '**/lint-result.xml', reportEncoding: 'UTF-8')
          }
        }
      }
    }
    stage('Deploy') {
      when {
        // Only execute this stage when building from the `beta` branch
        branch 'beta'
      }
      environment {
        // Assuming a file credential has been added to Jenkins, with the ID 'my-app-signing-keystore',
        // this will export an environment variable during the build, pointing to the absolute path of
        // the stored Android keystore file.  When the build ends, the temporarily file will be removed.
        SIGNING_KEYSTORE = credentials('my-app-signing-keystore')

        // Similarly, the value of this variable will be a password stored by the Credentials Plugin
        SIGNING_KEY_PASSWORD = credentials('my-app-signing-password')
      }
      steps {
        // Build the app in release mode, and sign the APK using the environment variables
        sh './gradlew assembleRelease'

        // Archive the APKs so that they can be downloaded from Jenkins
        archiveArtifacts '**/*.apk'

        // Upload the APK to Google Play
        androidApkUpload googleCredentialsId: 'Google Play', apkFilesPattern: '**/*-release.apk', trackName: 'beta'
      }
    }
  }
}

@NonCPS
def getLastCommit() {
  def commitMessage = ""
  def changeLogSets = currentBuild.changeSets
  try {
    def entries = changeLogSets.last()
    def entry = entries.last()
    commitMessage = "Last commit ${entry.commitId} by [${entry.author}] on ${new Date(entry.timestamp)}:\n${entry.msg}"
  } catch {
    commitMessage = "Unknown changes"
  }
  return commitMessage
}
