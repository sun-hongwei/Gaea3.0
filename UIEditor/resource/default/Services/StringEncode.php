<?php

    function convertStringCode($data, $encodeName = 'UTF-8'){
        return mb_convert_encoding($data, $encodeName);
    }

    function convertDBStringCode($data, $encodeName = 'UTF-8'){
        $encodeName = strtoupper($encodeName);

        $encode = mb_detect_encoding($data, array('GB2312',"GBK",'BIG5', 'UTF-8', $encodeName, "ASCII", "ISO-8859-1"), true); 
        if (empty($encode))
            return $data;
            
        $needEncode = true;
        $isPageCode = true;
        switch ($encode){
            case "EUC-CN"://GB2312
            case "CP51936"://EUC-CN
            case "CP936"://GBK
            case "CP54936"://GB18030
            case "CP20936"://"GB2312"
                $needEncode = $encodeName != "GB2312" && $encodeName != "GBK" && $encodeName != "GB18030";
                break;
            case "CP950":
                $needEncode = $encodeName != "BIG5";
                break;
            case "CP20127":
                $needEncode = $encodeName != "ASCII";
                break;
            case "CP1252":
                $needEncode = $encodeName != "ISO-8859-1";
                break;
            case "CP65001":
                $needEncode = $encodeName != "UTF-8";
                break;
            default:
                $isPageCode = false;
                break;
        }
        if ($needEncode && !$isPageCode){
            $needEncode = strtoupper($encode) != $encodeName;
        }

        if ($needEncode) 
            return iconv($encode, $encodeName, $data);  
        else
            return $data;
    }

?>