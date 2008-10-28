// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * @fileoverview
 * An XMLHttpRequest wrapper.
 */

var xmlhttp = (function () {

  // Domain for XmlHTTPRequest.readyState
  var XML_READY_STATE_UNINITIALIZED  = 0;
  var XML_READY_STATE_LOADING        = 1;
  var XML_READY_STATE_LOADED         = 2;
  var XML_READY_STATE_INTERACTIVE    = 3;
  var XML_READY_STATE_COMPLETED      = 4;

  /** Helper function to get the status of a request object */
  function getResponseStatus(req) {
    var status = -1;
    try {
      status = req.status;
    } catch (ex) {
      // firefox may throw an exception when you access request values
    }
    return status;
  }

  /**
   * Wraps a handler to ensure that memory doesn't leak.
   *
   * @param {XMLHttpRequest} req the request object.
   * @param {function () : void} handler the handler to wrap.
   * @return {function () : void} the wrapped handler.
   */
  function responseClosure(req, handler) {
    return function () {
      if (req.readyState == XML_READY_STATE_COMPLETED) {
        // See Memory Leaks and Ajax/XHR at
        // http://isaacschlueter.com/2006/10/msie-memory-leaks/
        // We need to break the reference loop.  On Firefox, this is not the
        // XMLHttpRequest object, so we have to pass req in.  To break the cycle
        // we remove listeners from the XMLHttpRequest object.
        // See the below for an explanation of why we use noop instead of null:
        // http://www.mail-archive.com/mochikit@googlegroups.com/msg03527.html
        // http://www.zanthan.com/itymbi/archives/002257.html#002257
        req.onreadystatechange = noopHandler;

        var status = getResponseStatus(req);
        if (status === 0 || (status > 0 && (status - (status % 100)) === 200)) {
          var content = req.responseText;
          // Make sure that xml http response processing does not interleave
          // with other code.
          setTimeout(function () { handler(req.responseText); }, 0);
        }
      }
    };
  }

  function noopHandler() {}

  /**
   * Candidate Active X types.
   * @private
   */
  var XH_ACTIVE_X_IDENTS_ = [
    "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0",
    "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1",
    "MICROSOFT.XMLHTTP" ];
  /**
   * The active x identifier used for ie.
   * @private
   */
  var xh_ieProgId;

  // Nobody (on the web) is really sure which of the progid's listed is totally
  // necessary. It is known, for instance, that certain installations of IE will
  // not work with only Microsoft.XMLHTTP, as well as with MSXML2.XMLHTTP.
  // Safest course seems to be to do this -- include all known progids for
  // XmlHttp.
  if (typeof XMLHttpRequest === 'undefined' &&
      typeof ActiveXObject !== 'undefined') {
    for (var i = 0; i < XH_ACTIVE_X_IDENTS_.length; i++) {
      var candidate = XH_ACTIVE_X_IDENTS_[i];

      try {
        new ActiveXObject(candidate);
        xh_ieProgId = candidate;
        break;
      } catch (e) {
        // do nothing; try next choice
      }
    }

    // couldn't find any matches
    if (!xh_ieProgId) {
      // ActiveX might be disabled, or msxml might not be installed
      throw ("Could not create ActiveXObject");
    }
  }

  /**
   * Create and return an xml http request instance.
   * @return {XMLHttpRequest}
   */
  function xmlhttpCreate() {
    return xh_ieProgId ? new ActiveXObject(xh_ieProgId)
                       : new XMLHttpRequest();
  }

  /**
   * Sends a message synchronously
   * @param {string} resource a URL.
   * @param {function (XMLHttpRequest) : void} callback that will be called with
   *     the response text.
   */
  function getExternalResource(resource, callback) {
    var req = xmlhttpCreate();
    req.onreadystatechange = responseClosure(req, callback);
    req.open('GET', resource, true);
    req.send(null);
  }

  return {
    getExternalResource: getExternalResource
  };
})();
