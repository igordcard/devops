/* Copyright 2017 Sandvine
 *
 * All Rights Reserved.
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may
 *   not use this file except in compliance with the License. You may obtain
 *   a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 */

def project_checkout(url_prefix,project,refspec,revision) {
    // checkout the project
    // this is done automaticaly by the multibranch pipeline plugin
    // git url: "${url_prefix}/${project}"

    sh "git fetch origin ${refspec}"
    if (GERRIT_PATCHSET_REVISION.size() > 0 ) {
        sh "git checkout -f ${revision}"
    }
}

def ci_pipeline(mdg,url_prefix,project,branch,refspec,revision,build_system) {
    println("build_system = ${build_system}")
    ci_helper = load "devops/jenkins/ci-pipelines/ci_helper.groovy"

    stage('Prepare') {
        sh 'env'
    }

    stage('Checkout') {
        project_checkout(url_prefix,project,refspec,revision)
    }

    container_name = "${project}-${branch}".toLowerCase()

    stage('Docker-Build') {
        sh "docker build -t ${container_name} ."
    }

    withDockerContainer("${container_name}") {
        stage('Docker-Setup') {
            sh '''
               groupadd -o -g $(id -g) -r jenkins
               useradd -o -u $(id -u) --create-home -r -g  jenkins jenkins
               '''
        }
        stage('Test') {
            sh 'devops-stages/stage-test.sh'
        }
        stage('Build') {
            sh(returnStdout:true,  script: 'devops-stages/stage-build.sh').trim()
        }
    }

    stage('Archive') {
        sh(returnStdout:true,  script: 'devops-stages/stage-archive.sh').trim()
        ci_helper.archive(mdg,branch,'untested')
    }

    if ( build_system ) {

        stage('Build System') {
            def downstream_params_stage_3 = [
                string(name: 'GERRIT_BRANCH', value: "${branch}"),
                string(name: 'UPSTREAM_JOB_NAME', value: "${JOB_NAME}" ),
                string(name: 'UPSTREAM_JOB_NUMBER', value: "${BUILD_NUMBER}" ),
            ]

            // callout to stage_3.  This is the system build
            result = build job: "osm-stage_3/${branch}", parameters: downstream_params_stage_3, propagate: true
            if (result.getResult() != 'SUCCESS') {
                project = result.getProjectName()
                build = result.getNumber()
                error("${project} build ${build} failed")
            }
        }
    }

}

return this
