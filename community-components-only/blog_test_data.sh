#!/usr/bin/env bash

curl "https://api.mockaroo.com/api/bf24ebf0?count=100&key=ff7856d0"|kafkacat -P -b localhost:9092 -t orders

curl "https://api.mockaroo.com/api/b9dd8b20?count=1000&key=ff7856d0"|kafkacat -P -b localhost:9092 -t store_customers

kafkacat -P -b localhost:9092 -t syslog_dummy <<EOF
{"host":"BZ2,24a43cde91a0,v3.9.27.8537"}
{"host":"BZ2,dc9fdbec6a10,v3.9.27.8537"}
{"host":"asgard02"}
{"host":"U7PG2,f09fc2238301,v3.9.27.8537"}
{"host":"rpi-03"}
{"host":"rpi-02"}
EOF

kafkacat -P -b localhost:9092 -t products <<EOF
{"product":"Toaster","sku":"H1235"}
{"product":"Kettle","sku":"H1425"}
{"product":"Banana","sku":"F0192"}
{"product":"Apple","sku":"F1723"}
{"product":"Cat","sku":"x1234"}
EOF

kafkacat -P -b localhost:9092 -t ubnt_users <<EOF
{"device_type":"Raspberr","device_name":"rpi-01.moffatt.me"}
{"device_type":"","device_name":"Fire 01 (Red)"}
{"device_type":"","device_name":"Fire 02 (Yellow)"}
{"device_type":"SlimDevi","device_name":"Squeezebox - Kitchen"}
{"device_type":"SlimDevi","device_name":"Squeezebox - Sitting Room"}
{"device_type":"Raspberr","device_name":"rpi-03.moffatt.me"}
{"device_type":"","device_name":"cdh57-01-node-01.moffatt.me"}
{"device_type":"Apple","device_name":"Robin's work iPhone"}
{"device_type":"Apple","device_name":"Burner iPhone"}
{"device_type":"","device_name":"logstash-irc.moffatt.me"}
EOF

