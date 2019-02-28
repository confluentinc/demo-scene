var rawResponseBody = '';
var xhr = new XMLHttpRequest();

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};

window.chartColors = {
	red: 'rgb(255, 99, 132)',
	orange: 'rgb(255, 159, 64)',
	yellow: 'rgb(255, 205, 86)',
	green: 'rgb(75, 192, 192)',
	blue: 'rgb(54, 162, 235)',
	purple: 'rgb(153, 102, 255)',
	grey: 'rgb(201, 203, 207)'
};



function displayServerVersion() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var serverVersionResponse = JSON.parse(this.responseText);
            document.getElementById("copyright").innerHTML = "(c) Confluent Inc., KSQL server v" + serverVersionResponse.KsqlServerInfo.version
        }
    };
    xhr.open("GET", "/info", true);
    xhr.send();
}

function sendRequest(resource, sqlExpression) {
    xhr.abort();

    var properties = getProperties();

    xhr.onreadystatechange = function() {
        if (xhr.response !== '' && ((xhr.readyState === 3 && streamedResponse) || xhr.readyState === 4)) {
            rawResponseBody = xhr.response;
            renderResponse();
        }
        if (xhr.readyState === 4 || xhr.readyState === 0) {
            document.getElementById('request_loading').hidden = true;
            document.getElementById('cancel_request').hidden = true;
        }
    };

    var data = JSON.stringify({
        'ksql': sqlExpression,
        'streamsProperties': properties
    });

    console.log("Sending:" + data)

    document.getElementById('cancel_request').hidden = false;
    document.getElementById('request_loading').hidden = false;
    xhr.open('POST', resource);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(data);
}

function cancelRequest() {
    //var responseElement = document.getElementById('response');
    //var response = responseElement.innerHTML;
    xhr.abort();
    //responseElement.innerHTML = response;
}

function upperCaseFirst(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}
function isPrimitive(test) {
    return (test !== Object(test));
};

function createTable() {
    var dataSet = []
    taskTable = $('#taskTable').DataTable( {
        data: dataSet,
        "columns" : [ { title: "Task-Id",
                            "data" : "id"
                        }, {title: "Group",
                            "data" : "groupId"
                        }, {
                            title: "Priority",
                            "data" : "priority"
                        }, {
                            title: "Tag",
                            "data" : "tag"
                        }, {
                            title: "Source",
                           "data" : "source"
                        }, {
                            title: "Status",
                          "data" : "status"
                        }, {
                            title: "Submitted",
                          "data" : "submitted"
                        }, {
                            title: "Allocated",
                          "data" : "allocated"
                        }, {
                            title: "Running",
                          "data" : "running"
                        }, {
                            title: "Completed",
                          "data" : "completed"
                        }, {
                            title: "Duration",
                          "data" : "duration"
                        }, {
                            title: "Worker",
                          "data" : "workerEndpoint"
                        }, {
                            title: "Meta",
                          "data" : "meta"
                        }
                     ]
    } );
    $('#refreshTable').click(function() {
        refreshTable();
        refreshChart();
    })
}

function refreshTable() {
    $.get({
          url:"/kwq/tasks",
          success:function(data){
                taskTable.clear();
                data.forEach(e => {
                    e.duration = ""
                    if (e.submitted_ts != 0) { e.submitted = moment(e.submitted_ts).format("HH:mm:ss") }
                    else { e.submitted = "" }

                    if (e.allocated_ts != 0) { e.allocated = moment(e.allocated_ts).format("HH:mm:ss") }
                    else { e.allocated = "" }

                    if (e.running_ts != 0) { e.running = moment(e.running_ts).format("HH:mm:ss") }
                    else { e.running = ""}

                    if (e.completed_ts != 0) {
                        e.completed = moment(e.completed_ts).format("HH:mm:ss")
                        e.duration = (e.completed_ts - e.running_ts) / 1000
                        }
                    else { e.completed = "" }
                })
                taskTable.rows.add(data);
                taskTable.draw();
            return false;
          }
    })
}

function randomNumber(min, max) {
			return Math.random() * (max - min) + min;
}

function randomBar(date, lastClose) {
    var open = randomNumber(lastClose * 0.95, lastClose * 1.05);
    var close = randomNumber(open * 0.95, open * 1.05);
    return {
        t: date.valueOf(),
        y: 0//close
    };
}

