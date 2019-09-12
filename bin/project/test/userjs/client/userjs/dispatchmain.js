/*

参数列表

@Param {json} uiInfo 窗体描述信息
@Param {json} initData 初始化信息，一般通过Gaea系统设置，可以为空 
*/
function dispatchmain_start(uiInfo, initdata) {
    ControlHelp.setEvent(uiInfo, "showgrid",
        "onClick",
        function (e, uiInfo, control) {
            var control = getFrameControlByName(uiInfo, "linkGrid");
            var value = getFrameControlValue(control);
            alert("值：" + JSON.stringify(value) + "！");
        });
    
    var datecontrol = getFrameControlByName(uiInfo, "date1");
    var dc = getFrameControl(datecontrol);
    dc.setSelectDays(1);
    dc.setSelectDays(2);
    dc.setSelectDays(21);
    dc.setSelectDays(4);
    dc.setSelectDays(5);
    dc.setSelectDays(11);
    dc.setSelectDates(new Date("2019-02-23"));
}

/*
@Param {json} uiInfo 窗体描述信息
@Param {json} controlInfo 当前用户单击的控件的描述信息
@Param {string} state 当前用户单击的控件的描述信息
*/
function dispatchmain_getRunFlowTaskInfo(uiInfo, controlInfo, state) {}

/*
@Param {json} uiInfo 窗体描述信息
@Param {json} controlInfo 当前用户单击的控件的描述信息，controlInfo.id为控件id，controlInfo.name为控件name
@Param {json} actionInfo 当前用户单击的控件的描述信息，格式如下：
{inputstate:"控件关联得判定值（decidevalue）", jumpedstate:"本次action执行后，现在所处的状态", ret:0}
ret含义如下：
0当前节点可跳转，state为目标状态码
1当前节点不可跳转，等待其他节点状态完成，state为目标状态码
2当前节点已经为最后一个可执行状态节点，state为终止状态码
3当前节点已经为最后一个可执行状态节点，且通过会签的非会签节点跳转到此节点，state为终止状态码
4当前节点可以跳转，并且当前为and节点[会签]，但输入的状态并不是and状态集合中的一个，说明客户做了and外的跳转，
5当前节点不可以跳转，输入的state不是当前任务的合法下级节点，
6当前节点可以跳转，多个and节点全部完成，
*/
function dispatchmain__ActionExecuted(uiInfo, controlInfo, actionInfo) {}

/*

*/
var dispatchmain = {

}

var info = getGlobalScriptInfo("dispatchmain")
dispatchmain_start(info.uiInfo, info.initdata);