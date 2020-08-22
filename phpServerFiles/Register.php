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
	$output = "{'msg' : 'user registered successfuly'}";
}	
else{
	$output = "{'msg' : 'faild'}";
}

echo ($output);


//5- close connection
mysqli_close($dbConnection);

?>