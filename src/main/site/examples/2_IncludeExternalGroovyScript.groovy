if (event == 'RunListener.onFinalized') {
    def client = evaluate(new File('../includes/RestClient.groovy'))

    def resp = client.post('http://localhost:9200/jenkins/runInstances', [
            jobName       : env.JOB_NAME,
            jobDuration   : run.duration,
            jobResult     : run.result.toString(),
            jobBuildNumber: run.number,
            jobTimestamp  : run.timestamp.timeInMillis,
    ])
    assert resp.status == 201
    log.info "HTTP Post completed successfully."
}