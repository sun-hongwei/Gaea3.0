package com.wh.system.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class HostInfoHelp {
    public static String getUserName(){  
        Map<String,String> map = System.getenv();  
        return map.get("USERNAME");
    }  
    public static String getComputerName(){  
        Map<String,String> map = System.getenv();  
        return map.get("COMPUTERNAME");
    }  
    public static String getDomain(){  
        Map<String,String> map = System.getenv();  
        return map.get("USERDOMAIN");
    }  
    //得到计算机的ip地址和mac地址  
    public static String getIP(){  
        try{  
            InetAddress address = InetAddress.getLocalHost();  
            return address.getHostAddress();  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
        return null;
    }  

    public static String getHostName(){  
        try{  
            InetAddress address = InetAddress.getLocalHost();  
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);  
            byte[] mac = ni.getHardwareAddress();  
            String sMAC = "";  
            for (int i = 0; i < mac.length; i++) {  
                sMAC = new Formatter().format(Locale.getDefault(), "%02X%s", mac[i],  
                        (i < mac.length - 1) ? "-" : "").toString();  
  
            }  
            return sMAC;  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
        return null;
    }  

    public static String getOSName(){  
        Properties props = System.getProperties();  
        return props.getProperty("os.name");
    } 

    public static String getOSVersion(){  
        Properties props = System.getProperties();  
        return props.getProperty("os.version");
    } 
}
