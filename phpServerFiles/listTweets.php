<?php
require "DBInfo.inc";

 // define quesry  
 //StartFrom

if ($usename=$_GET['op'] == 1) // send my ID, to list my following tweets (tweets wrote by user i follow) 
{ 
// my following

// http://localhost:8080/TwitterServer/listTweets.php?op=1&user_id=1&StartFrom=0

	$query="select * from user_tweets where user_id in (select following_user_id from following where user_id=". $_GET['user_id'] . ") or user_id=" . $_GET['user_id'] . " order by tweet_date DESC". 
" LIMIT 20 OFFSET ". $_GET['StartFrom']  ;

}

elseif ($usename=$_GET['op'] == 2) // send selected id to display his tweets only
{ // specific person tweets

// http://localhost:8080/TwitterServer/listTweets.php?op=2&user_id=1&StartFrom=0

$query="select * from user_tweets where user_id=" . $_GET['user_id'] . " order by tweet_date DESC" . 
" LIMIT 20 OFFSET ". $_GET['StartFrom'] ;  // $usename=$_GET['username'];

}

elseif ($usename=$_GET['op']==3) 
{ // search post

// http://localhost:8080/TwitterServer/listTweets.php?op=3&query=hello&StartFrom=0

$query="select * from user_tweets where tweet_text like '%" . $_GET['query'] . 
"%' LIMIT 20 OFFSET ". $_GET['StartFrom'] ;  // $usename=$_GET['username'];

}

$result = mysqli_query($dbConnection , $query);


$tweetsList = array();

if(!$result)
{
	die("Error in query");
}
else
{
	while ($row = mysqli_fetch_assoc($result))
	{
		$tweetsList[] = $row;
	}

	if ($tweetsList) 
	{

		print(" {\"msg\":\"hasTweets\",
		 \"tweets\":'".json_encode($tweetsList)."'}");

	}
	else 
	{
		$arrMsgs = array('msg' => 'noTweets');

		print ($arrMsgs);
	}
}


mysqli_free_result($result);
//5- close connection
mysqli_close($dbConnection);

?>

