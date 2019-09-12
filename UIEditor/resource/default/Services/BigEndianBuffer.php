<?php
//if(!class_exists('BigEndianBuffer')){
abstract class BigEndianBuffer {
	public function readUtf8String()
	{
		//$len = $this->readInt();
		//读长度的长度字节
		$bytes = $this->readBytes(1); 
		$res=$this->BytesToInt($bytes);
		//读长度字节
		$bytes1 = $this->readBytes($res);
		$srr = iconv('UTF-8','GBK',$bytes1);
		$result = sprintf("%d", $srr);
		//读内容
		$data = $this->readBytes($result);
		return $data;
	}
	
	public function writeUtfString($str)
	{
		//得到UTF-8编码下字符长度
		$len = strlen($str);
		//发送字符长度的长度字节
		$len_str = sprintf("%s", $len);
		$str2 = iconv('GBK','UTF-8',$len_str);
		$bylen = pack("C", strlen($str2));
		$this->writeBytes($bylen);
		//发送字符长度字节
		$this->writeBytes($str2);
		//发送字符
		$this->writeBytes ($str);
	}
	
	//字节转成整型
	public function BytesToInt($input)
	{
		//$ster = pack("C",$input);
		$res = ord($input);
		return $res;
	}
	
	public function readInt() {
		$bytes = $this->readBytes ( 4 );
		$result = unpack ( 'N', $bytes );
		$result = $result [1];
		return $result;
	}
	
	public function _pack64($v) {
		// x64
		if (PHP_INT_SIZE >= 8) {
			$v = ( int ) $v;
			return pack ( "NN", $v >> 32, $v & 0xFFFFFFFF );
		}
		// x32, int
		if (is_int ( $v )) {
			return pack ( "NN", $v < 0 ? - 1 : 0, $v );
		}
		// x32, bcmath	
		if (function_exists ( "bcmul" )) {
			if (bccomp ( $v, 0 ) == - 1) {
				$v = bcadd ( "18446744073709551616", $v );
			}
			$h = bcdiv ( $v, "4294967296", 0 );
			$l = bcmod ( $v, "4294967296" );
			return pack ( "NN", ( float ) $h, ( float ) $l ); // conversion to float is intentional; int would lose 31st bit
		}
		// x32, no-bcmath
		$p = max ( 0, strlen ( $v ) - 13 );
		$lo = abs ( ( float ) substr ( $v, $p ) );
		$hi = abs ( ( float ) substr ( $v, 0, $p ) );
		$m = $lo + $hi * 1316134912.0; // (10 ^ 13) % (1 << 32) = 1316134912
		$q = floor ( $m / 4294967296.0 );
		$l = $m - ($q * 4294967296.0);
		$h = $hi * 2328.0 + $q; // (10 ^ 13) / (1 << 32) = 2328
		if ($v < 0) {
			if ($l == 0) {
				$h = 4294967296.0 - $h;
			} else {
				$h = 4294967295.0 - $h;
				$l = 4294967296.0 - $l;
			}
		}
		return pack ( "NN", $h, $l );
	}
	/**
	 * Portability function to unpack a x64 value with PHP limitations
	 * @return   mixed   Might return a string of numbers or the actual value
	 */
	public function _unpack64($v) {
		list ( $hi, $lo ) = array_values ( unpack ( "N*N*", $v ) );
		// x64
		if (PHP_INT_SIZE >= 8) {
			if ($hi < 0)
				$hi += (1 << 32); // because php 5.2.2 to 5.2.5 is totally fucked up again
			if ($lo < 0)
				$lo += (1 << 32);
			return ($hi << 32) + $lo;
		}
		// x32, int
		if ($hi == 0) {
			if ($lo > 0) {
				return $lo;
			}
			return sprintf ( "%u", $lo );
		} elseif ($hi == - 1) {
			// x32, int
			if ($lo < 0) {
				return $lo;
			}
			return sprintf ( "%.0f", $lo - 4294967296.0 );
		}
		$neg = "";
		$c = 0;
		if ($hi < 0) {
			$hi = ~ $hi;
			$lo = ~ $lo;
			$c = 1;
			$neg = "-";
		}
		$hi = sprintf ( "%u", $hi );
		$lo = sprintf ( "%u", $lo );
		// x32, bcmath
		if (function_exists ( "bcmul" )) {
			return $neg . bcadd ( bcadd ( $lo, bcmul ( $hi, "4294967296" ) ), $c );
		}
		// x32, no-bcmath
		$hi = ( float ) $hi;
		$lo = ( float ) $lo;
		$q = floor ( $hi / 10000000.0 );
		$r = $hi - $q * 10000000.0;
		$m = $lo + $r * 4967296.0;
		$mq = floor ( $m / 10000000.0 );
		$l = $m - $mq * 10000000.0 + $c;
		$h = $q * 4294967296.0 + $r * 429.0 + $mq;
		$h = sprintf ( "%.0f", $h );
		$l = sprintf ( "%07.0f", $l );
		if ($h == "0") {
			return $neg . sprintf ( "%.0f", ( float ) $l );
		}
		return $neg . $h . $l;
	}
}
//}
?>