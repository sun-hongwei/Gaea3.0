// JavaScript Document
var code_base = {'0':0,'1':1,'2':2,'3':3,'4':4,'5':5,'6':6,'7':7,'8':8,'9':9,'a':10,'b':11,'c':12,'d':13,'e':14,'f':15};

function code_conversion(str){
	if(str.length%2!=0){
		alert('必须成对');
		return "";
	}
	
	var rtn = '';
	for(var i=0;i<str.length;i=i+2){
		var a = code_base[str[i]];
		var b = code_base[str[(i+1)]];
		var c = a*16 + b ;
		rtn = rtn + String.fromCharCode(c) ;
	}
	return rtn;
}


