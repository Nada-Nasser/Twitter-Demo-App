<?php
require "DBInfo.inc";

$email = $_GET["email"];

$password = $_GET["password"];


// http://localhost/TwitterServer/Login.php?email=mohamed@yahoo.com&password=123456

$query = "SELECT * FROM `login` WHERE email = '".$email."' AND password = '".$password."'";

$result = mysqli_query($dbConnection , $query);


$userInfo = array();

if(!$result)
{
	print ("{'msg' : 'Login failed}");
}
else{
	while ($row = mysqli_fetch_assoc($result))
	{
		$userInfo[] = $row;
		break;
	}

	if ($userInfo) {
		print(" {\"msg\":\"loggedIn\",
		 \"info\":'".json_encode($userInfo)."'}");

	}
	else {
		print("{'msg':'loginFailed'}");
	}
}


mysqli_free_result($result);
//5- close connection
mysqli_close($dbConnection);

?>

