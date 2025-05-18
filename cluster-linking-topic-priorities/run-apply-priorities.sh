function runPriorities() {
    node apply-priorities.js
    sleep 10
    runPriorities
}

runPriorities