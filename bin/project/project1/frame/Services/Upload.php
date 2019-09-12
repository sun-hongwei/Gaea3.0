
<?php

require_once("UploadUserProcessor.php");

$filename = $_POST['filename'];
$userdata = $_POST['userdata'];
$command = $_POST['command'];
$filesize = $_POST['filesize'];

$dir = dirname(dirname(__FILE__));
$uploadDir = $dir . DIRECTORY_SEPARATOR. "uploads" . DIRECTORY_SEPARATOR;

$fullfilename = getFile($uploadDir, $command, $filename, $userdata);
$fullfilename = iconv('UTF-8','GB2312',$fullfilename);
$dir = dirname($fullfilename);
if (!file_exists($dir)){
	mkdir($dir,0777,true);
}	

$tmpFile = $uploadDir . md5($fullfilename);

$isok = true;
if (!move_uploaded_file($_FILES["file"]["tmp_name"],$tmpFile)){
	$isok = false;
}

$size = 0;
if ($isok){
	$size = filesize($tmpFile);
	$isok = $size > 0;
}

if ($isok){
	if ($size > 0){}
	$fp = fopen($fullfilename, "abw");
	try{
		$handle = fopen($tmpFile, "rb");  
		try{
			$buffer = fread($handle, $size);
			fwrite($fp, $buffer, $size);  
		}catch(Exception $e){
			$isok = false;
		}
		fclose($handle); 
	}catch(Exception $e){
		$isok = false;
	}
	fclose($fp);
	
	if ($isok){
		$size = filesize($fullfilename);
		if ($size > $filesize){
			unlink($fullfilename);
			$isok = false;
		}else if ($size == $filesize){
			$isok = processCommand($command, $fullfilename, $userdata);
		}
	}
}

if (file_exists($tmpFile))
	unlink($tmpFile); 	

if ($isok)
	echo '{"ret":true}';
else
	echo '{"ret":false}';

?>