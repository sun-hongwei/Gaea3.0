function writeToDocument(selector, headerString) {
    selector.append(headerString);
}

var DocumentWriter = function() {
    this.text = "";
    this.needOneWrite = false;
    this.writeForElement = function(element, headerString) {
        var dataheader = { tabParent: element };
        this.write(dataheader, headerString);
    }
    this.write = function(data, headerString) {
        if (!Tools.isNull(data.parent)) {
            writeToDocument($("#" + data.parent), headerString);
        } else if (!Tools.isNull(data.tabParent)) {
            writeToDocument($(data.tabParent), headerString);
        } else if (!Tools.isNull(data.parentLayout)) {
            writeToDocument($(data.parentLayout), headerString);
        } else {
            if (!this.needOneWrite) {
                $('body').append(headerString);
            } else {
                this.text += headerString;
            }
        }
    }

    this.beginWrite = function() {
        this.text = "";
        this.needOneWrite = true;
    }

    this.endWrite = function() {
        if (!this.needOneWrite)
            return;

        if (Tools.isNull(this.text))
            return;

        with(document) {
            write(this.text);
            close();
        }
    }
}