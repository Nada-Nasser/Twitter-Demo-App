<?php

require "DBInfo.inc";

$user_id = $_GET["user_id"];
$tweet_text = $_GET["tweet_text"];
$tweet_picture = $_GET["tweet_picture"];


// http://localhost/TwitterServer/AddTweet.php?user_id=1&tweet_text=Hello, This is my fisrt tweet&tweet_picture=photo.png

$query = 

	"INSERT INTO `tweets`(`user_id`, `tweet_text`, `tweet_picture`) VALUES (". $user_id.",\"".$tweet_text."\",\"".$tweet_picture."\");";

$result = mysqli_query($dbConnection , $query);

if ($result) {

	//$output = "{\"msg\" : \"post added\"}";
	$arr = array('msg' => 'post added');
	$output = $arr;
}
else{
	//$output = "{'msg' : 'post failed'}";
	$arr = array('msg' => 'post failed');
	$output = $arr;
}

echo (json_encode($output));


//5- close connection
mysqli_close($dbConnection);

?>