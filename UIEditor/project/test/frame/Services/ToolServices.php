<?php
require_once('Common.php');
require_once('Configure.php');

error_reporting(0);

$command = isset ( $_POST ["command"] ) ? $_POST ["command"] : NULL;
$require = $_POST;

function getFileContent($file_path){
	if(file_exists($file_path)){
		$str = file_get_contents($file_path);//将整个文件内容读入到一个字符串中
		return $str;
	}
	return null;
}

function getReportFileContent($name){
	$file_path = "../report/" . $name;
	return getFileContent($file_path);
}

function getDataSourceFileContent($name){
	$file_path = "../datasource/" . $name;
	return getFileContent($file_path);
}

function getUIFileContent($name){
	$file_path = "../business/" . $name;
	return getFileContent($file_path);
}

function getConfigFileContent($name){
	$file_path = "../config/" . $name;
	return getFileContent($file_path);
}

function getMenuFileContent($name){
	$file_path = "../menu/" . $name;
	return getFileContent($file_path);
}

function getTreeFileContent($name){
	$file_path = "../tree/" . $name;
	return getFileContent($file_path);
}

function fillTreeDataRole($id, $data){
	$result = array();
	for($i = 0; $i < count($data); $i++){
		if ($id == $data[$i]["id"]){
			$result[$id] = $data[$i];
			$subs = fillTreeDataRole($data[$i]["pid"], $data);
			foreach ($subs as $key => $value) {
				$result[$key] = $value;
			}
		// } else if ($id == $data[$i]["pid"]) {
		// 	$result[$data[$i]["id"]] = $data[$i];
		}
	}
	return $result;
}


if (isset( $require["Command_Running_Hint"] ))
{
}

if ($command != "getdate")
{
	if (!CommonObject::VerfiyMD5ForRequest($require)){
		$rjson = CommonObject::CreatErrorResponse ( - 10001, "数据已经被篡改！" , 
		CommonObject::GetSessionIDForRequest($require), 
		CommonObject::GetSessionInfoForRequest($require));
		return $rjson;
	}
}
if ($command == "getdate")
{
	$rjson["ret"] = true;
	$rjson["value"] = date("Y-m-d H:i:s");
	echo json_encode($rjson);
}
else if ($command == "getUIMainContent")
{
	$str = getUIFileContent("main.dat");
	if(!empty($str)){
		$json = json_decode($str, true);
		if (isset($json["main"])){
			$str = getUIFileContent($json["main"]);
			if(!empty($str)){
				echo '{"ret":true,"data":' . $str . '}';
				return;
			}
		}
	}
	echo '{"ret":false}';
}
else if ($command == "getMenuMainContent")
{
	$str = getMenuFileContent("main.menu");
	if(!empty($str)){
		echo '{"ret":true,"data":' . $str . '}';
	}else
		echo '{"ret":false}';
}
else if ($command == "logoff"){
	$userid = $require["data"]["value"][0]["params"]["userid"];
	echo '{"ret":true}';
}
else if ($command == "getTreeMainContent")
{
	$str = getTreeFileContent("main.tree");
	if(!empty($str)){
		echo '{"ret":true,"data":' . $str . '}';
		return;
	}
	echo '{"ret":false}';
}
else if ($command == "getDataSourceContent")
{
	$name = $require["data"]["value"][0]["params"]["name"];
	$str = getDataSourceFileContent($name . ".dsm");
	if(!empty($str)){
		echo '{"ret":true,"data":' . $str . '}';
		return;
	} 
	echo '{"ret":false}';
}
else if ($command == "getMetaFile")
{
	$str = getConfigFileContent("main.meta");
	if(!empty($str)){
		echo '{"ret":true,"data":' . $str . '}';
		return;
	}
	echo '{"ret":false}';
}
else if ($command == "getTomcatUri"){
	$uri = array();
	$uri["ret"] = true;
	$uri["uri"] = Configure::get("ServiceCommunication", "uri");
	$str = json_encode($uri);
	echo $str;
}
else if ($command == "getUIContent")
{
	$name = $require["data"]["value"][0]["params"]["name"];
	$str = getUIFileContent($name . ".js");
	if(!empty($str)){
		echo '{"ret":true,"data":' . $str . '}';
	}else
		echo '{"ret":false}';
}
else if ($command == "getReportInfo")
{
	$name = $require["data"]["value"][0]["params"]["name"];
	$str = getReportFileContent($name . ".rpt");
	if(!empty($str)){
		echo '{"ret":true,"data":' . $str . '}';
	}else
		echo '{"ret":false}';
}

?>