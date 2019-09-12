var GlobalDialog = {
    dialogs: [],

    showFrameDialog: function (uiName, title, onload, ondestory, userparam) {
        var dialog = new FrameDialog();
        GlobalDialog.dialogs.push(dialog);
        dialog.showFrameDialog(uiName, title, onload, function (userparam, dialogId) {
                GlobalDialog.removeDialog();
                if (!Tools.isNull(ondestory))
                    ondestory(userparam, dialogId);
            }, userparam,
            GlobalDialog.dialogs.length, -1, -1);
    },

    removeDialog: function () {
        if (GlobalDialog.dialogs.length == 0)
            return;
        var index = GlobalDialog.dialogs.length - 1;
        var dialog = GlobalDialog.dialogs[index];
        GlobalDialog.dialogs.splice(index, 1);
        return dialog;
    },

    showDialog: function (title, dialogWidth, dialogHeight, onload, ondestory, param) {
        var dialog = new FrameDialog();
        GlobalDialog.dialogs.push(dialog);
        dialog.showFrameDialog(title, dialogWidth,
            onload, ondestory, param,
            GlobalDialog.dialogs.length, dialogWidth, dialogHeight);
    },

    close: function () {
        if (GlobalDialog.dialogs.length == 0)
            return;

        var index = GlobalDialog.dialogs.length - 1;
        var dialog = GlobalDialog.dialogs[index];
        dialog.close();
    },

    clear: function (uiInfo) {
        if (GlobalDialog.dialogs.length == 0)
            return;
        var inputHash = uiInfo.nameHash;
        var labels = uiInfo.labels;
        for (var i in inputHash) {
            if (inputHash[i] == labels[i]) {
                delete inputHash[i];
            }
        }
        for (var k in inputHash) {
            if (inputHash[k].element.typename == "上传框") {
                var control = getFrameControlByName(uiInfo, k);
                control = getFrameControl(control);
                control.clear();
            } else if (inputHash[k].element.typename != "按钮") {
                var control = getFrameControlByName(uiInfo, k)
                setFrameControlValue(control, "");
            }
        }
    },
}