# New Project Template

**This is not an officially supported Google product.**

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

    Copyright 2020 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

# CUJ recording

## Usage

### executeCuj

To get data on the latency of a CUJ running on a connected device, run 

```bash
./executeCuj <input_file>
```

where input file is formatted as follows (all on seperate lines):

```bash
<preparatory_actions> 
<measured_actions> 
<num_iterations> 
<r?>
```

preparatory_actions consists of actions to be performed prior to the CUJ 
in question and measured actions consists of the CUJ in question plus a final 
"termination" action, which will only be able to be performed once the last
action in the CUJ has fully terminated. num_iterations denotes the number of 
iterations you'd like the test to run through your CUJ. Optionally, you may 
follow the command with "r" to signify that you'd like the run of median length
to be outputted by the program. (Note that if you'd like to record, the entire
test, including eachiteration, much have length <= 3 minutes.)

the action list lines must be formatted as follows:

```bash
<action1>, <action2>, ... , <actionN>
```
and each action is either to launch an app, click on a particular View or to enter 
text in a particular textbox. Actions are represented in one of the following forms:
     
      1. “start;package_name" to launch the specified package.
      2. “edit;text_displayed;text_entered” to enter text_entered into a textbox, where text_displayed is the text currently visible in the textbox.
      3. “click;text_displayed” to click a non-editable view (normally a text-view or button) on the screen which has text containing the string text_displayed displayed on it
      4. “clickImage;text_description” to click a view without text (normally an image view) where the description of the view contains the string description_text
     
Note that all text fields are case sensitive. Additionally, if an action of form 
2, 3, or 4 is followed by “;strict” the search for a corresponding view will enforce 
that an exact match is found for text_displayed (in cases 2 or 3) or text_description 
(in case 4).

### stitch

to stitch together several videos to compare them side-by-side, use the stitch command 

