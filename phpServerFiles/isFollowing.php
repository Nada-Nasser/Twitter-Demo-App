<?php
require "DBInfo.inc";

$user_id = $_GET["user_id"];
$following_user_id = $_GET["following_user_id"];

// http://localhost/TwitterServer/isFollowing.php?user_id=1&following_user_id=2

$query = "SELECT * FROM `following` WHERE `user_id` = ". $user_id ." AND `following_user_id` = ".$following_user_id.";";

$result = mysqli_query($dbConnection , $query);


$userInfo = array();

if(!$result)
{
	die("Error in query");
}
else{
	while ($row = mysqli_fetch_assoc($result))
	{
		$userInfo[] = $row;
		break;
	}

	if ($userInfo) {
		$arr = array('msg' => 'following');
		$output = $arr;

	}
	else {
		$arr = array('msg' => 'not following');
		$output = $arr;
	}
}


echo (json_encode($output));

mysqli_free_result($result);
//5- close connection
mysqli_close($dbConnection);

?>

