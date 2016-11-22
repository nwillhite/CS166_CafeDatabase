COPY MENU
FROM '/home/csmajs/nwill016/Downloads/CS166_CafeDatabase-master/data/menu.csv'
WITH DELIMITER ';';

COPY USERS
FROM '/home/csmajs/nwill016/Downloads/CS166_CafeDatabase-master/data/users.csv'
WITH DELIMITER ';';

COPY ORDERS
FROM '/home/csmajs/nwill016/Downloads/CS166_CafeDatabase-master/data/orders.csv'
WITH DELIMITER ';';
ALTER SEQUENCE orders_orderid_seq RESTART 87257;

COPY ITEMSTATUS
FROM '/home/csmajs/nwill016/Downloads/CS166_CafeDatabase-master/data/itemStatus.csv'
WITH DELIMITER ';';

