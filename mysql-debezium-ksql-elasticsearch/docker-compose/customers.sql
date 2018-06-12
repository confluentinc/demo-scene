GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replicator' IDENTIFIED BY 'replpass';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT  ON *.* TO 'debezium' IDENTIFIED BY 'dbz';

# Create the database that we'll use to populate data and watch the effect in the binlog
CREATE DATABASE demo;
GRANT ALL PRIVILEGES ON demo.* TO 'mysqluser'@'%';

use demo;

create table CUSTOMERS (
        id INT PRIMARY KEY,
        first_name VARCHAR(50),
        last_name VARCHAR(50),
        email VARCHAR(50),
        gender VARCHAR(50),
	club_status VARCHAR(8),
        comments VARCHAR(90),
        create_ts timestamp DEFAULT CURRENT_TIMESTAMP ,
        update_ts timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (1, 'Rica', 'Blaisdell', 'rblaisdell0@rambler.ru', 'Female', 'bronze', 'Universal optimal hierarchy');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (2, 'Ruthie', 'Brockherst', 'rbrockherst1@ow.ly', 'Female', 'platinum', 'Reverse-engineered tangible interface');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (3, 'Mariejeanne', 'Cocci', 'mcocci2@techcrunch.com', 'Female', 'bronze', 'Multi-tiered bandwidth-monitored capability');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (4, 'Hashim', 'Rumke', 'hrumke3@sohu.com', 'Male', 'platinum', 'Self-enabling 24/7 firmware');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (5, 'Hansiain', 'Coda', 'hcoda4@senate.gov', 'Male', 'platinum', 'Centralized full-range approach');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (6, 'Robinet', 'Leheude', 'rleheude5@reddit.com', 'Female', 'platinum', 'Virtual upward-trending definition');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (7, 'Fay', 'Huc', 'fhuc6@quantcast.com', 'Female', 'bronze', 'Operative composite capacity');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (8, 'Patti', 'Rosten', 'prosten7@ihg.com', 'Female', 'silver', 'Integrated bandwidth-monitored instruction set');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (9, 'Even', 'Tinham', 'etinham8@facebook.com', 'Male', 'silver', 'Virtual full-range info-mediaries');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (10, 'Brena', 'Tollerton', 'btollerton9@furl.net', 'Female', 'silver', 'Diverse tangible methodology');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (11, 'Alexandro', 'Peeke-Vout', 'apeekevouta@freewebs.com', 'Male', 'gold', 'Ameliorated value-added orchestration');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (12, 'Sheryl', 'Hackwell', 'shackwellb@paginegialle.it', 'Female', 'gold', 'Self-enabling global parallelism');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (13, 'Laney', 'Toopin', 'ltoopinc@icio.us', 'Female', 'platinum', 'Phased coherent alliance');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (14, 'Isabelita', 'Talboy', 'italboyd@imageshack.us', 'Female', 'platinum', 'Cloned transitional synergy');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (15, 'Rodrique', 'Silverton', 'rsilvertone@umn.edu', 'Male', 'gold', 'Re-engineered static application');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (16, 'Clair', 'Vardy', 'cvardyf@reverbnation.com', 'Male', 'platinum', 'Expanded bottom-line Graphical User Interface');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (17, 'Brianna', 'Paradise', 'bparadiseg@nifty.com', 'Female', 'bronze', 'Open-source global toolset');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (18, 'Waldon', 'Keddey', 'wkeddeyh@weather.com', 'Male', 'gold', 'Business-focused multi-state functionalities');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (19, 'Josiah', 'Brockett', 'jbrocketti@com.com', 'Male', 'gold', 'Realigned didactic info-mediaries');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (20, 'Anselma', 'Rook', 'arookj@europa.eu', 'Female', 'gold', 'Cross-group 24/7 application');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (21, 'Basilio', 'Shall', 'bshallk@hibu.com', 'Male', 'platinum', 'Robust human-resource Graphical User Interface');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (22, 'Court', 'Kibard', 'ckibardl@histats.com', 'Male', 'platinum', 'Programmable zero defect knowledge user');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (23, 'Ronna', 'Larne', 'rlarnem@walmart.com', 'Female', 'silver', 'Exclusive non-volatile core');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (24, 'Thom', 'Bockmann', 'tbockmannn@digg.com', 'Male', 'gold', 'Reduced modular installation');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (25, 'Isac', 'Wasselin', 'iwasselino@goodreads.com', 'Male', 'gold', 'Persistent well-modulated customer loyalty');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (26, 'Kristofer', 'Godilington', 'kgodilingtonp@npr.org', 'Male', 'platinum', 'Decentralized heuristic function');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (27, 'Archy', 'Dunstan', 'adunstanq@gravatar.com', 'Male', 'platinum', 'Pre-emptive needs-based productivity');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (28, 'Emilio', 'Giovanitti', 'egiovanittir@hibu.com', 'Male', 'silver', 'Front-line directional application');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (29, 'Jameson', 'Cruwys', 'jcruwyss@twitter.com', 'Male', 'gold', 'Operative homogeneous functionalities');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (30, 'Brice', 'Nason', 'bnasont@163.com', 'Male', 'silver', 'Multi-layered human-resource hardware');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (31, 'Nollie', 'Bartrap', 'nbartrapu@mashable.com', 'Male', 'silver', 'Function-based 24/7 monitoring');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (32, 'Albrecht', 'Muress', 'amuressv@themeforest.net', 'Male', 'platinum', 'Persevering client-driven alliance');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (33, 'Levey', 'Johannes', 'ljohannesw@tripod.com', 'Male', 'platinum', 'Re-engineered mission-critical open system');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (34, 'Jacinda', 'Cavil', 'jcavilx@jalbum.net', 'Female', 'gold', 'Diverse maximized focus group');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (35, 'Giusto', 'Hawtry', 'ghawtryy@biblegateway.com', 'Male', 'silver', 'Inverse cohesive flexibility');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (36, 'Taryn', 'Hallwell', 'thallwellz@exblog.jp', 'Female', 'silver', 'Intuitive methodical artificial intelligence');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (37, 'Rosene', 'Baggally', 'rbaggally10@unblog.fr', 'Female', 'silver', 'Centralized bifurcated utilisation');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (38, 'Leo', 'Coogan', 'lcoogan11@free.fr', 'Male', 'platinum', 'Reverse-engineered didactic knowledge user');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (39, 'Linda', 'Wipper', 'lwipper12@seattletimes.com', 'Female', 'platinum', 'Distributed optimal strategy');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (40, 'Deeann', 'Willgress', 'dwillgress13@feedburner.com', 'Female', 'gold', 'Optimized coherent firmware');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (41, 'Gawen', 'Tessington', 'gtessington14@mtv.com', 'Male', 'silver', 'Focused even-keeled ability');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (42, 'Lorrayne', 'Varney', 'lvarney15@netvibes.com', 'Female', 'bronze', 'Customizable coherent migration');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (43, 'Jerry', 'Vlies', 'jvlies16@blogs.com', 'Female', 'platinum', 'Multi-channelled solution-oriented throughput');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (44, 'Dar', 'Dabrowski', 'ddabrowski17@who.int', 'Male', 'gold', 'Universal maximized throughput');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (45, 'Corette', 'Megarrell', 'cmegarrell18@edublogs.org', 'Female', 'bronze', 'Distributed composite collaboration');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (46, 'Ann', 'Adamovitch', 'aadamovitch19@digg.com', 'Female', 'bronze', 'Balanced user-facing alliance');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (47, 'Farleigh', 'Robottom', 'frobottom1a@ifeng.com', 'Male', 'silver', 'Upgradable intangible standardization');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (48, 'Lenna', 'Penwell', 'lpenwell1b@auda.org.au', 'Female', 'silver', 'Robust intangible circuit');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (49, 'Hobard', 'Grzeskowski', 'hgrzeskowski1c@fastcompany.com', 'Male', 'platinum', 'Public-key transitional project');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (50, 'Graham', 'Whitwell', 'gwhitwell1d@dot.gov', 'Male', 'silver', 'Re-contextualized fault-tolerant flexibility');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (51, 'Dimitry', 'Parrish', 'dparrish1e@fda.gov', 'Male', 'bronze', 'Balanced fault-tolerant artificial intelligence');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (52, 'Giovanni', 'Forrestor', 'gforrestor1f@hostgator.com', 'Male', 'silver', 'Polarised 6th generation algorithm');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (53, 'Vivie', 'Jobe', 'vjobe1g@privacy.gov.au', 'Female', 'silver', 'Horizontal bottom-line portal');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (54, 'Thedrick', 'Perrygo', 'tperrygo1h@webeden.co.uk', 'Male', 'bronze', 'Cloned 6th generation database');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (55, 'Martelle', 'Boshell', 'mboshell1i@blogspot.com', 'Female', 'silver', 'Optional eco-centric concept');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (56, 'Penni', 'Rowbottam', 'prowbottam1j@amazon.co.jp', 'Female', 'bronze', 'Optimized intermediate flexibility');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (57, 'Farley', 'Coytes', 'fcoytes1k@ucoz.ru', 'Male', 'bronze', 'Up-sized 4th generation model');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (58, 'Virgie', 'Romeril', 'vromeril1l@stumbleupon.com', 'Male', 'silver', 'Object-based client-server definition');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (59, 'Nessi', 'Broxup', 'nbroxup1m@wix.com', 'Female', 'platinum', 'Switchable modular policy');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (60, 'Shermy', 'Gallear', 'sgallear1n@amazonaws.com', 'Male', 'gold', 'Intuitive zero administration leverage');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (61, 'Bret', 'Scarlan', 'bscarlan1o@cmu.edu', 'Male', 'platinum', 'Stand-alone bandwidth-monitored adapter');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (62, 'Phaedra', 'Okill', 'pokill1p@chron.com', 'Female', 'bronze', 'Streamlined cohesive frame');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (63, 'Bliss', 'Pikhno', 'bpikhno1q@bizjournals.com', 'Female', 'platinum', 'Public-key value-added extranet');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (64, 'Michaella', 'Mattock', 'mmattock1r@slashdot.org', 'Female', 'silver', 'User-friendly intermediate adapter');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (65, 'Muire', 'Blant', 'mblant1s@themeforest.net', 'Female', 'gold', 'Persistent system-worthy firmware');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (66, 'Thorpe', 'Howels', 'thowels1t@sfgate.com', 'Male', 'silver', 'Extended scalable success');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (67, 'Druci', 'Lehrmann', 'dlehrmann1u@multiply.com', 'Female', 'bronze', 'Robust executive adapter');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (68, 'Ward', 'McKirton', 'wmckirton1v@archive.org', 'Male', 'gold', 'Decentralized 24 hour ability');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (69, 'Reggy', 'Heyworth', 'rheyworth1w@walmart.com', 'Male', 'platinum', 'Focused 24/7 analyzer');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (70, 'Erminia', 'Krelle', 'ekrelle1x@umn.edu', 'Female', 'gold', 'Enterprise-wide zero administration migration');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (71, 'Milt', 'Kubicki', 'mkubicki1y@dmoz.org', 'Male', 'platinum', 'Virtual dedicated website');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (72, 'Ramonda', 'Dawton', 'rdawton1z@creativecommons.org', 'Female', 'silver', 'Self-enabling system-worthy adapter');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (73, 'Elijah', 'Caig', 'ecaig20@chronoengine.com', 'Male', 'bronze', 'Cloned bandwidth-monitored encoding');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (74, 'Agna', 'Manton', 'amanton21@patch.com', 'Female', 'bronze', 'Customer-focused well-modulated utilisation');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (75, 'Rani', 'Manneville', 'rmanneville22@deliciousdays.com', 'Female', 'bronze', 'Intuitive maximized artificial intelligence');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (76, 'Colline', 'Kenewel', 'ckenewel23@answers.com', 'Female', 'bronze', 'Realigned national instruction set');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (77, 'Elroy', 'Pepperell', 'epepperell24@census.gov', 'Male', 'platinum', 'Re-engineered web-enabled solution');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (78, 'Franciska', 'Rubica', 'frubica25@over-blog.com', 'Female', 'gold', 'Versatile client-server circuit');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (79, 'Hollyanne', 'Coda', 'hcoda26@biglobe.ne.jp', 'Female', 'silver', 'Object-based clear-thinking orchestration');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (80, 'Jeannine', 'Viste', 'jviste27@hexun.com', 'Female', 'platinum', 'Switchable 5th generation capacity');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (81, 'Ninette', 'Lecky', 'nlecky28@xing.com', 'Female', 'gold', 'Quality-focused real-time task-force');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (82, 'Cyrillus', 'Kindall', 'ckindall29@blogspot.com', 'Male', 'bronze', 'Implemented uniform functionalities');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (83, 'Aili', 'Shurrock', 'ashurrock2a@nih.gov', 'Female', 'gold', 'Devolved empowering circuit');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (84, 'Kristofer', 'Brolechan', 'kbrolechan2b@soundcloud.com', 'Male', 'silver', 'Robust 5th generation installation');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (85, 'Pollyanna', 'Hillen', 'phillen2c@examiner.com', 'Female', 'silver', 'Distributed stable moderator');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (86, 'Ketti', 'Basezzi', 'kbasezzi2d@hhs.gov', 'Female', 'platinum', 'Cross-group uniform encoding');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (87, 'Mano', 'Redford', 'mredford2e@google.com.br', 'Male', 'platinum', 'Inverse 24/7 service-desk');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (88, 'Dunn', 'Armsby', 'darmsby2f@salon.com', 'Male', 'silver', 'Team-oriented motivating extranet');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (89, 'Inness', 'Chin', 'ichin2g@sciencedirect.com', 'Male', 'silver', 'Synchronised client-server secured line');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (90, 'Sileas', 'Puckett', 'spuckett2h@addthis.com', 'Female', 'silver', 'Re-engineered grid-enabled moderator');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (91, 'Jacquenetta', 'Tilston', 'jtilston2i@yellowpages.com', 'Female', 'bronze', 'Fully-configurable 5th generation standardization');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (92, 'Genovera', 'Dewer', 'gdewer2j@spotify.com', 'Female', 'silver', 'Up-sized mission-critical budgetary management');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (93, 'Augustin', 'Lebond', 'alebond2k@fema.gov', 'Male', 'silver', 'Customer-focused dynamic core');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (94, 'Athene', 'Dericut', 'adericut2l@wired.com', 'Female', 'silver', 'Reduced needs-based matrices');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (95, 'Zebedee', 'Gusticke', 'zgusticke2m@github.io', 'Male', 'silver', 'Versatile needs-based solution');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (96, 'Pasquale', 'Lemme', 'plemme2n@amazon.co.uk', 'Male', 'bronze', 'Streamlined executive interface');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (97, 'Aimil', 'Vanns', 'avanns2o@wikipedia.org', 'Female', 'bronze', 'Triple-buffered didactic help-desk');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (98, 'Ferdie', 'Rosnau', 'frosnau2p@trellian.com', 'Male', 'bronze', 'Cross-group cohesive synergy');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (99, 'Feliks', 'Klimowski', 'fklimowski2q@issuu.com', 'Male', 'silver', 'Networked multimedia process improvement');
insert into CUSTOMERS (id, first_name, last_name, email, gender, club_status, comments) values (100, 'Pembroke', 'Creer', 'pcreer2r@slashdot.org', 'Male', 'gold', 'Virtual multimedia definition');
