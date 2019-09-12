var GlobalCookies = {
    USER_DATA_ROLE_KEY: "USER_DATA_ROLE_KEY",
    USER_FUNCTION_ROLE_KEY: "USER_FUNCTION_ROLE_KEY",
    /*向cookie中存储值*/
    setCookie: function(key, value) {
        if (Tools.isObject(value))
            value = JSON.stringify(value);
        document.cookie = key + "=" + escape(value);
    },

    removeCookie: function(name) {
        var exp = new Date();
        exp.setTime(exp.getTime() - 1000);
        var cval = GlobalCookies.getCookie(name);
        if (cval != null)
            document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
    },

    /*从cookie中取值*/
    getCookie: function(key) {
        var strCookie = document.cookie;
        if (strCookie.indexOf(key) == -1) {
            return "";
        } else {
            //将多cookie切割为多个名/值对 
            var arrCookie = strCookie.split("; ");
            //遍历cookie数组，处理每个cookie对 
            for (var i = 0; i < arrCookie.length; i++) {
                var arr = arrCookie[i].split("=");
                //alert(arr[0]);
                //找到名称为userId的cookie，并返回它的值 
                if (key == arr[0]) {
                    //alert(arr[1]);
                    return unescape(arr[1]);
                }
            }
            return "";
        }
    },

    /*清除session*/
    clearCookies: function() {
        if (document.cookie == "")
            return;
        var strCookie = document.cookie;
        //将多cookie切割为多个名/值对 
        var arrCookie = strCookie.split("; ");
        for (var i = 0; i < arrCookie.length; i++) {
            var arr = arrCookie[i].split("=");
            document.cookie = arr[0] + "=;expires=" + (new Date(0)).toGMTString();
        }
    },

}