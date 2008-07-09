/*
 * Menu: Profiles > Create Caja Profile
 * License: Apache License 2.0
 * DOM: http://localhost/com.aptana.ide.scripting
 */

// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
function main() {
 	loadBundle("com.aptana.ide.editors");
    var profileObject = Packages.com.aptana.ide.editors.profiles.Profile;
    var profile = new profileObject("Caja", "caja", true);
    var temp = new Array();
    var base = "bundleentry://com.google.caja.ecajalipse/libraries/lib/caja/";
    var i = 0;
    temp[i] = bundles.resolveInternalUrl(base + "caja.js");
	profile.addURIs(temp);
    profiles.addProfile(profile);
}
