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
 * Extract a calendar from the document, hook it into the calendar widget,
 * and render it in the DOM.
 *
 * @author mikesamuel@gmail.com
 */

(function () {
  var baseDate = time.date(2008, 4, 28);
  var displayPeriod = time.duration(5, 0, 0, 0);  // 5 days
  var incrementPeriod = time.duration(7, 0, 0, 0);  // 1 week

  function updateCalendar() {
    var endDate = time.plusDuration(baseDate, displayPeriod);
    var vcalendar = event_store.toCalendar(
        extractHcal(document.getElementById('datastore')), baseDate, endDate);
    var layoutPolicy = new LayoutPolicy(7, baseDate, 2);
    var layout = new Layout(layoutPolicy);
    layout.layout(baseDate, endDate, vcalendar.events);

    var container = document.getElementById('container');
    var html = widget.render(layout);
    container.innerHTML = html;
    gadgets.window.adjustHeight();
  }

  updateCalendar();

  document.getElementById('prev-button').onclick = function () {
    baseDate = time.plusDuration(baseDate, -incrementPeriod);
    updateCalendar();
  };

  document.getElementById('next-button').onclick = function () {
    baseDate = time.plusDuration(baseDate, incrementPeriod);
    updateCalendar();
  };

  var t1 = (new Date).getTime();
})();
