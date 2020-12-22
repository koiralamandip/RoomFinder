<?php

  //Hostname of the website/server
  $host = 'localhost';
  //Database name to connect to
  $dbName = 'roomfinder';
  //Username and Password to login to the Database
  $username = 'root';
  $password = '';
  //PDO to make a connection to the above credentials
	$pdo = new PDO ('mysql:host='.$host.';dbname='.$dbName,$username,$password);
?>
