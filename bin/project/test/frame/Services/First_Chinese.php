<?php 

function pykey( $py_key)   

{   

    $pinyin = 65536 + pys($py_key);   

    if ( 45217 <= $pinyin && $pinyin <= 45252 )   

    {   

        $zimu = "A";   

        return $zimu;   

    }   

    if ( 45253 <= $pinyin && $pinyin <= 45760 )   

    {   

        $zimu = "B";   

        return $zimu;   

    }   

    if ( 45761 <= $pinyin && $pinyin <= 46317 )   

    {   

        $zimu = "C";   

        return $zimu;   

    }   

    if ( 46318 <= $pinyin && $pinyin <= 46825 )   

    {   

        $zimu = "D";   

        return $zimu;   

    }   

    if ( 46826 <= $pinyin && $pinyin <= 47009 )   

    {   

        $zimu = "E";   

        return $zimu;   

    }   

    if ( 47010 <= $pinyin && $pinyin <= 47296 )   

    {   

        $zimu = "F";   

        return $zimu;   

    }   

    if ( 47297 <= $pinyin && $pinyin <= 47613 )   

    {   

        $zimu = "G";   

        return $zimu;   

    }   

    if ( 47614 <= $pinyin && $pinyin <= 48118 )   

    {   

        $zimu = "H";   

        return $zimu;   

    }   

    if ( 48119 <= $pinyin && $pinyin <= 49061 )   

    {   

        $zimu = "J";   

        return $zimu;   

    }   

    if ( 49062 <= $pinyin && $pinyin <= 49323 )   

    {   

        $zimu = "K";   

        return $zimu;   

    }   

    if ( 49324 <= $pinyin && $pinyin <= 49895 )   

    {   

        $zimu = "L";   

        return $zimu;   

    }   

    if ( 49896 <= $pinyin && $pinyin <= 50370 )   

    {   

        $zimu = "M";   

        return $zimu;   

    }   

    if ( 50371 <= $pinyin && $pinyin <= 50613 )   

    {   

        $zimu = "N";   

        return $zimu;   

    }   

    if ( 50614 <= $pinyin && $pinyin <= 50621 )   

    {   

        $zimu = "O";   

        return $zimu;   

    }   

    if ( 50622 <= $pinyin && $pinyin <= 50905 )   

    {   

        $zimu = "P";   

        return $zimu;   

    }   

    if ( 50906 <= $pinyin && $pinyin <= 51386 )   

    {   

        $zimu = "Q";   

        return $zimu;   

    }   

    if ( 51387 <= $pinyin && $pinyin <= 51445 )   

    {   

        $zimu = "R";   

        return $zimu;   

    }   

    if ( 51446 <= $pinyin && $pinyin <= 52217 )   

    {   

        $zimu = "S";   

        return $zimu;   

    }   

    if ( 52218 <= $pinyin && $pinyin <= 52697 )   

    {   

        $zimu = "T";   

        return $zimu;   

    }   

    if ( 52698 <= $pinyin && $pinyin <= 52979 )   

    {   

        $zimu = "W";   

        return $zimu;   

    }   

    if ( 52980 <= $pinyin && $pinyin <= 53640 )   

    {   

        $zimu = "X";   

        return $zimu;   

    }   

    if ( 53689 <= $pinyin && $pinyin <= 54480 )   

    {   

        $zimu = "Y";   

        return $zimu;   

    }   

    if ( 54481 <= $pinyin && $pinyin <= 62289 )   

    {   

        $zimu = "Z";   

        return $zimu;   

    }   

    $zimu = $py_key;   

    return $zimu;   

}   

function pys( $pysa )   

{   

    $pyi = "";   

    $i= 0;   

    for ( ; $i < strlen( $pysa ); $i++)   

    {   

        $_obfuscate_8w= ord( substr( $pysa,$i,1) );   

        if ( 160 < $_obfuscate_8w)   

        {   

            $_obfuscate_Bw = ord( substr( $pysa, $i++, 1 ) );   

            $_obfuscate_8w = $_obfuscate_8w * 256 + $_obfuscate_Bw - 65536;   

        }   

        $pyi.= $_obfuscate_8w;   

    }   

    return $pyi;   

}   

function getfirst($str, $charset='utf8'){
         $dict=array(
         'a'=>0xB0C4,
         'b'=>0xB2C0,
         'c'=>0xB4ED,
         'd'=>0xB6E9,
         'e'=>0xB7A1,
         'f'=>0xB8C0,
         'g'=>0xB9FD,
         'h'=>0xBBF6,
         'j'=>0xBFA5,
         'k'=>0xC0AB,
         'l'=>0xC2E7,
         'm'=>0xC4C2,
         'n'=>0xC5B5,
         'o'=>0xC5BD,
         'p'=>0xC6D9,
         'q'=>0xC8BA,
         'r'=>0xC8F5,
         's'=>0xCBF9,
         't'=>0xCDD9,
         'w'=>0xCEF3,
         'x'=>0xD188,
         'y'=>0xD4D0,
         'z'=>0xD7F9,
         );
         if ('utf8' == $charset){
             $str = iconv("UTF-8", "gb2312", $str);
         }
         $str_1 = substr($str, 0, 1);
         //取GB2312字符串首字母,原理是GBK汉字是按拼音顺序编码的.
         if ($str_1>=chr(0x81) && $str_1<=chr(0xfe)){
             $num = hexdec(bin2hex(substr($str, 0, 2)));
             foreach ($dict as $k=>$v){
                 if($v>=$num){
                     break;
                 }
             }
             return $k;
         }else {
             return $str_1;
         }
            
    }

?>  

