# Groovy Events Listener Plugin

A Jenkins plugin, which executes Groovy code when an event occurs.

Table of contents
---

1. [Overview](#overview)
1. [Building](#building)
1. [Basic Usage](#basic-usage)
1. [Authors](#authors)
1. [License](#license)
1. [Similar Plugins](#similar-plugins)
1. [Releases (Release Notes, Changelog, Artifacts)](../../releases)
1. [Issues (Bugs / Issues / Enhancements)](../../issues)

Overview
---

The reason I created the plugin was because I wanted to integrate Jenkins with an external application.
Invoking a Jenkins jobs via the REST API was simple, but getting Jenkins to callback the external application wasn't
straight forward.

All the plugins I'd seen either had to be individually configured per job (i.e. in a post build step), or their features
were limited to making a HTTP GET/POST request (a bit restrictive).

Basically:

- I wanted to be able to write my own code
- I didn't want to repeat myself

So I wrote this plugin. Along the way, I realised it could have some other applications too:

- customised logging
- performance monitoring
- incident escalation
- integration with 3rd party applications
- much more...

Building
---

Prerequisites:

- JDK 11 (or above)

To setup for use with Intellij IDEA

```Shell
./gradlew cleanIdea idea
```

To run Jenkins ([http://localhost:8080](http://localhost:8080)) locally with the plugin installed:

```Shell
./gradlew clean server
```

To build the Jenkins plugin (.jpi) file:

```Shell
./gradlew clean jpi
```

To publish/release the Jenkins plugin:

1. Update the `version` in `gradle.properties`, to remove "-SNAPSHOT" (increment and re-add after publishing)

```Shell
./gradlew clean publish
```

Basic Usage
---

To get started:

1. Install the plugin (or [run Jenkins locally](#building))
1. Navigate to the *Jenkins > Manage Jenkins > Configuration* page
1. You should now see a *Global Events Plugin* section (similar to the following screenshot).

![Version 1.0.0](src/main/site/screenshot-version-1.005.png "Version 1.005")

This plugin executes the supplied Groovy code, every time an event is triggered.

So lets get started with the simplest example.

```Groovy
log.info "hello world!"
```

Now save the changes, kick off a Jenkins job, and you will see "hello world!" written to the logs three times. Alternatively,
there's now a `Test Groovy Code` button, which will execute the code with the `event`=`RunListener.onStarted`.

The plugin actually injects a couple of variables, which you can use in your code. Here's some examples using the `event`
and `env` variables.

This code limits the logging to only occur when a job is completed! **N.B.** this behaviour can also be replicated using the configuration options.

```Groovy
if (event == Event.JOB_STARTED) {
    log.info "hello world!"
}
```

And this one filters on jobs whose name starts with "Foobar":

```Groovy
if (env.JOB_NAME.startsWith('Foobar')) {
    log.info "hello world!"
}
```

There is also a `context` variable of type `Map`. You can add your own entries to it, by returning a `Map` from your code.
E.g.

```Groovy
if (event == Event.JOB_FINALIZED) {
    def newCount = (context.finishCount ?: 0) + 1
    log.info "hello world! finishCount=$newCount"
    return [finishCount: newCount]
}
```

This will keep a record in memory, of how many times jobs have finished. You can achieve the same result by
adding variables directly to the `context` variable:

```Groovy
if (event == Event.JOB_FINALIZED) {
    context.finishCount = (context.finishCount ?: 0) + 1
    log.info "hello world! finishCount=${context.finishCount}"
}
```

You can also use `@Grab` annotations if you'd like to import external dependencies
(thanks [Daniel](https://github.com/CoreMedia/job-dsl-plugin/commit/830fae7a0fd8a046c620600e46633166804190e3) for your solution!).

```Groovy
@Grab('commons-lang:commons-lang:2.4')
import org.apache.commons.lang.WordUtils
log.info "Hello ${WordUtils.capitalize('world')}!"
```

Not bad! And finally, you can import Groovy scripts, so you can hide away some of the heavy lifting. Here I'm using
a [RestClient.groovy](src/main/site/includes/RestClient.groovy) script.

```Groovy
def client = evaluate(new File('../includes/RestClient.groovy'))

def resp = client.post('http://localhost:9200/jenkins/runInstances', [
        jobName       : env.JOB_NAME,
        jobDuration   : run.duration,
        jobResult     : run.result.toString(),
        jobBuildNumber: run.number,
        jobTimestamp  : run.timestamp.timeInMillis,
])
assert resp.status == 201
```

You can pretty much do whatever you want from here: custom logging to a file, sending performance metrics to
a server, sending email or messenger notifications, calling a SOAP service... The world's your oyster. If
you've got something cool that you want to share, let me know and I'll add it to the [examples](src/main/site/examples)!

For more details on which events trigger the code, what variables are available and details on configuring logging,
please see the plugin's [help file](https://cdn.rawgit.com/jenkinsci/groovy-events-listener-plugin/master/src/main/resources/org/jenkinsci/plugins/globalEventsPlugin/GlobalEventsPlugin/help-onEventGroovyCode.html).

Authors
---

Marky Jackson <marky.r.jackson@gmail.com>

License
---

Licensed under the [MIT License (MIT)](LICENSE)

Similar Plugins
---

These plugins have similar (but different) functionality:

- [https://wiki.jenkins-ci.org/display/JENKINS/Notification+Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Notification+Plugin)
- [https://wiki.jenkins-ci.org/display/JENKINS/Extreme+Notification+Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Extreme+Notification+Plugin)
- [https://github.com/speedledger/elasticsearch-jenkins](https://github.com/speedledger/elasticsearch-jenkins)
- [https://github.com/jenkinsci/post-completed-build-result-plugin](https://github.com/jenkinsci/post-completed-build-result-plugin)
