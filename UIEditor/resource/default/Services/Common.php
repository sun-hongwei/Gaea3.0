<?php
date_default_timezone_set("Asia/Shanghai");
class CommonObject
{
	public static $emptyResult = NULL;
	public static $needCheckRole = true;

	public static function setSession($key, &$value){
		$env =& $_SESSION["run_env"];
		if (empty($env)){
			$_SESSION["run_env"] = array();
			$env =& $_SESSION["run_env"];
		}

		$env[$key] =& $value;
	}

	public static function &getSession($key){
		if (!CommonObject::existSession($key))
			return self::$emptyResult;

		$env =& $_SESSION["run_env"];

		return $env[$key];
	}

	public static function setValue(&$arrays, $key, &$value){
		$arrays[$key] =& $value;
	}

	public static function &getValue(&$arrays, $key){
		if (!isset($arrays[$key]))
			return self::$emptyResult;

		return $arrays[$key];
	}

	public static function existSession($key){
		return isset($_SESSION["run_env"]);
	}

	//建立错误回执json
	public static function CreatErrorResponse($errno, $erroemsg, $sid, $siinfo) {
		$json = '{"ret":false,"erron":"' . $errno . '","errmsg":"' . $erroemsg . '", "sid":"' . $sid . '","siinfo":"' . $siinfo . '"}';
		return json_decode($json, TRUE);
	}
	
	public static function filterChar($outstr) {
		
		$outstr = str_replace ( '\r', '', $outstr );
		$outstr = str_replace ( '\n', '', $outstr );
		$outstr = str_replace ( '\r\n', '', $outstr );
		$outstr = str_replace ( '\t', '', $outstr );
		$outstr = str_replace ( chr ( 13 ), '', $outstr );
		$outstr = str_replace ( PHP_EOL, '', $outstr );
		$outstr = preg_replace ( '/\s*/', '', $outstr );
		
		return $outstr;
	
	}

	public static function VerfiyMD5ForRequest(&$request)
	{
		if (!isset($request["data"]))
			return false;
		
		if (!isset($request["data"]["sign"]))
			return false;
			
		if (!isset($request["data"]["value"]))
			return false;
			
		$md5string = $request["data"]["sign"];
		$value = $request["data"]["value"];
		
		//$value=preg_replace('/[",\']/','',$value);
		$signvalue = md5($value); 
		$isok = $md5string == $signvalue;

		if ($isok)
		{
			$value = base64_decode($value);
			
			$json = json_decode($value, TRUE);
	
			$request["data"]["value"] = $json;
		}
		return $isok;
	}
	
	public static function GetSessionIDForRequest($request)
	{
		if (!isset($request["sid"]))
			return "";
		
		return $request["sid"];
	}

	public static function GetSessionInfoForRequest($request)
	{
		if (!isset($request["siinfo"]))
			return "";
		
		return $request["siinfo"];
	}

	public static function GetUserNameForRequest($request)
	{
		if (!isset($request["username"]))
			return "";
		
		return $request["username"];
	}

	public static function GetCommandForRequest($request)
	{
		if (!isset($request["command"]))
			return "";
		
		return $request["command"];
	}
	
	static function arrayRecursive(&$array, $function, $apply_to_keys_also = false)
    {
        static $recursive_counter = 0;
        if (++$recursive_counter > 1000) {
            die('possible deep recursion attack');
        }
        foreach ($array as $key => $value) {
            if (is_array($value)) {
                CommonObject::arrayRecursive($array[$key], $function, $apply_to_keys_also);
            } else {
                $array[$key] = $function($value);
            }
      
            if ($apply_to_keys_also && is_string($key)) {
                $new_key = $function($key);
                if ($new_key != $key) {
                    $array[$new_key] = $array[$key];
                    unset($array[$key]);
                }
            }
        }
        $recursive_counter--;
    }
      
    /**************************************************************
     *
     *  将数组转换为JSON字符串（兼容中文）
     *  @param  array   $array      要转换的数组
     *  @return string      转换得到的json字符串
     *  @access public
     *
     *************************************************************/
    public static function JSONToString($array) {
        CommonObject::arrayRecursive($array, 'urlencode', true);
        $json = json_encode($array);
        return urldecode($json);
    }
	
	public static function JsonString($TmpStr){
	
		
		$TmpStr=preg_replace("/\{\s*([^\"]*)\s*:/", "{\"$1\":", $TmpStr); 
		$TmpStr=preg_replace("/,\s*([^\"]*)\s*:/", ",\"$1\":", $TmpStr); 
		
		$TmpStr=preg_replace("/:\s*([^\"]*)\s*\}/", ":\"$1\"}", $TmpStr) ;
		$TmpStr=preg_replace("/:\s*([^\"]*)\s*,/", ":\"$1\",", $TmpStr); 
		
		$TmpStr = preg_replace ( "/\r/", '', $TmpStr );
		$TmpStr = preg_replace ( "/\n/", '', $TmpStr );
		$TmpStr = preg_replace ( "/\r\n/", '', $TmpStr );
		$TmpStr = preg_replace ( "/\t/", '', $TmpStr );
		return $TmpStr;
	}

	public static function create_guid() {  
		$charid = strtolower(md5(uniqid(mt_rand(), true)));  
		$hyphen = chr(45);// "-"  
		$uuid = ""//chr(123)// "{"  
		.substr($charid, 0, 8).$hyphen  
		.substr($charid, 8, 4).$hyphen  
		.substr($charid,12, 4).$hyphen  
		.substr($charid,16, 4).$hyphen  
		.substr($charid,20,12);
		//.chr(125);// "}"  
		return $uuid;  
	}
}
?>