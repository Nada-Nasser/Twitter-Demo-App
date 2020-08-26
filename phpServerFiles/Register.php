<?php

require "DBInfo.inc";

$name = $_GET["name"];
$email = $_GET["email"];
$password = $_GET["password"];
$pic = $_GET["pic"];

// http://localhost/TwitterServer/Register.php?name=mohamed&email=mohamed@yahoo.com&password=123456&pic=photo.png

$query =
		 "INSERT INTO `login`(`first_name`, `email`, `password`, `picture_path`) VALUES ('".$name."','".$email."','".$password."','"
		.$pic."');";

$result = mysqli_query($dbConnection , $query);

if ($result) {

	$arr = array('msg' => 'registered');
	$output = $arr;
}	
else{
//	$output = "{\"msg\":\"Register failed\"}";
	$arr = array('msg' => 'Registered failed');
	$output = $arr;
}

echo (json_encode($output));


//5- close connection
mysqli_close($dbConnection);

?>