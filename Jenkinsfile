// input parameters:
//   boolean: BUILD_FROM_SOURCE
//   string:  TAG_OR_BRANCH

node {
    stage("Checkout") {
        git branch: 'systest', url: 'https://osm.etsi.org/gerrit/osm/devops'
    }
    stage("Build") {
        from_source = ''
        if ( params.BUILD_FROM_SOURCE )
        {
            from_source = '--source'
        }
        container_name=params.TAG_OR_BRANCH.replaceAll(/\./,"")

        sh "jenkins/host/start_build system --build-container osm-${container_name} -b ${params.TAG_OR_BRANCH} ${from_source}"
    }
}
