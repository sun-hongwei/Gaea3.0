CustomVTypeExecutor.register('code', "无效的输入字符，仅可以输入[A-Za-z0-9_-]！", function (v) {
    var reg = new RegExp("^[A-Za-z0-9]*$"); //正则表达式 表示可以输入数字和字母
    text = v.replace(/\_/g, "");
    text = v.replace(/\-/g, "");
    if (reg.test(text)) return true;
});

CustomVTypeExecutor.register('special', "无效的输入字符，仅可以输入[A-Za-z0-9_-.]！", function (v) {
    var containSpecial = RegExp(/[(\ )(\~)(\!)(\@)(\#)(\$)(\%)(\^)(\&)(\*)(\()(\))(\+)(\=)(\[)(\])(\{)(\})(\|)(\\)(\;)(\:)(\')(\")(\,)(\/)(\<)(\>)(\?)(\)]+/);
    var ishas = containSpecial.test(v) // 判断是否含有特殊字符
    return !ishas;
});
CustomVTypeExecutor.register('chinese', "无效的输入字符，只能输入中文", function (v) {
    var containSpecial =new RegExp("^[\u4e00-\u9fa5]+$");
    var ishas = containSpecial.test(v) // 判断是否含有特殊字符
    return ishas;
});
CustomVTypeExecutor.register('english', "无效的输入字符，只能输入英文", function (v) {
    var re = new RegExp("^[a-zA-Z\_]+$");
    var ishas = re.test(v) 
    return ishas;
});
/**
 * 
 * @param {*} e 
 * @param {指定错误名称} errName
 * firstData 为控件值 secondData 为比较值
 * firstData 为大值 secondData 为小值
 */
function compare_fun(e,errName){
    var firstData =e.value;
    var secondData =undefined;
    var isTime = false;
    var ishas = false;
    for(var i=0;i<CustomVTypeExecutor.specialData.length;i++){
        if (CustomVTypeExecutor.specialData[i].name == errName) {
            secondData = CustomVTypeExecutor.specialData[i].compare;
            secondDataControl = mini.get(secondData);
            secondData = getFrameControlValue(secondDataControl);
            isTime = CustomVTypeExecutor.specialData[i].type=="date"?true:false;
            if (isTime) {
                firstData =new Date(firstData).getTime();
                secondData = new Date(secondData).getTime();
            }
            ishas =isTime?secondData>firstData:parseFloat(secondData)>parseFloat(firstData);
        }
    }
    if (e.isValid) {
        if (ishas) {
            e.errorText = "不能小于前项！";
            e.isValid = false;
        }
    }
}
function compareMax_fun(e,errName){
    var firstData =e.value;
    var secondData =undefined;
    var isTime = false;
    var ishas = false;
    for(var i=0;i<CustomVTypeExecutor.specialData.length;i++){
        if (CustomVTypeExecutor.specialData[i].name == errName) {
            secondData = CustomVTypeExecutor.specialData[i].compare;
            secondDataControl = mini.get(secondData);
            secondData = getFrameControlValue(secondDataControl);
            isTime = CustomVTypeExecutor.specialData[i].type=="date"?true:false;
            if (isTime) {
                firstData =new Date(firstData).getTime();
                secondData = new Date(secondData).getTime();
            }
            ishas =isTime?secondData>=firstData:parseFloat(secondData)>=parseFloat(firstData);
        }
    }
    if (e.isValid) {
        if (ishas) {
            e.errorText = "不能小于等于前项！";
            e.isValid = false;
        }
    }
}