import hudson.model.Queue
import hudson.model.Job
import hudson.model.queue.SubTask

if (event == 'ExecutorListener.taskStarted') {
    Queue.Task task = task
    if (task instanceof Job) {
        log.info "ExecutorListener.taskStarted: Job, name: ${task.name}, url: ${task.url}"
    }
    if (task instanceof SubTask) {
        log.info "ExecutorListener.taskStarted: SubTask, name: ${task.name}, url: ${task.url}"
    }
}
if (event == 'ExecutorListener.taskCompleted') {
    Queue.Task task = task
    long durationMS = durationMS
    if (task instanceof Job) {
        log.info "ExecutorListener.taskCompleted: Job, duration: ${durationMS}, name: ${task.name}, url: ${task.url}"
    }
    if (task instanceof SubTask) {
        log.info "ExecutorListener.taskCompleted: SubTask, duration: ${durationMS}, name: ${task.name}, url: ${task.url}"
    }
}