// TODO: add labels dynamically https://github.com/chartjs/Chart.js/issues/2738
function createChart() {
    console.log("Loading mainChart")
    var date = moment().subtract(1, 'hour');

    var data = [randomBar(date, 30)];
    while (data.length < 60) {
        date = date.clone().add(1, 'minute');
        data.push(randomBar(date, data[data.length - 1].y));
    }

    //var data = [];
    var ctx = document.getElementById('mainChart').getContext('2d');
    var cfg = {
        type: 'bar',
        data: {
       // labels: ['total', 'submitted', 'allocated', 'running', 'error', 'completed'],
        /** {
          "total": 0,
          "submitted": 0,
          "allocated": 0,
          "running": 0,
          "error": 0,
          "completed": 0,
          "time": 0
        }
        **/
            datasets: [{
                label: 'Total',
                data: data,
                type: 'line',
                pointRadius: 1,
                fill: false,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },
            {
                label: 'Submitted',
                data: data,
                type: 'line',
                pointRadius: 1,
                fill: false,
                        backgroundColor: window.chartColors.yellow,
                        borderColor: window.chartColors.yellow,

                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },{
                label: 'Allocated',
                data: data,
                type: 'line',
                pointRadius: 1,
                fill: false,
                backgroundColor: window.chartColors.orange,
                borderColor: window.chartColors.orange,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },{
                label: 'Running',
                data: data,
                type: 'line',
                pointRadius: 1,
                fill: false,
                backgroundColor: window.chartColors.blue,
                borderColor: window.chartColors.blue,
                lineTension: 0,
                borderWidth: 2
            }, {
                label: 'Error',
                data: data,
                type: 'line',
                pointRadius: 3,
                fill: false,
                backgroundColor: window.chartColors.red,
                borderColor: window.chartColors.red,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },
            {
                label: 'Complete',
                data: data,
                type: 'line',
                pointRadius: 2,
                fill: false,
                backgroundColor: window.chartColors.green,
                borderColor: window.chartColors.green,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            }]
        },
        options: {
            scales: {
                xAxes: [{
                    type: 'time',
                    distribution: 'series'
                }],
                yAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: 'Task status by Job'
                    }
                }]
            }
        }
    };
chart = new Chart(ctx, cfg);
}

function removeChartData(chart) {
    chart.data.labels.pop();
    chart.data.datasets.forEach((dataset) => {
        dataset.data.pop();
    });
    chart.update();
}

function addChartData(label, data) {
    //chart.data.labels.push(label);
    chart.data.datasets.forEach((dataset) => {
        dataset.data.push(data);
    });
    chart.update();
}


/**
{
  "total": 0,
  "submitted": 0,
  "allocated": 0,
  "running": 0,
  "error": 0,
  "completed": 0,
  "time": 0
}
**/
function refreshChart() {
    $.get({
          url:"/kwq/stats",
          success:function(data){
          chart.data.datasets[0].data = []
          chart.data.datasets[1].data = []
          chart.data.datasets[2].data = []
          chart.data.datasets[3].data = []
          chart.data.datasets[4].data = []
          chart.data.datasets[5].data = []
                data.forEach(e => {
                    console.log(e)
                    var total = new Object();
                    total.y = e.total;
                    total.t = e.time;

                    var submitted = new Object();
                    submitted.y = e.submitted;
                    submitted.t = e.time;

                    var allocated = new Object();
                    allocated.y = e.allocated;
                    allocated.t = e.time;

                    var running = new Object();
                    running.y = e.running;
                    running.t = e.time;

                    var error = new Object();
                    error.y = e.error;
                    error.t = e.time;

                    var completed = new Object();
                    completed.y = e.completed;
                    completed.t = e.time;

                    chart.data.datasets[0].data.push(total);
                    chart.data.datasets[1].data.push(submitted);
                    chart.data.datasets[2].data.push(allocated);
                    chart.data.datasets[3].data.push(running);
                    chart.data.datasets[4].data.push(error);
                    chart.data.datasets[5].data.push(completed);
                })
                chart.update();
            return false;
          }
    })
}

function createStuff() {
    createChart();
    createTable();
    refreshTable();
    refreshChart();

}
window.onload = createStuff;

setInterval(function(){
    refreshChart();
    refreshTable();
}, 10000);
