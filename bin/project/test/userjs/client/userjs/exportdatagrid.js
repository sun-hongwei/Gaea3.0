/*

参数列表

@Param {json} uiInfo 窗体描述信息
@Param {json} initData 初始化信息，一般通过Gaea系统设置，可以为空 
*/
function exportdatagrid_start(uiInfo, initdata) {
    ControlHelp.setEvent(uiInfo, "export", "onClick", function (file) {
        var datagridcontrol = getFrameControlObjectByName(uiInfo, "linkGrid");
        datagridcontrol.exportToExcel("linkGrid", "表格")
    });
    ControlHelp.setEvent(uiInfo, "printReport", "onClick", function (file) {
        var report = getFrameControlObjectByName(uiInfo, "reportmain");
        report.print();
    });

}

/*
@Param {json} uiInfo 窗体描述信息
@Param {json} controlInfo 当前用户单击的控件描述信息
@Param {string} state 当前用户选择的状态（decidevalue）
*/
function exportdatagrid_getRunFlowTaskInfo(uiInfo, controlInfo, state) {
}

/*
@Param {json} uiInfo 窗体描述信息
@Param {json} controlInfo 当前用户单击的控件的描述信息，controlInfo.id为控件id，controlInfo.name为控件name
@Param {json} actionInfo 执行动作的描述信息，格式如下：
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
function exportdatagrid__ActionExecuted(uiInfo, controlInfo, actionInfo) {
}

/*

*/
var exportdatagrid = {

}

var info = getGlobalScriptInfo("exportdatagrid")
exportdatagrid_start(info.uiInfo, info.initdata);