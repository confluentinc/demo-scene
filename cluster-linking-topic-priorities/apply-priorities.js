/*** Lag Settings ***/

// priority list of topic names
const priorities = [ 
    ['outpost.critical1', 'outpost.critical2'], 
    ['outpost.medium1', 'outpost.medium2'], 
    ['outpost.low1', 'outpost.low2'] 
];

// if this lag is reached, then the throttles will kick in (expressed in terms of # of messages)
const lagThreshold = 10; 


/*** Cluster Settings for Destination cluster ***/
const hostname = "REPLACE";
const clusterId = "REPLACE";
const basicAuth = "REPLACE"; // base64 encoded "apiKey:apiSecret"
const linkName = "laptop_outpost_1";

const http = require("https");


function checkForLag()
{
    // API used: https://docs.confluent.io/cloud/current/api.html#tag/Cluster-Linking-(v3)/operation/listKafkaMirrorTopics
    const options = {
        "method": "GET",
        "hostname": hostname,
        "port": 443,
        "path": "/kafka/v3/clusters/" + clusterId + "/links/" + linkName + "/mirrors",
        "headers": {
            "Authorization": "Basic " + basicAuth
        }
    };
    
    const req = http.request(options, function (res) {
        const chunks = [];
        
        res.on("data", function (chunk) {
            chunks.push(chunk);
        });
        
        res.on("end", function () {
            const body = Buffer.concat(chunks);
            const topicsArray = JSON.parse(body).data;

    // pre-processing the list of mirror topics to get the info that we care about (name, lag, status)
    const mirrorTopics = {};
    const mirrorTopicsNamesArray = [];
    console.log(topicsArray.length + ' mirror topics found');
    topicsArray.forEach((topic) => {
        const name = topic.mirror_topic_name;
        const maxLag = topic.mirror_lags.reduce((max, lagObj) => Math.max(max, lagObj.lag), 0); // we only care about the maximum lag among the partitions
        mirrorTopics[name] = {
            maxLag,
            mirrorStatus: topic.mirror_status,
            topicData: topic
        };
        mirrorTopicsNamesArray.push(name);
    });
    
    let shouldThrottle = false;
    let resumeTopics = []; // these topics are "safe" and we will call the "Resume" command on them to ensure they are not throttled
    for (let i = 0; i < priorities.length; i ++)
    {
        const priorityTopics = priorities[i];
        console.log('\nPriority Level ' + i +': ' + priorityTopics.join(', '));
        // add this level of priority to the "safe" topics that we will let run
        resumeTopics = resumeTopics.concat(priorityTopics);
        
        priorityTopics.forEach((topicName) => {
            if (shouldThrottle)
            {
                return; // we're already going to throttle so don't do any more logic
            }

            const topic = mirrorTopics[topicName];
            if (topic.maxLag > lagThreshold)
            {
                console.log('Found lagging topic: ' + topicName);
                shouldThrottle = true; // lag threshold has been reached; start to throttle lower priorities
            }
            if (topic.mirrorStatus === 'PAUSED')
            {
                console.log('Found paused topic: ' + topicName);
                shouldThrottle = true; // unpause the priority levels one at a time, in order of priority
            }
        });
        
        if (shouldThrottle)
        {
            break; // break the loop at this priority level if we will throttle
        }
    }
    
    console.log('\n');
    if (shouldThrottle)
    {
        console.log('Important topics are lagging, starting to throttle topics.');
        console.log('These topics have priority and will be allowed to mirror: ' + resumeTopics.join (', '));
        // dispatch "resume" command on this list of topic names to ensure they are not paused
        alterMirrorTopics('resume', resumeTopics);
        
        // find all mirror topics that don't have priority
        const topicsToPause = mirrorTopicsNamesArray.map((topicName) => {
            if (resumeTopics.indexOf(topicName) >= 0)
            {
                return null;
            }
            return topicName;
        }).filter((e) => e !== null);
        console.log('\nThese mirror topics are low priority and will be paused: ' + topicsToPause.join(', '));
        alterMirrorTopics('pause', topicsToPause);
    }
    else
    {
        console.log('No critical lag, will resume all topics');
        // un-pause (resume) all topics to make sure nothing is throttled
        
        // dispatch "resume" command on all mirror topics on this link
        alterMirrorTopics('resume', mirrorTopicsNamesArray);
    }
    
    console.log('\n\n');
});
});

req.end();

}


function alterMirrorTopics(pauseOrResume, mirror_topic_names)
{
    const options = {
        "method": "POST",
        "hostname": hostname,
        "port": 443,
        "path": "/kafka/v3/clusters/" + clusterId + "/links/" + linkName + "/mirrors:" + pauseOrResume,
        "headers": {
        "Authorization": "Basic " + basicAuth
        }
    };
    
    const req = http.request(options, function (res) {
        const chunks = [];
    
        res.on("data", function (chunk) {
        chunks.push(chunk);
        });
    
        res.on("end", function () {
        const body = Buffer.concat(chunks);
        // console.log(body.toString());
        // TODO check for errors from the API
        });
    });

    req.write(JSON.stringify({
        mirror_topic_names
    }));

    req.end();

}


checkForLag();