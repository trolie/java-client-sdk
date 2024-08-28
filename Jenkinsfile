@Library(['BSFBuildLibrary', 'MarketsBuildExtensions']) _
import ge.energy.grid.*
import ge.energy.markets.*

def versionNumber = VersionNumber([projectStartDate: '',
                                   versionNumberString: '${BUILD_YEAR}${BUILD_MONTH, XX}${BUILD_DAY, XX}_${BUILDS_TODAY, XX}',
                                   versionPrefix: '',
                                   worstResultForIncrement: 'ABORTED'])

def extensions = new marketsextensions(this)
def utils = new buildpipeline(this, [branchingStrategy: extensions.marketsBranchingStrategy()])
def goal = utils.isProductionBranch() ? "deploy" : "install"
// Build target repository used if goal is deploy
def buildTarget = extensions.isReleaseRepository() ? "release" : "snapshot"
def trimmedBranch = extensions.getTrimmedBranch()
def projectName = "trolie-java-client-sdk"

def APP_VERSION = "1.0.0"

pipeline {

    // Note that most of our builds have limited infrastructure, and there
    // are some cases where we have stateful resources.  This is general best practice.
    options {
        disableConcurrentBuilds()
    }
    agent { label 'dind' }

    environment {
        artifactoryUrl = "https://artifactory.build.ge.com"
        artifactoryCredentials = "GRID_ARTIFACTORY_USERPASS"
        rpmRemote = "ngem-rpm-release"
        ARTIFACTORY = credentials('GRID_ARTIFACTORY_USERPASS')
        DOCKER_REGISTRY = 'dig-grid-artifactory.apps.ge.com'
        DOCKER_REG_CREDS = credentials("GRID_ARTIFACTORY_USERPASS")
    }

    stages {
        stage('Build and Publish') {

            environment {
                SIGNER_KEYSTORE = credentials('codesigner.p12')
                SIGNER_STORE_PASS = credentials('code_signer_storepass')
                SIGNER_ALIAS = credentials('kosh_signer_userpass')
            }

            steps {
                script {
                    echo "Setting build version"

                    currentBuild.displayName = versionNumber
                    echo "Build version set to ${versionNumber}"

                    imageSuffix = "${trimmedBranch}_${BUILD_NUMBER}".toLowerCase()
                    withCredentials([usernamePassword(credentialsId: 'GRID_ARTIFACTORY_USERPASS',
                            usernameVariable: 'ARTIFACTORY_USR', passwordVariable: 'ARTIFACTORY_PSW')]) {

                        sh "docker login -u $ARTIFACTORY_USR -p $ARTIFACTORY_PSW $DOCKER_REGISTRY"
                        sh "cp $SIGNER_KEYSTORE ./codesigner.p12"
                        sh """docker build \
                --pull \
                --no-cache \
                --build-arg ARTIFACTORY_USR=$ARTIFACTORY_USR \
                --build-arg ARTIFACTORY_PSW=$ARTIFACTORY_PSW \
                --build-arg SIGNER_KEYSTORE=codesigner.p12 \
                --build-arg SIGNER_STORE_PASS=$SIGNER_STORE_PASS \
                --build-arg SIGNER_ALIAS=$SIGNER_ALIAS_USR \
                --build-arg SIGNER_KEY_PASS=$SIGNER_ALIAS_PSW \
                --build-arg BUILD_NUMBER=${versionNumber} \
                --build-arg BUILD_GOAL=$goal \
                -t ${projectName}_$imageSuffix \
                .
                """
                        sh "docker run -d --name ctr_${projectName}_$imageSuffix ${projectName}_$imageSuffix"
                        sh "docker cp ctr_${projectName}_$imageSuffix:/src/. $workspace/results/"

                        // Clean up our leftovers out of the Docker Daemon.
                        sh "docker container rm --force ctr_${projectName}_$imageSuffix || true"
                    }


                    // Now, all the files end up in the "results" directory.
                    // The RPM can be uploaded from there.
                    if( utils.isProductionBranch() ) {
                        // Publish build container so downstream builds can use this image to speed up their build
                        // Note: Container version should be kept in sync with pom version.
                        //       Could automate reading/trimming pom version in the future
                        tag = "${APP_VERSION}_${versionNumber}"
                        imageName = "dig-grid-artifactory.apps.ge.com/ngem-docker/${projectName}-build-base:${tag}"
                        echo "Pushing build container $imageName"
                        sh "docker tag ${projectName}_$imageSuffix $imageName"
                        sh "docker push $imageName"
                    }

                    // Clean up our leftovers out of the Docker Daemon.
                    sh "docker image rm --force ${projectName}_$imageSuffix || true"

                }
            }
        }
    }

    post {
        success {
            script {
                tag_git {
                    gitCredentialsId = "github-markets"
                    gitRepo = "github.build.ge.com/energy-markets/trolie-java-client-sdk"
                    buildNumber = versionNumber
                }
                utils.notifyComplete(currentBuild.currentResult)
            }
        }
        changed {
            script {
                if( utils.isProductionBranch() ) {
                    utils.notifyChanged(currentBuild.currentResult)
                }
            }
        }
        failure {
            script {
                if( utils.isProductionBranch() ) {
                    utils.notifyFailed()
                }
            }
        }
        always {
            script{
                utils.notifyComplete(currentBuild.currentResult)
                sh "rm -f codesigner.p12"
            }
        }
    }
}