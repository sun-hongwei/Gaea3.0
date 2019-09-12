/*


参数列表

*/
function allpass_start(uiInfo, initdata) {
    runflowtest_start(uiInfo, initdata);
}

function allpass_getRunFlowTaskInfo(uiInfo, controlInfo, state) {
    return runflowtest_getRunFlowTaskInfo(uiInfo, controlInfo, state);
}

var info = getGlobalScriptInfo("allpass")
allpass_start(info.uiInfo, info.initdata);
