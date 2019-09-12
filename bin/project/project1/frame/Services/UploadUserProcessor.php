
<?php
//$name=$_POST['username'];

function getFile($uploadDir, $command, $filename, $userdata){
    switch($command){
        default:
        return $uploadDir . $filename;
    }
}
function processCommand($command, $filename, $userdata){
    switch($command){
        default:
        return true;
    }
}

?>