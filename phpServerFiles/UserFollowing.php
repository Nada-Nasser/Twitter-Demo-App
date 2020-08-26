<?php

require "DBInfo.inc";

$user_id = $_GET["user_id"];
$following_user_id = $_GET["following_user_id"];
$op = $_GET["op"];



// http://localhost:8080/TwitterServer/UserFollowing.php?user_id=1&following_user_id=2&op=1

if($op == 1) // [user id]  follow [following user id]
{ 
	$query = 

		"INSERT INTO `following`(`user_id`, `following_user_id`) VALUES ("
		.$user_id
		.","
		.$following_user_id
		.");";

	$result = mysqli_query($dbConnection , $query);

	if ($result) {

		$arr = array('msg' => 'follow done');
		$output = $arr;
	}
	else{
		//$output = "{'msg' : 'post failed'}";
		$arr = array('msg' => 'follow failed');
		$output = $arr;
	}
}
elseif ($op == 2)  //[user id] unfollow [user id] 
{
	$query = "DELETE FROM `following` WHERE `user_id` = ".$user_id." AND `following_user_id` = ".$following_user_id.";";

	$result = mysqli_query($dbConnection , $query);

	if ($result) {

		//$output = "{\"msg\" : \"post added\"}";
		$arr = array('msg' => 'unfollow done');
		$output = $arr;
	}
	else{
		die("Error in query");
	}
}

echo (json_encode($output));


//5- close connection
mysqli_close($dbConnection);

?>