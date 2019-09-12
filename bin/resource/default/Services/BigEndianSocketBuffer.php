<?php

//禁用错误报告
// error_reporting(0);
// //报告运行时错误
// error_reporting(E_ERROR | E_WARNING | E_PARSE);
// //报告所有错误
// error_reporting(E_ALL);
//if(!class_exists('BigEndianSocketBuffer')){
require_once ('BigEndianBuffer.php');

class BigEndianSocketBuffer extends BigEndianBuffer {
	private $socket;
	private $buffer;
	private $errstr;
	/**
	 * 一个构造方法。
	 * @param string $host 主机地址
	 * @param string $port 端口号
	 */
	private $connected = false;
	public function isConnected()
	{
		return $this->connected;
	}
	
	public function getLastError(){
		return $this->errstr;
	}

	protected function tryConnectSocket($remote, $port, $timeout = 30) { 
			# this works whether $remote is a hostname or IP 
			$ip = ""; 
			if( !preg_match('/^\d+\.\d+\.\d+\.\d+$/', $remote) ) { 
				$ip = gethostbyname($remote); 
				if ($ip == $remote) { 
					$this->errstr = "Error Connecting Socket: Unknown host"; 
					return false; 
				} 
			} else $ip = $remote; 

			if (!($this->socket = @socket_create(AF_INET, SOCK_STREAM, SOL_TCP))) { 
				$this->errstr = "Error Creating Socket,code:". $error . ",msg:" . socket_strerror(socket_last_error()); 
				return false; 
			} 

			socket_set_nonblock($this->socket); 

			$error = NULL; 
			$start = time(); 
			$connected; 
			
			$index = 0;
			while (!($connected = @socket_connect($this->socket, $ip, $port)) && time() - $start < $timeout) { 
				$error = socket_last_error();
				switch($error){
					case SOCKET_EWOULDBLOCK:
					case SOCKET_EINPROGRESS:
					case SOCKET_EALREADY:
						break;
					case SOCKET_EISCONN:
						$connected = true;
						break;
					default:
						$this->errstr = "Error Connecting Socket,code:". $error . ",msg:" . socket_strerror($error); 
						socket_close($this->socket); 
						return false; 
				}
				if ($connected)
					break;

				if ($index > 5)
					$index = 5;

				usleep($index++ * 50); 
			} 

			if (!$connected) { 
				$error = socket_last_error();
				$this->errstr = "Error Connecting Socket: Connect Timed Out After $timeout seconds,code:". $error . ",msg:" . socket_strerror($error); 
				socket_close($this->socket); 
				return false; 
			} 
			
			socket_set_block($this->socket); 

			return true;      
	} 

	public function __construct($host, $port) {
		$this->connected = $this->tryConnectSocket($host, $port);
	}

	/**
	 * @see BigEndianBuffer::readBytes()
	 * @param int $len
	 * @return bytes
	 */
	public function readBytes($len) {
		if (is_null ( $len ) || $len < 0) {
			return false;
		}
		$data = '';
		while ($len > 0)
		{
			$piecelen = $len;
			if ($piecelen > 2048)
			{
				$piecelen = 2048;
			}
			$strSocket = '';
			if (($err = socket_recv ( $this->socket, $strSocket, $piecelen, MSG_WAITALL )) <= 0) {
				return false;
			}
			if ($data == '')
				$data = $strSocket;
			else
				$data .= $strSocket;
			$len -= $err;
		}
		return $data;
	}
	
	/**
	 * @see BigEndianBuffer::writeBytes()
	 *
	 * @param bytes $bytes
	 */
	public function writeBytes($bytes) {
		$len = strlen($bytes);
		$sendlen = 0;
		while ($len > $sendlen) {
			$piecelen = $len-$sendlen;
			if ($piecelen > 2048)
			{
				$piecelen = 2048;
			}
			$sendpiecelen = socket_write ( $this->socket, $bytes[$sendlen], $piecelen);
			if ($sendpiecelen <= 0){
				return false;
			}
			$sendlen += $sendpiecelen;
		}
		return true;
	}
	public function Close()
	{
		socket_close($this->socket);				
	}
}
//}
?>