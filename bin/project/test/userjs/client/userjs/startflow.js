/*


参数列表

*/
function startflow_start(uiInfo, initdata) {
    runflowtest_start(uiInfo, initdata);

}

function startflow_getRunFlowTaskInfo(uiInfo, controlInfo, state) {
    return runflowtest_getRunFlowTaskInfo(uiInfo, controlInfo, state);
}

var info = getGlobalScriptInfo("startflow")
startflow_start(info.uiInfo, info.initdata);
