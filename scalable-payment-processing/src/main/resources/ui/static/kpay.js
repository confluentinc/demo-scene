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
    $('#refreshAccountTable').click(function() {
        refreshAccountTable();
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
function refreshAccountTable() {
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

function createPaymentPipelineChart() {
    console.log("Loading createPaymentPipelineChart")
    var date = moment().subtract(1, 'hour');

    var data = [randomBar(date, 30)];
    while (data.length < 60) {
        date = date.clone().add(1, 'minute');
        data.push(randomBar(date, data[data.length - 1].y));
    }

    var ctx = document.getElementById('paymentPipelineChart').getContext('2d');
    var cfg = {
        type: 'bar',
        data: {
            datasets: [{
                label: 'Inflight Count',
                data: [],
                type: 'line',
                pointRadius: 1,
                fill: false,
                lineTension: 0,
                borderWidth: 2,
                cubicInterpolationMode: 'monotone'
            },
                {
                    label: 'Inflight $',
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
                    label: 'Confirmed Count',
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
                    label: 'Confirmed $',
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
                        labelString: 'Pipeline performance'
                    }
                }]
            }
        }
    };
    paymentPipelineChart = new Chart(ctx, cfg);
}

function createLatencyChart() {
    console.log("Loading createLatencyChart")
    var date = moment().subtract(1, 'hour');

    var data = [randomBar(date, 30)];
    while (data.length < 60) {
        date = date.clone().add(1, 'minute');
        data.push(randomBar(date, data[data.length - 1].y));
    }

    var ctx = document.getElementById('latencyChart').getContext('2d');
    var cfg = {
        type: 'bar',
        data: {
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
                label: 'Payment $ Total ',
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
                label: 'Max latency (ms)',
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
                label: 'Min latency (ms)',
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
                        labelString: 'Pipeline latency'
                    }
                }]
            }
        }
    };
chart = new Chart(ctx, cfg);
}


/**
 *  {
  "k": {
    "type": "inflightStats",
    "amount": 45.8,
    "count": 1
  },
  "v": {
    "type": "confirmedStats",
    "amount": 8444.69,
    "count": 170
  }
}
**/
function refreshPaymentPipelineChart() {
    $.get({
        url: "/kpay/metrics/pipeline",
        success: function (e) {
            console.log(e)
            var inflightCount = new Object();
            inflightCount.y = e.k.count;
            inflightCount.t = e.k.timestamp;

            var inflightAmount = new Object();
            inflightAmount.y = e.k.amount;
            inflightAmount.t = e.k.timestamp;

            var confirmedCount = new Object();
            confirmedCount.y = e.v.count;
            confirmedCount.t = e.v.timestamp;

            var confirmedAmount = new Object();
            confirmedAmount.y = e.v.amount;
            confirmedAmount.t = e.v.timestamp;

            paymentPipelineChart.data.datasets[0].data.push(inflightCount);
            paymentPipelineChart.data.datasets[1].data.push(inflightAmount);
            paymentPipelineChart.data.datasets[2].data.push(confirmedCount);
            paymentPipelineChart.data.datasets[3].data.push(confirmedAmount);
            paymentPipelineChart.update();
            return false;
        }
    })
}

/**
 "totalPayments": 3
 "totalDollarAmount": 170.22,
 "maxLatency": 471,
 "minLatency": 338,
 **/
function refreshLatencyChart() {
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
    createLatencyChart();
    createPaymentPipelineChart();
    createTable();

    refreshAccountTable();
    refreshLatencyChart();
    refreshPaymentPipelineChart();
}
window.onload = createStuff;

setInterval(function(){
    refreshLatencyChart();
    refreshPaymentPipelineChart();
    refreshAccountTable();
}, 5000);
