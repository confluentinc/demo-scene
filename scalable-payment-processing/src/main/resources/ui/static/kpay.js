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
            document.getElementById("copyright").innerHTML = "(c) Confluent Inc., KPay" + serverVersionResponse.KsqlServerInfo.version
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
    accountTable = $('#accountTable').DataTable( {
        data: dataSet,
        "columns" : [ { title: "Username",
                            "data" : "name"
                        }, {title: "Amount",
                            "data" : "amount"
                        }, {
                            title: "Last Amount",
                            "data" : "lastAmount"
                        }, {
                            title: "Last When?",
                            "data" : "lastWhen"
                        }
                     ]
    } );
    $('#refreshTable').click(function() {
        refreshTable();
        refreshChart();
    })
}

/**
 * {
    "amount": -186.68,
    "lastPayment": {
      "amount": 41.09,
      "from": "mary",
      "id": "mary",
      "state": "debit",
      "timestamp": 1551455838493,
      "to": "adrian",
      "txnId": "pay-1551455838493"
    },
    "name": "mary"
  }
 */
function refreshTable() {
    $.get({
          url:"/kpay/listAccounts",
          success:function(data){
                accountTable.clear();
                data.forEach(e => {
                    e.lastAmount = e.lastPayment.amount;
                    e.lastWhen = moment(e.lastPayment.timestamp);
                })
                accountTable.rows.add(data);
                accountTable.draw();
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
         {
  "largestPayment": {
    "amount": 75.66,
    "from": "larry",
    "id": "pay-1551457231539",
    "state": "complete",
    "timestamp": 1551457231539,
    "to": "allan",
    "txnId": "pay-1551457231539"
  },
  "totalPayments": 3
  "totalDollarAmount": 170.22,
  "maxLatency": 471,
  "minLatency": 338,
  "throughputPerWindow": 0,
}
        **/
            datasets: [{
                label: 'Payment Count',
                data: [],
                type: 'line',
                pointRadius: 1,
                fill: false,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },
            {
                label: 'Payment Total ',
                data: [],
                type: 'line',
                pointRadius: 1,
                fill: false,
                        backgroundColor: window.chartColors.yellow,
                        borderColor: window.chartColors.yellow,

                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },{
                label: 'Max latency',
                data: [],
                type: 'line',
                pointRadius: 1,
                fill: false,
                backgroundColor: window.chartColors.orange,
                borderColor: window.chartColors.orange,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },{
                label: 'Min latency',
                data: [],
                type: 'line',
                pointRadius: 1,
                fill: false,
                backgroundColor: window.chartColors.blue,
                borderColor: window.chartColors.blue,
                lineTension: 0,
                borderWidth: 2
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
                        labelString: 'Payment statistics'
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
 "totalPayments": 3
 "totalDollarAmount": 170.22,
 "maxLatency": 471,
 "minLatency": 338,
**/
function refreshChart() {
    $.get({
        url: "/kpay/metrics/throughput",
        success: function (e) {
            console.log(e)
            var totalPayments = new Object();
            totalPayments.y = e.totalPayments;
            totalPayments.t = e.timestamp;

            var totalDollarAmount = new Object();
            totalDollarAmount.y = e.totalDollarAmount;
            totalDollarAmount.t = e.timestamp;

            var maxLatency = new Object();
            maxLatency.y = e.maxLatency;
            maxLatency.t = e.timestamp;

            var minLatency = new Object();
            minLatency.y = e.minLatency;
            minLatency.t = e.timestamp;

            chart.data.datasets[0].data.push(totalPayments);
            chart.data.datasets[1].data.push(totalDollarAmount);
            chart.data.datasets[2].data.push(maxLatency);
            chart.data.datasets[3].data.push(minLatency);
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
