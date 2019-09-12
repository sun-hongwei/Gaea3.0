function TimerControl() {
    this.timerName = new Date().getTime() + "_" + Math.floor(Math.random() * 100);
    this.timerTime = 0;
    this.asyn = false;
    this.times = -1;
    this.oldtimes = 0;
    this.cellback = undefined;
    this.pausetimes = 0;
    this.restart = function (cellback) {
        if (!cellback) {
            cellback = this.cellback;
        }
        this.oldtimes = this.pausetimes;
        this.times += 1;
        if (this.times != this.oldtimes) {
            this.setControllableTimer(cellback, true);
        }
    }
    this.resetTimer = function (times, cellback) {
        var olddata_times = this.times;
        var olddata_oldtimes = this.oldtimes
        if (this.times == 0) {
            var timer = new TimerControl();
            var restart = true;
        } else {
            timer = this;
        }
        timer.timerTime = this.timerTime;
        timer.asyn = this.asyn;
        if (!cellback) {
            cellback = this.cellback;
        }
        timer.oldtimes = olddata_times == 0 ? 0 : (times == undefined ? olddata_oldtimes : times);
        if (timer.oldtimes == 0) {
            timer.times = times == undefined ? olddata_oldtimes : times;
        } else {
            times = times == undefined ? 0 : times;
            timer.times = times - (olddata_oldtimes - olddata_times);
        }
        if (restart)
            timer.setControllableTimer(cellback, true);
    }
    this.pauseTimer = function () {
        this.pausetimes = this.oldtimes;
        this.oldtimes = this.times - 1;
        clearTimeout("timer_" + this.timerName);
    }
    this.setControllableTimer = function (cellback, restart) {
        this.setTimeoutControl(cellback, restart);
    }
    this.setTimeoutControl = function (cellback, restart) {
        var _this = this;
        if (_this.oldtimes == 0) {
            _this.oldtimes = _this.times;
        }
        if (restart) {
            this.cellback = cellback;
        }
        eval("var timer_" + _this.timerName + "=" +
            setTimeout(function () {
                var times = _this.times == -1 ? 1 : _this.times;
                if (_this.times != 0) {
                    _this.times = times - 1;
                }
                var thisTimes = _this.oldtimes - _this.times;
                if (_this.oldtimes == _this.times) {
                    return false;
                } else if (_this.times == 0) {
                    cellback(thisTimes, true);
                } else {
                    cellback(thisTimes, false);
                    _this.setTimeoutControl(cellback);
                }
            }, _this.timerTime)
        );
    }
    this.setCycleTimer = function (cellback) {
        if (this.asyn)
            this.setTimeoutCycleTimer(cellback);
        else
            this.setIntervalCycleTimer(cellback);
    }
    this.setOnceTimer = function (cellback) {
        if (this.asyn)
            this.setTimeoutOnceTimer(cellback);
        else
            this.setIntervalOnceTimer(cellback);
    }
    this.setTimeoutCycleTimer = function (cellback) {
        var _this = this;
        if (_this.oldtimes == 0) {
            _this.oldtimes = _this.times;
        }
        var times = _this.times == 0 ? 1 : _this.times;
        eval("var timer_" + _this.timerName + "=" +
            setTimeout(function () {
                if (times > 0) {
                    if (_this.times != 0) {
                        _this.times = times - 1;
                    } else {
                        _this.times = -(times + 1);
                    }
                    cellback(_this.oldtimes - _this.times);
                    _this.setTimeoutCycleTimer(cellback);
                } else {
                    clearTimeout(_this.timerName);
                }
            }, _this.timerTime)
        );
    }
    this.setTimeoutOnceTimer = function (cellback) {
        this.timerName = setTimeout(cellback, this.timerTime);
    }
    this.setIntervalCycleTimer = function (cellback) {
        var _this = this;
        _this.timerName = setInterval(function () {
            _this.oldtimes += 1;
            cellback(_this.oldtimes);
        }, _this.timerTime)
    }
    this.setIntervalOnceTimer = function (cellback) {
        var _this = this;
        _this.timerName = setInterval(function () {
            var times = _this.times;
            if (times == -1) {
                cellback()
                _this.times += 1;
            } else {
                clearInterval(_this.timerName);
            }
        }, _this.timerTime)
    }
    this.stopCycleTimer = function () {
        window.clearInterval(this.timerName);
    }
}
