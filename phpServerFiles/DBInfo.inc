<?php

$host = "";
$user = "";
$dbpassword="";
$dbName = "twitter";

$dbConnection = mysqli_connect($host,$user,$dbpassword,$dbName);

if(mysqli_connect_errno())
{
	die("ERROR : ".mysqli_connect_error());
	return;
}

?>