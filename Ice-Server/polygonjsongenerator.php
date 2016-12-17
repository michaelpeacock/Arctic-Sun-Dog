<?php
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PATCH, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Origin, Content-Type, X-Auth-Token');
header('Content-type: application/json');
$json = file_get_contents('php://input');
$json_decode = json_decode($json, true); 
$json_encode = json_encode($json_decode);
echo $json_encode;

$myfile = fopen("polygon.txt", "w") or die("Unable to open file!");
fwrite($myfile, $json_encode);
fclose($myfile);
?>