kafkacat -P -b localhost:9092 -t customers <<EOF
{"id":1,"first_name":"Darrell","last_name":"MacNamara","email":"dmacnamara0@theguardian.com","country":"US"}
{"id":2,"first_name":"Henri","last_name":"McGrail","email":"hmcgrail1@economist.com","country":"CA"}
{"id":3,"first_name":"Shanta","last_name":"Levicount","email":"slevicount2@exblog.jp","country":"US"}
{"id":4,"first_name":"Cicily","last_name":"Dalrymple","email":"cdalrymple3@goo.ne.jp","country":"FR"}
{"id":5,"first_name":"Kathrine","last_name":"MacClay","email":"kmacclay4@shutterfly.com","country":"US"}
{"id":6,"first_name":"Eadith","last_name":"Hartzogs","email":"ehartzogs5@yolasite.com","country":"FR"}
{"id":7,"first_name":"Benedikta","last_name":"Astlet","email":"bastlet6@tripadvisor.com","country":"FR"}
{"id":8,"first_name":"Peg","last_name":"Brogi","email":"pbrogi7@tripadvisor.com","country":"FR"}
{"id":9,"first_name":"Corbet","last_name":"Du Fray","email":"cdufray8@google.es","country":"FR"}
{"id":10,"first_name":"Elwira","last_name":"Done","email":"edone9@sfgate.com","country":"CA"}
{"id":11,"first_name":"Mavra","last_name":"Turfus","email":"mturfusa@ihg.com","country":"FR"}
{"id":12,"first_name":"Dean","last_name":"Attridge","email":"dattridgeb@unicef.org","country":"US"}
{"id":13,"first_name":"Tabbie","last_name":"Turtle","email":"tturtlec@istockphoto.com","country":"US"}
{"id":14,"first_name":"Elga","last_name":"Switsur","email":"eswitsurd@gov.uk","country":"US"}
{"id":15,"first_name":"Selene","last_name":"Dilland","email":"sdillande@booking.com","country":"US"}
{"id":16,"first_name":"Joshia","last_name":"Monshall","email":"jmonshallf@bluehost.com","country":"CA"}
{"id":17,"first_name":"Kristoffer","last_name":"Spiaggia","email":"kspiaggiag@so-net.ne.jp","country":"US"}
{"id":18,"first_name":"Myrtie","last_name":"Hearn","email":"mhearnh@arizona.edu","country":"CA"}
{"id":19,"first_name":"Domeniga","last_name":"Cross","email":"dcrossi@nymag.com","country":"US"}
{"id":20,"first_name":"Pru","last_name":"Devine","email":"pdevinej@ted.com","country":"FR"}
{"id":21,"first_name":"Kenna","last_name":"Martindale","email":"kmartindalek@narod.ru","country":"US"}
{"id":22,"first_name":"Bethanne","last_name":"Lightewood","email":"blightewoodl@lycos.com","country":"FR"}
{"id":23,"first_name":"Atlante","last_name":"Richard","email":"arichardm@google.nl","country":"FR"}
{"id":24,"first_name":"Giorgi","last_name":"Tern","email":"gternn@google.com.au","country":"US"}
{"id":25,"first_name":"Sondra","last_name":"Hyndes","email":"shyndeso@meetup.com","country":"CA"}
{"id":26,"first_name":"Shelba","last_name":"Gohier","email":"sgohierp@gov.uk","country":"CA"}
{"id":27,"first_name":"Darb","last_name":"Rousel","email":"drouselq@weather.com","country":"US"}
{"id":28,"first_name":"Angie","last_name":"Kinnin","email":"akinninr@alexa.com","country":"CA"}
{"id":29,"first_name":"Marchelle","last_name":"Nellen","email":"mnellens@ycombinator.com","country":"FR"}
{"id":30,"first_name":"Rowen","last_name":"Pache","email":"rpachet@forbes.com","country":"CA"}
{"id":31,"first_name":"Reynard","last_name":"Rulf","email":"rrulfu@psu.edu","country":"GB"}
{"id":32,"first_name":"Bartolemo","last_name":"Skeleton","email":"bskeletonv@sogou.com","country":"US"}
{"id":33,"first_name":"Jim","last_name":"Reynalds","email":"jreynaldsw@ed.gov","country":"US"}
{"id":34,"first_name":"Karon","last_name":"Bartczak","email":"kbartczakx@nymag.com","country":"FR"}
{"id":35,"first_name":"Krystyna","last_name":"Da Costa","email":"kdacostay@mayoclinic.com","country":"CA"}
{"id":36,"first_name":"Heall","last_name":"Twigge","email":"htwiggez@businessinsider.com","country":"FR"}
{"id":37,"first_name":"Hildegaard","last_name":"Vasler","email":"hvasler10@aboutads.info","country":"DE"}
{"id":38,"first_name":"Gavrielle","last_name":"Petti","email":"gpetti11@weebly.com","country":"FR"}
{"id":39,"first_name":"Dario","last_name":"Stoakes","email":"dstoakes12@youtube.com","country":"FR"}
{"id":40,"first_name":"Kamillah","last_name":"Beddows","email":"kbeddows13@furl.net","country":"CA"}
{"id":41,"first_name":"Joete","last_name":"Cunio","email":"jcunio14@myspace.com","country":"FR"}
{"id":42,"first_name":"Shana","last_name":"Clausson","email":"sclausson15@goo.gl","country":"FR"}
{"id":43,"first_name":"Justine","last_name":"Hankinson","email":"jhankinson16@foxnews.com","country":"US"}
{"id":44,"first_name":"Wallie","last_name":"Lonie","email":"wlonie17@altervista.org","country":"FR"}
{"id":45,"first_name":"Karleen","last_name":"Jopke","email":"kjopke18@rakuten.co.jp","country":"FR"}
{"id":46,"first_name":"Alysa","last_name":"Kauschke","email":"akauschke19@adobe.com","country":"FR"}
{"id":47,"first_name":"Kahlil","last_name":"Grimmett","email":"kgrimmett1a@wikipedia.org","country":"FR"}
{"id":48,"first_name":"Melina","last_name":"Van der Kruys","email":"mvanderkruys1b@is.gd","country":"FR"}
{"id":49,"first_name":"Krystyna","last_name":"Dowle","email":"kdowle1c@myspace.com","country":"FR"}
{"id":50,"first_name":"Marshall","last_name":"Bohin","email":"mbohin1d@wordpress.org","country":"FR"}
{"id":51,"first_name":"Rossy","last_name":"Kenlin","email":"rkenlin1e@washingtonpost.com","country":"FR"}
{"id":52,"first_name":"Dulcine","last_name":"Nevins","email":"dnevins1f@yellowpages.com","country":"FR"}
{"id":53,"first_name":"Derwin","last_name":"Kirman","email":"dkirman1g@yolasite.com","country":"FR"}
{"id":54,"first_name":"Ruddy","last_name":"Lightoller","email":"rlightoller1h@netlog.com","country":"FR"}
{"id":55,"first_name":"Barnebas","last_name":"Ezzy","email":"bezzy1i@tuttocitta.it","country":"DE"}
{"id":56,"first_name":"Munroe","last_name":"Pettyfar","email":"mpettyfar1j@wired.com","country":"FR"}
{"id":57,"first_name":"Elston","last_name":"Rothman","email":"erothman1k@oaic.gov.au","country":"FR"}
{"id":58,"first_name":"Erek","last_name":"Whellans","email":"ewhellans1l@psu.edu","country":"FR"}
{"id":59,"first_name":"Bern","last_name":"Westman","email":"bwestman1m@so-net.ne.jp","country":"FR"}
{"id":60,"first_name":"Sebastien","last_name":"Jeens","email":"sjeens1n@surveymonkey.com","country":"FR"}
{"id":61,"first_name":"Kris","last_name":"Gear","email":"kgear1o@irs.gov","country":"FR"}
{"id":62,"first_name":"Dot","last_name":"Dabourne","email":"ddabourne1p@storify.com","country":"US"}
{"id":63,"first_name":"Laurette","last_name":"Calderhead","email":"lcalderhead1q@bigcartel.com","country":"US"}
{"id":64,"first_name":"Freddi","last_name":"Forton","email":"fforton1r@weibo.com","country":"US"}
{"id":65,"first_name":"Deane","last_name":"Dallewater","email":"ddallewater1s@wsj.com","country":"US"}
{"id":66,"first_name":"Gillan","last_name":"Fahrenbacher","email":"gfahrenbacher1t@furl.net","country":"FR"}
{"id":67,"first_name":"Emmet","last_name":"Decort","email":"edecort1u@blog.com","country":"US"}
{"id":68,"first_name":"Ulberto","last_name":"Sango","email":"usango1v@symantec.com","country":"CA"}
{"id":69,"first_name":"Cacilie","last_name":"Muzzollo","email":"cmuzzollo1w@multiply.com","country":"FR"}
{"id":70,"first_name":"Raquela","last_name":"Dmitriev","email":"rdmitriev1x@examiner.com","country":"US"}
{"id":71,"first_name":"Evie","last_name":"Cossom","email":"ecossom1y@alexa.com","country":"FR"}
{"id":72,"first_name":"Noella","last_name":"Konig","email":"nkonig1z@mapy.cz","country":"FR"}
{"id":73,"first_name":"Helli","last_name":"Vennart","email":"hvennart20@dailymotion.com","country":"FR"}
{"id":74,"first_name":"Ferdinand","last_name":"Buesnel","email":"fbuesnel21@shutterfly.com","country":"CA"}
{"id":75,"first_name":"Delmar","last_name":"Leyson","email":"dleyson22@youku.com","country":"CA"}
{"id":76,"first_name":"Donn","last_name":"Housecroft","email":"dhousecroft23@nyu.edu","country":"GB"}
{"id":77,"first_name":"Evangelina","last_name":"Rossbrook","email":"erossbrook24@epa.gov","country":"CA"}
{"id":78,"first_name":"Elysee","last_name":"Thumann","email":"ethumann25@gizmodo.com","country":"US"}
{"id":79,"first_name":"Salomi","last_name":"Angelini","email":"sangelini26@purevolume.com","country":"FR"}
{"id":80,"first_name":"Winni","last_name":"Redan","email":"wredan27@mapy.cz","country":"FR"}
{"id":81,"first_name":"Clarette","last_name":"Hillock","email":"chillock28@hhs.gov","country":"FR"}
{"id":82,"first_name":"Cris","last_name":"Darling","email":"cdarling29@opensource.org","country":"FR"}
{"id":83,"first_name":"Gilligan","last_name":"Borgars","email":"gborgars2a@oracle.com","country":"FR"}
{"id":84,"first_name":"Meyer","last_name":"Doyle","email":"mdoyle2b@people.com.cn","country":"FR"}
{"id":85,"first_name":"Gifford","last_name":"Drysdale","email":"gdrysdale2c@washingtonpost.com","country":"FR"}
{"id":86,"first_name":"Shelden","last_name":"Gouny","email":"sgouny2d@storify.com","country":"DE"}
{"id":87,"first_name":"Kara-lynn","last_name":"Dillintone","email":"kdillintone2e@census.gov","country":"FR"}
{"id":88,"first_name":"Rodger","last_name":"De Courtney","email":"rdecourtney2f@dropbox.com","country":"CA"}
{"id":89,"first_name":"Dania","last_name":"Swalteridge","email":"dswalteridge2g@amazonaws.com","country":"US"}
{"id":90,"first_name":"Ludovika","last_name":"Halewood","email":"lhalewood2h@ucsd.edu","country":"FR"}
{"id":91,"first_name":"Dimitry","last_name":"Warrier","email":"dwarrier2i@cnbc.com","country":"DE"}
{"id":92,"first_name":"Ruggiero","last_name":"Carnilian","email":"rcarnilian2j@g.co","country":"CA"}
{"id":93,"first_name":"Beatriz","last_name":"Coffelt","email":"bcoffelt2k@goodreads.com","country":"US"}
{"id":94,"first_name":"Torr","last_name":"O'Brien","email":"tobrien2l@skyrock.com","country":"CA"}
{"id":95,"first_name":"Kimmie","last_name":"Ranald","email":"kranald2m@ehow.com","country":"US"}
{"id":96,"first_name":"Shara","last_name":"Kordes","email":"skordes2n@ucoz.com","country":"FR"}
{"id":97,"first_name":"Cassondra","last_name":"Dumbelton","email":"cdumbelton2o@blog.com","country":"CA"}
{"id":98,"first_name":"Newton","last_name":"Wooderson","email":"nwooderson2p@g.co","country":"US"}
{"id":99,"first_name":"Ajay","last_name":"Barson","email":"abarson2q@google.com.hk","country":"CA"}
{"id":100,"first_name":"Barri","last_name":"Handman","email":"bhandman2r@wiley.com","country":"CA"}
EOF