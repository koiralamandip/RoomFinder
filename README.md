# RoomFinder

1. Place room_finder directory under "xampp/htdocs/". THis the the backend RESTful API created using PHP.

2. Create a database in mysql called "roomfinder". Or feel free to change the names in room_finder/databaseConnection.php

3. Create two tables users and rooms under that database, with the following:

| Tables | Entities | Attributes | Data types | Constraints |
| ------ | -------- | ---------- | ---------- | ----------- |
| users | user | user_id | NUMBER(11) | PK |
| | | firstname | VARCHAR(255) | |
| | | surname | VARCHAR(255) | |
| | | phone | VARCHAR(10) | |
| | | username | VARCHAR(255) | |
| | | password | VARCHAR(255) | |
| | | | | |
| rooms | room | room_id | NUMBER(11) | PK |
| | | room_count | NUMBER(10) | |
| | | room_type | VARCHAR(255) | |
| | | price | VARCHAR(10) | |
| | | location | VARCHAR(255) | |
| | | latitude | VARCHAR(255) | |
| | | longitude | VARCHAR(255) | |
| | | contact | VARCHAR(10) | |
| | | ownerName | VARCHAR(255) | |
| | | description | TEXT | |
| | | user_id* | NUMBER(11) | FK |

4. You may need to modify the localhost IP address to match your's, inside Session.java, property name serverIP.
