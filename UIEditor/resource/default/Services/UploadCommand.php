<?php
require_once('Common.php');
require_once('ContextOperation.php');

error_reporting(0);

$command = isset ( $_POST ["command"] ) ? $_POST ["command"] : NULL;
$require = $_POST;

if (!CommonObject::VerfiyMD5ForRequest($require)){
    $rjson = CommonObject::CreatErrorResponse ( - 10001, "数据已经被篡改！" , 
    CommonObject::GetSessionIDForRequest($require), 
    CommonObject::GetSessionInfoForRequest($require));
    return $rjson;
}

if (isset( $require["Command_Running_Hint"] ))
{
    ContextOperation::SetCommandHint($require, $require["Command_Running_Hint"]);
}

$dir = dirname(dirname(__FILE__));
$uploadDir = $dir . DIRECTORY_SEPARATOR. "uploads" . DIRECTORY_SEPARATOR;
$filename = $require["data"]["value"][0]["params"]["filename"];
$fullfilename = $uploadDir . $filename;
$fullfilename = iconv('UTF-8','GB2312',$fullfilename);

if ($command == "getSize")
{
    if (file_exists($fullfilename)){
        $size = filesize($fullfilename);
        echo '{"ret":true,"size":' . $size . '}';
    }else
        echo '{"ret":false,"size":-1}';
}else if ($command == "removeFile"){
    if(file_exists($fullfilename) && is_file($fullfilename)){
        try{
            unlink($fullfilename);
            echo '{"ret":true}';
            return;
        }catch(Exception $e){
            
        }
    }
    echo '{"ret":false}';
}else
   echo '{"ret":false}';
