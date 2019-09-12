/*


参数列表

*/
function pass_start(uiInfo, initdata) {
    runflowtest_start(uiInfo, initdata);
}

function pass_getRunFlowTaskInfo(uiInfo, controlInfo, state) {
    return runflowtest_getRunFlowTaskInfo(uiInfo, controlInfo, state);
}

var info = getGlobalScriptInfo("pass")
pass_start(info.uiInfo, info.initdata);
