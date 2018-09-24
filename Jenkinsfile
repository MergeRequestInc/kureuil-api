#!groovy
import groovy.transform.Field

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(daysToKeepStr:'15'))
    }

    parameters {
        booleanParam(name: 'RELEASE_BUILD', description: 'Start a release procedure ?', defaultValue: false)
    }

    stages {
        stage('Configuration')
        {
            steps {
                ansiColor('xterm')
                {
                    sh "git config user.email \"jenkins@xephi.fr\" && git config user.name \"Jenkins\" && git config push.default simple"
					script {
						env.GIT_URL = sh(returnStdout: true, script: 'git config --get remote.origin.url').trim()
					}
                    library 'jenkins-release'
                }
            }
        }
        stage('Pre-Release')
        {
            when {
                expression {
                    return params.RELEASE_BUILD
                }
            }
            steps {
                ansiColor('xterm')
                {
					/*
					 * Get the version from file by the regex given
					*/
					script {
						env.current_version = getProjectVersion('version.sbt')
					}

                    /*
                     * Remove the -SNAPSHOT and parse the major.minor.patch pattern
                    */
					releaseVersion('version.sbt')
                    echo "Start a release for version ${env.release}."
                }
            }
        }
        stage('Build') {
            steps {
                ansiColor('xterm') {
                    /* 
                    * Use jenkins tools with correct name : Java8 and SBT
                    * Care: Tools name are case sensitives
                    */
                    sh "${tool name: 'sbt-1.2.3', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt clean update test:compile"
                }
            }
        }
        stage('Unit Test')
        {
            steps {
                ansiColor('xterm') {
                    lock('kureuil-api') {
                        sh "${tool name: 'sbt-1.2.3', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt clean coverage test"
                    }
                    sh "${tool name: 'sbt-1.2.3', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt coverageReport"
                    sh "${tool name: 'sbt-1.2.3', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt coverageAggregate"
                }
            }
        }
        stage('Package') {
            steps {
                ansiColor('xterm') {
                    sh "${tool name: 'sbt-1.2.3', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt package debian:packageBin"
                }
            }
        }
        stage('Archive')
        {
            steps {
                ansiColor('xterm')
                {
                    /*
                     * Get test reports
                    */
                    junit allowEmptyResults: true, testResults: '**/target/test-reports/*.xml'

                    /*
                     * Scoverage report
                     */
                    step([$class: 'ScoveragePublisher', reportDir: 'target/scala-2.12/scoverage-report', reportFile: 'scoverage.xml'])

                    /*
                     * Archive artifacts on nexus
                    */
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/**/*.jar **/target/**/*.deb'
                }
            }
        }
        stage('Release')
        {
            when {
                expression {
                    return params.RELEASE_BUILD
                }
            }
            steps {
                ansiColor('xterm')
                {
                    sshagent(credentials: ['jenkins-deploy-key'])
                    {
                        generateChangelog()
						tagAndPush()
						bumpVersion('version.sbt')
						script {
							if (env.BRANCH_NAME.equalsIgnoreCase("master"))
							{
								retry(10) {
									bumpMinor('version.sbt')
								}
							}
						}
                    }
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}
