/*


参数列表

*/
function query_start(uiInfo, initdata) {
    runflowtest_start(uiInfo, initdata);
}

function query_getRunFlowTaskInfo(uiInfo, controlInfo, state) {
    return runflowtest_getRunFlowTaskInfo(uiInfo, controlInfo, state);
}

var info = getGlobalScriptInfo("query")
query_start(info.uiInfo, info.initdata);
