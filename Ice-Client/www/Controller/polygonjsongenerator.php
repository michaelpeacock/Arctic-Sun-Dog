<?php
$str_json = file_get_contents('php://input');
$myfile = fopen("../Public/polygon.json", "w") or die("Unable to open file!");

echo $str_json;
fwrite($myfile, $str_json);
fclose($myfile);
?>