package service

import addTxPoW
import createSQL
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.ServiceMDS
import ltd.mbor.minimak.jsonString

val scope = MainScope()

fun main() {
  js("""(function(global) {
    global.console = {
      log: function(msg) { MDS.log(msg) },
      trace: function(msg) { MDS.log(msg) },
      debug: function(msg) { MDS.log(msg) },
      info: function(msg) { MDS.log(msg) },
      warn: function(msg) { MDS.log(msg) },
      error: function(msg) { MDS.log(msg) }
    }
})(this);""")
  js("""(function(global) {
//    var timer = new java.util.Timer();
    var counter = 1;
    var ids = {};

    global.setTimeout = function(fn, delay) {
        var id = counter;
        counter += 1;
//        ids[id] = new JavaAdapter(java.util.TimerTask, { run : fn });
//        timer.schedule(ids[id], delay);
        fn();
        return id;
    };

    global.clearTimeout = function(id) {
//        ids[id].cancel();
//        timer.purge();
//        delete ids[id];
    };

    global.setInterval = function(fn, delay) {
        var id = counter;
        counter += 1;
//        ids[id] = new JavaAdapter(java.util.TimerTask, { run : fn });
//        timer.schedule(ids[id], delay, delay);
        fn();
        return id;
    };

    global.clearInterval = global.clearTimeout;

    // exports object in case of "isCommonJS"
    global.exports = {};

})(this);""")
  ServiceMDS.init{ msg ->
    ServiceMDS.log(msg.jsonString("event"))
    scope.launch {
      if (msg.jsonString("event") == "inited") {
        ServiceMDS.createSQL()
      } else if (msg.jsonString("event") == "NEWBLOCK") {
        ServiceMDS.addTxPoW(msg.jsonObject["data"]!!.jsonObject["txpow"]!!)
      }
    }
  }
}
